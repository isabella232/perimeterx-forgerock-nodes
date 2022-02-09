package com.perimeterx.BD.nodes.PX.Activities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.perimeterx.BD.nodes.PX.PXConstants;
import com.perimeterx.BD.nodes.PX.PXContext;
import com.perimeterx.BD.nodes.PX.ActivitiesData.PassReason;
import com.perimeterx.BD.nodes.PX.RiskAPI.S2SErrorReason;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PageRequestedActivityDetails implements ActivityDetails {
    @JsonProperty("http_method")
    private String httpMethod;
    @JsonProperty("http_version")
    private String httpVersion;
    @JsonProperty("px_cookie")
    private String riskCookie;
    @JsonProperty("pass_reason")
    private PassReason passReason;
    @JsonProperty("s2s_error_reason")
    private S2SErrorReason s2SErrorReason;
    @JsonProperty("s2s_error_message")
    private String s2sErrorMessage;
    @JsonProperty("s2s_error_http_status")
    private int s2sErrorHttpStatus;
    @JsonProperty("s2s_error_http_message")
    private String s2sErrorHttpMessage;
    @JsonProperty("risk_rtt")
    private long riskRtt;
    @JsonProperty("module_version")
    private String moduleVersion;
    @JsonProperty("client_uuid")
    private String clientUuid;
    @JsonProperty("cookie_origin")
    private String cookieOrigin;
    // @JsonUnwrapped
    // private CustomParameters customParameters;

    public PageRequestedActivityDetails(PXContext ctx) {
        this.httpMethod = ctx.getHttpMethod();
        this.httpVersion = ctx.getHttpVersion();
        this.riskCookie = ctx.getRiskCookie();
        this.passReason = ctx.getPassReason();
        this.s2SErrorReason = ctx.getS2SErrorReasonInfo().getReason();
        this.s2sErrorMessage = ctx.getS2SErrorReasonInfo().getMessage();
        this.s2sErrorHttpStatus = ctx.getS2SErrorReasonInfo().getHttpStatus();
        this.s2sErrorHttpMessage = ctx.getS2SErrorReasonInfo().getHttpMessage();
        this.riskRtt = ctx.getRiskRtt();
        this.moduleVersion = PXConstants.SDK_VERSION;
        this.clientUuid = ctx.getUuid();
        this.cookieOrigin = ctx.getCookieOrigin();
        // this.customParameters = ctx.getCustomParameters();
    }
}
