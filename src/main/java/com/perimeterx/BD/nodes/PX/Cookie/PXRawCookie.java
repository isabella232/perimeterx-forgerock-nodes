package com.perimeterx.BD.nodes.PX.Cookie;

public class PXRawCookie {
    String cookieVersion;

    String payload;

    public PXRawCookie(String cookieVersion, String payload) {
        this.cookieVersion = cookieVersion;
        this.payload = payload;
    }

    public String getPayload() {
        return this.payload;
    }

    public String getCookieVersion() {
        return this.cookieVersion;
    }
}
