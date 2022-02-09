package com.perimeterx.BD.nodes.PX.Cookie;

public class CookieData {
    private String pxCookie;
    private String userAgent;
    private String ip;
    private boolean mobileToken;
    private String cookieOrig;
    private String cookieVersion;

    public CookieData(String pxCookie, String userAgent, String ip, boolean mobileToken, String cookieOrig,
            String cookieVersion) {
        this.pxCookie = pxCookie;
        this.userAgent = userAgent;
        this.ip = ip;
        this.mobileToken = mobileToken;
        this.cookieOrig = cookieOrig;
        this.cookieVersion = cookieVersion;
    }

    public String getPxCookie() {
        return this.pxCookie;
    }

    public String getCookieOrig() {
        return this.cookieOrig;
    }

    public boolean isMobileToken() {
        return mobileToken;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public String getIp() {
        return this.ip;
    }

    public String getCookieVersion() {
        return this.cookieVersion;
    }
}
