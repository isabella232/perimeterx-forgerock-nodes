package com.perimeterx.BD.nodes.PX.Cookie;

import com.perimeterx.BD.nodes.pxVerificationNode.Config;

public abstract class PXCookieFactory {
    public static AbstractPXCookie create(Config pxConfiguration, CookieData cookieData) {
        switch (cookieData.getCookieVersion()) {
            case "_px3":
                return new PXCookieV3(pxConfiguration, cookieData);
        }
        return null;
    }
}
