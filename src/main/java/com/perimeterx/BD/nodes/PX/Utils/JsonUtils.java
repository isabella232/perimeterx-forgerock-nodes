package com.perimeterx.BD.nodes.PX.Utils;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.perimeterx.BD.nodes.PX.RiskAPI.RiskResponse;


public final class JsonUtils {

    private final static ObjectMapper mapper = new ObjectMapper();

    public final static ObjectReader riskResponseReader = mapper.readerFor(RiskResponse.class);
    public final static ObjectWriter writer = mapper.writer();

    protected JsonUtils() {
    }

    static void readJsonStringIntoObject(Object object, String content) throws IOException {
        mapper.readerForUpdating(object).readValue(content);
    }
}
