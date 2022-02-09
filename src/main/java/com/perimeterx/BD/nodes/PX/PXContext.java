package com.perimeterx.BD.nodes.PX;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.perimeterx.BD.nodes.pxVerificationNode.Config;
import com.perimeterx.BD.nodes.PX.ActivitiesData.BlockAction;
import com.perimeterx.BD.nodes.PX.ActivitiesData.BlockReason;
import com.perimeterx.BD.nodes.PX.ActivitiesData.PassReason;
import com.perimeterx.BD.nodes.PX.ActivitiesData.VidSource;
import com.perimeterx.BD.nodes.PX.Cookie.AbstractPXCookie;
import com.perimeterx.BD.nodes.PX.Cookie.PXRawCookie;
import com.perimeterx.BD.nodes.PX.RiskAPI.S2SErrorReasonInfo;
import com.perimeterx.BD.nodes.PX.Utils.PXUtils;

import org.forgerock.openam.auth.node.api.ExternalRequestContext;;

public class PXContext {
    private Config pxConfiguration;
    private Map<String, String> headers;
    private boolean isMobileToken;
    private String cookieOrigin = PXConstants.COOKIE_HEADER_NAME;
    private Set<PXRawCookie> cookies = new HashSet<PXRawCookie>();
    private PXRawCookie originalToken;
    private PXLogger logger;
    private Set<String> requestCookieNames;
    private String vid;
    private VidSource vidSource = VidSource.NONE;
    private String userAgent;
    private String fullUrl;
    private String httpMethod;
    private String httpVersion = "1.1";
    private BlockReason blockReason = BlockReason.NONE;
    private PassReason passReason = PassReason.NONE;
    private String s2sCallReason;
    private boolean madeS2SApiCall;
    private long riskRtt;
    private int riskScore;
    private String ip;
    private String riskCookie;
    private String pxCookieRaw;
    // private CustomParameters customParameters;
    private String originalUuid;
    private String originalTokenError;
    private String originalTokenCookie;
    private String decodedOriginalToken;
    private String cookieHmac;
    private String pxhd;
    private String uuid;
    private BlockAction blockAction;
    private boolean sensitiveRoute;
    private S2SErrorReasonInfo s2sErrorReasonInfo;

    public PXContext(final ExternalRequestContext req, Config pxConfiguration, PXLogger logger) {
        this.pxConfiguration = pxConfiguration;
        this.headers = PXUtils.getHeadersFromRequest(req.headers);
        this.logger = logger;

        parseCookies(req);
        this.userAgent = req.headers.containsKey("User-Agent") ? req.headers.get("User-Agent").get(0) : "";
        this.fullUrl = req.serverUrl;
        this.blockReason = BlockReason.NONE;
        this.passReason = PassReason.NONE;
        this.s2sCallReason = "";
        this.madeS2SApiCall = false;
        this.riskRtt = 0;
        this.httpMethod = "POST";
        // this.httpVersion = req.servletRequest.getProtcol();
        this.riskScore = 0;
        this.ip = req.clientIp;
        this.s2sErrorReasonInfo = new S2SErrorReasonInfo();

        this.sensitiveRoute = checkSensitiveRoute(pxConfiguration.pxSensitiveRoutes(), req.serverUrl);

    }

    private PXRawCookie parseToken(String token) {
        PXRawCookie rawCookieData = null;
        if (token.isEmpty()) {
            return null;
        }

        String[] splitCookie = token.split(":\\s?", 2);
        if (splitCookie.length == 2) {
            rawCookieData = new PXRawCookie("_px" + splitCookie[0], splitCookie[1]);
        } else if (splitCookie.length == 1) {
            rawCookieData = new PXRawCookie("UNDEFINED", splitCookie[0]);
        }

        return rawCookieData;
    }

