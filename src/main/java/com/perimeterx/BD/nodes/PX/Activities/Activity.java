package com.perimeterx.BD.nodes.PX.Activities;

import java.util.Map;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.perimeterx.BD.nodes.PX.PXConstants;
import com.perimeterx.BD.nodes.PX.PXContext;


public class Activity {

    @JsonProperty("type")
    private String type;
    @JsonProperty("headers")
    private Map<String, String> headers;
    private long timestamp;
    @JsonProperty("socket_ip")
    private String socketIp;
    @JsonProperty("url")
    private String url;
    @JsonProperty("px_app_id")
    private String pxAppId;
    @JsonProperty("vid")
    private String vid;
    @JsonProperty("details")
    private ActivityDetails details;
    @JsonProperty("pxhd")
    private String pxhd;

    public Activity(String activityType, String appId, PXContext ctx, ActivityDetails details) {
        this.type = activityType;
        this.headers = ctx.getHeaders();
        this.timestamp = System.currentTimeMillis();
        this.socketIp = ctx.getIP();
        this.pxAppId = appId;
        this.url = ctx.getFullUrl();
        this.vid = ctx.getVid();
        this.details = details;
        if ((activityType.equals(PXConstants.ACTIVITY_PAGE_REQUESTED)
                || activityType.equals(PXConstants.ACTIVITY_BLOCKED)) && ctx.getPxhd() != null) {
            this.pxhd = ctx.getPxhd();
        }
    }
}
