package com.perimeterx.BD.nodes.PX.ActivitiesData;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PassReason {
    NONE(null), COOKIE("cookie"), S2S("s2s"), S2S_TIMEOUT("s2s_timeout"), S2S_ERROR("s2s_error");

    public String value;

    PassReason(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }

}
