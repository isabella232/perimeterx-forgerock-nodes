package com.perimeterx.BD.nodes.PX.Activities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.perimeterx.BD.nodes.PX.PXConstants;
import com.perimeterx.BD.nodes.PX.PXContext;
import com.perimeterx.BD.nodes.PX.ActivitiesData.BlockReason;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class BlockActivityDetails implements ActivityDetails {
    @JsonProperty("block_score")
    private int blockScore;
    @JsonProperty("block_reason")
    private BlockReason blockReason;
    @JsonProperty("block_uuid")
    private String blockUuid;
    @JsonProperty("http_method")
    private String httpMethod;
    @JsonProperty("http_version")
    private String httpVersion;
    @JsonProperty("px_cookie")
    private String pxCookie;
    @JsonProperty("risk_rtt")
    private long riskRtt;
    @JsonProperty("module_version")
    private String moduleVersion;
    @JsonProperty("cookie_origin")
    private String cookieOrigin;
    @JsonProperty("simulated_block")
    private Boolean simulatedBlock;
    // @JsonUnwrapped
    // private CustomParameters customParameters;
    @JsonProperty("block_action")
    private String blockAction;

    public BlockActivityDetails(PXContext ctx) {
        this.blockScore = ctx.getRiskScore();
        this.blockReason = ctx.getBlockReason();
        this.blockUuid = ctx.getUuid();
        this.httpMethod = ctx.getHttpMethod();
        this.httpVersion = ctx.getHttpVersion();
        this.pxCookie = ctx.getRiskCookie();
        this.riskRtt = ctx.getRiskRtt();
        this.cookieOrigin = ctx.getCookieOrigin();
        this.moduleVersion = PXConstants.SDK_VERSION;
        this.simulatedBlock = !ctx.isBlocking();
        // this.customParameters = ctx.getCustomParameters();
        if (ctx.getBlockAction() != null) {
            this.blockAction = ctx.getBlockAction().getCode();
        }
    }
}
