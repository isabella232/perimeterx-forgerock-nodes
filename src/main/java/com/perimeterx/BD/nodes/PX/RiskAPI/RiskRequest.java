package com.perimeterx.BD.nodes.PX.RiskAPI;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.perimeterx.BD.nodes.PX.PXContext;


public class RiskRequest {

    @JsonProperty("request")
    public Request request;
    @JsonProperty("vid")
    public String vid;
    @JsonProperty("additional")
    public Additional additional;
    @JsonProperty("firstParty")
    public boolean firstParty;
    @JsonProperty("pxhd")
    public String pxhd;

    public static RiskRequest fromContext(PXContext context) {
        RiskRequest riskRequest = new RiskRequest();
        riskRequest.request = Request.fromContext(context);
        riskRequest.vid = context.getVid();
        riskRequest.pxhd = context.getPxhd();
        riskRequest.additional = Additional.fromContext(context);

        if (riskRequest.pxhd != null && "no_cookie".equals(riskRequest.additional.callReason)) {
            riskRequest.additional.callReason = "no_cookie_w_vid";
        }
        return riskRequest;
    }
}
