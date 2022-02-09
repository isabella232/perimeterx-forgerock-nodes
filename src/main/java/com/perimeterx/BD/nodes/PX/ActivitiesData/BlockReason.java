package com.perimeterx.BD.nodes.PX.ActivitiesData;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BlockReason {

    NONE("none"), SERVER("s2s_high_score"), COOKIE("cookie_high_score");

    private String value;

    BlockReason(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }
}