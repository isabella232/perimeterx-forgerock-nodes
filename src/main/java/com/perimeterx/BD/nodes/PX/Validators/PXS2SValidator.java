package com.perimeterx.BD.nodes.PX.Validators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.perimeterx.BD.nodes.pxVerificationNode.Config;
import com.perimeterx.BD.nodes.PX.PXConstants;
import com.perimeterx.BD.nodes.PX.PXContext;
import com.perimeterx.BD.nodes.PX.PXLogger;
import com.perimeterx.BD.nodes.PX.ActivitiesData.BlockReason;
import com.perimeterx.BD.nodes.PX.ActivitiesData.PassReason;
import com.perimeterx.BD.nodes.PX.Exceptions.PXException;
import com.perimeterx.BD.nodes.PX.RiskAPI.RiskRequest;
import com.perimeterx.BD.nodes.PX.RiskAPI.RiskResponse;
import com.perimeterx.BD.nodes.PX.RiskAPI.S2SErrorReason;
import com.perimeterx.BD.nodes.PX.RiskAPI.S2SErrorReasonInfo;
import com.perimeterx.BD.nodes.PX.Utils.JsonUtils;
import com.perimeterx.BD.nodes.PX.Utils.PXUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class PXS2SValidator implements PXValidator {
    private static final PXLogger logger = PXLogger.getLogger(PXCookieValidator.class);
    private static final Charset UTF_8 = Charset.forName("utf-8");

    private Config pxConfiguration;
    private CloseableHttpClient httpClient;

    public PXS2SValidator(Config pxConfiguration) {
        this.pxConfiguration = pxConfiguration;
        initHttpClient();
    }

    private void initHttpClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(5);
        cm.setDefaultMaxPerRoute(5);
        httpClient = HttpClients.custom().setConnectionManager(cm)
                .setDefaultHeaders(PXUtils.getDefaultHeaders(String.valueOf(pxConfiguration.pxAuthToken()))).build();
    }

    private CloseableHttpResponse executeRiskAPICall(String requestBody, PXContext ctx) throws ConnectTimeoutException {
        String serverUrl = String.format(PXConstants.SERVER_URL, this.pxConfiguration.pxAppId());
        HttpPost post = new HttpPost(serverUrl + PXConstants.API_RISK);
        post.setEntity(new StringEntity(requestBody, UTF_8));
        post.setConfig(PXUtils.getRequestConfig(pxConfiguration));

        try {
            return httpClient.execute(post);

        } catch (ConnectTimeoutException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw new ConnectTimeoutException(e.getMessage());
        } catch (IOException e) {
            logger.error("unable_to_send_request: {}", e);
        }
        return null;
    }

    private String createRequestBody(PXContext ctx) {
        try {
            RiskRequest riskRequest = RiskRequest.fromContext(ctx);
            String requestBody = JsonUtils.writer.writeValueAsString(riskRequest);
            logger.debug("Risk API Request: {}", requestBody);
            return requestBody;
        } catch (JsonProcessingException e) {
            ctx.setPassReason(PassReason.S2S_ERROR);
            logger.error("Error {}: {}", e.toString(), e.getStackTrace());
            return null;
        }
    }

    private RiskResponse validateRiskAPIResponse(CloseableHttpResponse httpResponse, PXContext ctx) {
        StatusLine httpStatus = httpResponse.getStatusLine();

        if (httpStatus.getStatusCode() != 200) {
            return null;
        }

        try {
            String s = IOUtils.toString(httpResponse.getEntity().getContent(), UTF_8);
            if (s.equals("null")) {
                throw new PXException("Risk API returned null JSON");
            }
            logger.debug("Risk API Response: {}", s);
            return JsonUtils.riskResponseReader.readValue(s);
        } catch (Exception e) {
            logger.debug("invalid_response: {}", e);
        }
        return null;
    }

    private RiskResponse makeRiskApiCall(PXContext ctx) throws IOException {
        CloseableHttpResponse httpResponse = null;
        try {
            String requestBody = createRequestBody(ctx);
            if (requestBody == null) {
                return null;
            }

            httpResponse = executeRiskAPICall(requestBody, ctx);
            if (httpResponse == null) {
                return null;
            }

            ctx.setMadeS2SApiCall(true);
            return validateRiskAPIResponse(httpResponse, ctx);
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }

    public boolean verify(PXContext ctx) {
        logger.debug("Evaluating Risk API request, call reason: {}", ctx.getS2SCallReason());
        RiskResponse response = null;
        long startRiskRtt = System.currentTimeMillis();
        long rtt;

        try {
            response = makeRiskApiCall(ctx);
            rtt = System.currentTimeMillis() - startRiskRtt;
            logger.debug("Risk API response returned successfully, risk score: {}, round_trip_time: {}",
                    (response == null) ? "" : response.getScore(), rtt);

            if (!isResponseValid(response)) {
                handleS2SError(ctx, rtt, response, null);
                return true;
            }

            //ctx.setResponsePxhd(response.getPxhd());
            ctx.setRiskScore(response.getScore());
            ctx.setUuid(response.getUuid());
            ctx.setBlockAction(response.getAction());

            if (ctx.getRiskScore() < pxConfiguration.pxBlockingScore()) {
                ctx.setPassReason(PassReason.S2S);
                return true;
            }

            ctx.setBlockReason(BlockReason.SERVER);
            return false;

        } catch (ConnectTimeoutException e) {
            // Timeout handling - report pass reason and proceed with request
            logger.error("Timeout {}: {}", e.toString(), e.getStackTrace());
            ctx.setPassReason(PassReason.S2S_TIMEOUT);
            return true;
        } catch (Exception e) {
            handleS2SError(ctx, System.currentTimeMillis() - startRiskRtt, response, e);
            logger.error("Error {}: {}", e.toString(), e.getStackTrace());
            return true;
        } finally {
            ctx.setRiskRtt(System.currentTimeMillis() - startRiskRtt);
        }
    }

    private boolean isResponseValid(RiskResponse response) {
        return response != null && response.getStatus() == 0;
    }

    private void handleS2SError(PXContext ctx, long rtt, RiskResponse response, Exception exception) {
        ctx.setRiskRtt(rtt);
        ctx.setPassReason(PassReason.S2S_ERROR);

        if (!ctx.getS2SErrorReasonInfo().isErrorSet()) {
            S2SErrorReason errorReason = getS2SErrorReason(ctx, response);
            String errorMessage = getS2SErrorMessage(response, exception);
            ctx.setS2SErrorReasonInfo(new S2SErrorReasonInfo(errorReason, errorMessage));
        }
    }

    private String getS2SErrorMessage(RiskResponse response, Exception exception) {
        if (exception != null) {
            return exception.toString();
        } else if (response != null && !isResponseValid(response)) {
            return response.getMessage();
        }
        int CURRENT_FUNCTION_INDEX = 1;
        return String.format("Error: %s - Response is %s",
                Thread.currentThread().getStackTrace()[CURRENT_FUNCTION_INDEX].toString(),
                response == null ? "null" : response.toString());
    }

    private S2SErrorReason getS2SErrorReason(PXContext ctx, RiskResponse response) {
        if (!ctx.isMadeS2SApiCall()) {
            return S2SErrorReason.UNABLE_TO_SEND_REQUEST;
        } else if (response != null && !isResponseValid(response)) {
            return S2SErrorReason.REQUEST_FAILED_ON_SERVER;
        }
        return S2SErrorReason.UNKNOWN_ERROR;
    }
}
