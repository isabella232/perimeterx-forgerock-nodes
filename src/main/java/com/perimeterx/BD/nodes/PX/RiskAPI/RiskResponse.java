package com.perimeterx.BD.nodes.PX.RiskAPI;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RiskResponse {
    private String uuid;
    private int status;
    private int score;
    private String action;
    @JsonProperty("action_data")
    private RiskResponse actionData;
    @JsonProperty("data_enrichment")
    private JsonNode dataEnrichment;
    private String pxhd;
    private String message;

    @JsonCreator
    public RiskResponse(@JsonProperty(value = "uuid", required = true) String uuid) {
        this.uuid = uuid;
    }

    public int getScore() {
        return this.score;
    }

    public int getStatus() {
        return this.status;
    }

    public String getPxhd() {
        return this.pxhd;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getAction() {
        return this.action;
    }

    public String getMessage() {
        return this.message;
    }
}