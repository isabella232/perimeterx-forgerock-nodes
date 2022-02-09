package com.perimeterx.BD.nodes.PX.Cookie;

import com.fasterxml.jackson.databind.JsonNode;
import com.perimeterx.BD.nodes.pxVerificationNode.Config;
import com.perimeterx.BD.nodes.PX.Exceptions.PXException;


public class PXCookieV3 extends AbstractPXCookie {
    private String hmac;

    public PXCookieV3(Config pxConfiguration, CookieData cookieData) {
        super(pxConfiguration, cookieData);
        String[] splicedCookie = getPxCookie().split(":", 2);
        if (splicedCookie.length > 1) {
            this.pxCookie = splicedCookie[1];
            this.hmac = splicedCookie[0];
        }
    }

    @Override
    public String getHmac() {
        return this.hmac;
    }

    @Override
    public String getBlockAction() {
        return this.decodedCookie.get("a").asText();
    }

    @Override
    public int getScore() {
        return decodedCookie.get("s").asInt();
    }

    @Override
    public boolean isCookieFormatValid(JsonNode decodedCookie) {
        return decodedCookie.has("t") && decodedCookie.has("s") && decodedCookie.has("u") && decodedCookie.has("v")
                && decodedCookie.has("a");
    }

    @Override
    public boolean isSecured() throws PXException {
        String hmacString = new StringBuilder().append(this.getPxCookie()).append(userAgent).toString();
        return this.isHmacValid(hmacString, this.getHmac());
    }
}
