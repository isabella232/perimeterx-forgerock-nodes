package com.perimeterx.BD.nodes.PX;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PXConstants {
    private static Properties prop;

    static {
        prop = new Properties();
        InputStream propStream = PXConstants.class.getResourceAsStream("metadata.properties");
        if (propStream != null) {
            try {
                prop.load(propStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("There is an error, could not found the metadata.properties file");
        }
    }

    public static final String MOBILE_SDK_AUTHORIZATION_HEADER = "x-px-authorization";
    public static final String MOBILE_SDK_ORIGINAL_TOKEN_HEADER = "x-px-original-token";
    public static final String HEADER_ORIGIN = "header";
    public static final String COOKIE_HEADER_NAME = "cookie";
    public static final String SDK_VERSION = "ForgeRock Module v0.1.0";
    public static final String SERVER_URL = "https://sapi-%s.perimeterx.net";
    public static final String COLLECTOR_URL = "https://collector-%s.perimeterx.net";
    public static final String API_RISK = "/api/v3/risk";
    public static final String API_ACTIVITIES = "/api/v1/collector/s2s";
    public static final String CAPTCHA_ACTION_CAPTCHA = "c";
    public static final String BLOCK_ACTION_CAPTCHA = "b";
    public static final String BLOCK_ACTION_RATE = "r";
    public final static String ACTIVITY_BLOCKED = "block";
    public final static String ACTIVITY_PAGE_REQUESTED = "page_requested";
    public static final String CLIENT_HOST = "client.perimeterx.net";
    public static final String CAPTCHA_HOST = "captcha.px-cdn.net";
}
