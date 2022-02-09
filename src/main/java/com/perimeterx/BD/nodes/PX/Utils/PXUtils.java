package com.perimeterx.BD.nodes.PX.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.perimeterx.BD.nodes.pxVerificationNode.Config;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.message.BasicHeader;
import org.forgerock.guava.common.collect.ListMultimap;

public class PXUtils {
    public static Map<String, String> getHeadersFromRequest(ListMultimap requestHeaders) {
        HashMap<String, String> headers = new HashMap<>();
        requestHeaders.asMap().forEach((k, v) -> {
            headers.put(k.toString(), requestHeaders.get(k).get(0).toString());
        });
        return headers;
    }

    public static RequestConfig getRequestConfig(Config pxConfiguration) {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                .setConnectTimeout(pxConfiguration.pxConnectionTimeout())
                .setConnectionRequestTimeout(pxConfiguration.pxAPITimeout())
                .setSocketTimeout(pxConfiguration.pxAPITimeout());

        return requestConfigBuilder.build();
    }

    public static List<Header> getDefaultHeaders(String authToken) {
        Header contentType = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        Header authorization = new BasicHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        return Arrays.asList(contentType, authorization);
    }

}
