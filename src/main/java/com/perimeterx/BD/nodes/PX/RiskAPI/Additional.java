package com.perimeterx.BD.nodes.PX.RiskAPI;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.perimeterx.BD.nodes.PX.PXConstants;
import com.perimeterx.BD.nodes.PX.PXContext;


public class Additional {

    @JsonProperty("px_cookie")
    public String pxCookie;
    @JsonProperty("http_method")
    public String httpMethod;
    @JsonProperty("http_version")
    public String httpVersion;
    @JsonProperty("s2s_call_reason")
    public String callReason;
    @JsonProperty("px_cookie_raw")
    public String pxCookieRaw;
    @JsonProperty("cookie_origin")
    public String pxCookieOrigin;
    @JsonProperty("module_version")
    public final String moduleVersion = PXConstants.SDK_VERSION;
    @JsonProperty("original_uuid")
    public String originalUuid;
    @JsonProperty("original_token_error")
    public String originalTokenError;
    @JsonProperty("original_token")
    public String originalToken;
    @JsonProperty("decoded_original_token")
    public String decodedOriginalToken;
    @JsonProperty("risk_mode")
    public String riskMode;
    @JsonProperty("px_cookie_hmac")
    public String pxCookieHmac;
    // @JsonUnwrapped
    // public CustomParameters customParameters;
    @JsonProperty("request_cookie_names")
    public String[] requestCookieNames;
    @JsonProperty("enforcer_vid_source")
    public String vidSource;

    public static Additional fromContext(PXContext ctx) {
        Additional additional = new Additional();
        additional.pxCookie = ctx.getRiskCookie();
        additional.httpMethod = ctx.getHttpMethod();
        additional.httpVersion = ctx.getHttpVersion();
        additional.callReason = ctx.getS2SCallReason();
        additional.pxCookieRaw = ctx.getPxCookieRaw();
        additional.pxCookieOrigin = ctx.getCookieOrigin();
        // additional.customParameters = ctx.getCustomParameters();
        additional.originalUuid = ctx.getOriginalUuid();
        additional.originalTokenError = ctx.getOriginalTokenError();
        additional.originalToken = ctx.getPxOriginalTokenCookie();
        additional.decodedOriginalToken = ctx.getDecodedOriginalToken();
        additional.riskMode = ctx.getRiskMode();
        additional.pxCookieHmac = ctx.getCookieHmac();
        additional.requestCookieNames = ctx.getRequestCookieNames().toArray(String[]::new);
        additional.vidSource = ctx.getVidSource();
        return additional;
    }
}