    private void setVidCookie(Map<String, String> cookies) {
        if (cookies.containsKey("_pxvid")) {
            this.vid = cookies.get("_pxvid");
            this.vidSource = VidSource.VID_COOKIE;
        }
        if (cookies.containsKey("_pxhd")) {
            try {
                this.pxhd = URLDecoder.decode(cookies.get("_pxhd"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("Failed while decoding the pxhd value");
            }
        }
    }

    private void parseCookies(final ExternalRequestContext req) {
        if (req.headers.containsKey(PXConstants.MOBILE_SDK_AUTHORIZATION_HEADER)) {
            this.logger.debug("Mobile SDK token detected");
            this.isMobileToken = true;
            this.cookieOrigin = PXConstants.HEADER_ORIGIN;
            String authCookieHeader = req.headers.get(PXConstants.MOBILE_SDK_AUTHORIZATION_HEADER).get(0);
            String originalTokenHeader = req.headers.containsKey(PXConstants.MOBILE_SDK_ORIGINAL_TOKEN_HEADER)
                    ? req.headers.get(PXConstants.MOBILE_SDK_ORIGINAL_TOKEN_HEADER).get(0)
                    : "";
            this.originalToken = parseToken(originalTokenHeader);
            this.cookies.add(parseToken(authCookieHeader));
        } else {
            this.requestCookieNames = req.cookies.keySet();
            setVidCookie(req.cookies);
            req.cookies.forEach((k, v) -> {
                if (k.matches("^_px.+")) {
                    this.cookies.add(new PXRawCookie(k, v));
                }
            });
        }
    }

    public void setBlockAction(String blockAction) {
        switch (blockAction) {
            case "c":
                this.blockAction = BlockAction.CAPTCHA;
                break;
            case PXConstants.BLOCK_ACTION_CAPTCHA:
                this.blockAction = BlockAction.BLOCK;
                break;
            case PXConstants.BLOCK_ACTION_RATE:
                this.blockAction = BlockAction.RATE;
                break;
            default:
                this.blockAction = BlockAction.CAPTCHA;
                break;
        }
    }

    public BlockAction getBlockAction() {
        return this.blockAction;
    }

    private boolean checkSensitiveRoute(Set<String> sensitiveRoutes, String uri) {
        for (String sensitiveRoutePrefix : sensitiveRoutes) {
            if (uri.contains(sensitiveRoutePrefix)) {
                return true;
            }
        }
        return false;
    }

    public Boolean isBlocking() {
        return this.pxConfiguration.pxModuleMode() == 1;
    }

    public int getRiskScore() {
        return this.riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getPxCookie() {
        for (PXRawCookie cookie : this.cookies) {
            if (cookie.getCookieVersion() == "_px3" || cookie.getCookieVersion() == "UNDEFINED") {
                return cookie.getPayload();
            }
        }

        return null;
    }

    public void setRiskCookie(AbstractPXCookie riskCookie) {
        this.riskCookie = riskCookie.getDecodedCookie().toString();
    }

    public void setS2SCallReason(String s2sCallReason) {
        this.s2sCallReason = s2sCallReason;
    }

    public String getS2SCallReason() {
        return this.s2sCallReason;
    }

    public String getIP() {
        return this.ip;
    }

    public String getFullUrl() {
        return this.fullUrl;
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getRiskCookie() {
        return this.riskCookie;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public String getHttpVersion() {
        return this.httpVersion;
    }

    public String getPxCookieRaw() {
        return this.pxCookieRaw;
    }

    public String getCookieOrigin() {
        return this.cookieOrigin;
    }

    // public CustomParameters getCustomParameters() {
    // return this.customParameters;
    // }

    public String getOriginalUuid() {
        return this.originalUuid;
    }

    public String getOriginalTokenError() {
        return this.originalTokenError;
    }

    public String getPxOriginalTokenCookie() {
        return this.originalTokenCookie;
    }

    public String getDecodedOriginalToken() {
        return this.decodedOriginalToken;
    }

    public String getRiskMode() {
        return this.isBlocking() ? "active_blocking" : "monitor";
    }

    public String getCookieHmac() {
        return this.cookieHmac;
    }

    public void setCookieHmac(String hmac) {
        this.cookieHmac = hmac;
    }

    public Set<String> getRequestCookieNames() {
        return this.requestCookieNames;
    }

    public String getVidSource() {
        return this.vidSource.getValue();
    }

    public void setVidSource(VidSource source) {
        this.vidSource = source;
    }

    public String getVid() {
        return this.vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public String getPxhd() {
        return this.pxhd;
    }

    public void setPassReason(PassReason reason) {
        this.passReason = reason;
    }

    public PassReason getPassReason() {
        return this.passReason;
    }

    public void setMadeS2SApiCall(boolean madeS2SApiCall) {
        this.madeS2SApiCall = madeS2SApiCall;
    }

    public boolean isMadeS2SApiCall() {
        return this.madeS2SApiCall;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public boolean isMobileToken() {
        return this.isMobileToken;
    }

    public PXRawCookie getOriginalToken() {
        return this.originalToken;
    }

    public Set<PXRawCookie> getCookies() {
        return this.cookies;
    }

    public void setOriginalTokenError(String error) {
        this.originalTokenError = error;
    }

    public void setPxCookieRaw(String rawCookie) {
        this.pxCookieRaw = rawCookie;
    }

    public void setOriginalTokenCookie(String token) {
        this.originalTokenCookie = token;
    }

    public void setDecodedOriginalToken(String decodedToken) {
        this.decodedOriginalToken = decodedToken;
    }

    // public void setCookieVersion(String version) {
    // this.cookieVersion = version;
    // }

    public void setOriginalUuid(String uuid) {
        this.originalUuid = uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setBlockReason(BlockReason blockReason) {
        this.blockReason = blockReason;
    }

    public boolean isSensitiveRoute() {
        return this.sensitiveRoute;
    }

    public void setRiskRtt(long riskRtt) {
        this.riskRtt = riskRtt;
    }

    public long getRiskRtt() {
        return this.riskRtt;
    }

    // public void setResponsePxhd(String pxhd) {
    // this.responsePxhd = pxhd;
    // }

    public BlockReason getBlockReason() {
        return this.blockReason;
    }

    public S2SErrorReasonInfo getS2SErrorReasonInfo() {
        return this.s2sErrorReasonInfo;
    }

    public void setS2SErrorReasonInfo(S2SErrorReasonInfo info) {
        this.s2sErrorReasonInfo = info;
    }
}
