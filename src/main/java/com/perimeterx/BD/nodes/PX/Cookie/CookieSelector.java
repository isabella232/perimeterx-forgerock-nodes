package com.perimeterx.BD.nodes.PX.Cookie;

import java.util.Set;

import com.perimeterx.BD.nodes.pxVerificationNode.Config;
import com.perimeterx.BD.nodes.PX.PXContext;
import com.perimeterx.BD.nodes.PX.PXLogger;
import com.perimeterx.BD.nodes.PX.Exceptions.PXCookieDecryptionException;
import com.perimeterx.BD.nodes.PX.Utils.StringUtils;


public class CookieSelector {
    private static final PXLogger logger = PXLogger.getLogger(CookieSelector.class);

    public static AbstractPXCookie selectFromTokens(PXContext ctx, Config pxConfiguration) {
        AbstractPXCookie result = null;
        String s2SCallReason = "none";
        Set<PXRawCookie> tokens = ctx.getCookies();
        String cookieRaw = null;
        if (tokens != null) {
            for (PXRawCookie token : tokens) {
                String cookie = token.getPayload();
                String version = token.getCookieVersion();

                cookieRaw = version + "=" + cookie;
                AbstractPXCookie selectedCookie = buildPxCookie(ctx, pxConfiguration, cookie, version);
                s2SCallReason = evaluateCookie(selectedCookie, cookie);
                if (s2SCallReason == "none") {
                    result = selectedCookie;
                    break;
                }

            }
        }

        ctx.setPxCookieRaw(cookieRaw);
        if (!(s2SCallReason == "none" && result == null)) {
            ctx.setS2SCallReason(s2SCallReason);
        }

        return result;
    }

    public static AbstractPXCookie getOriginalToken(PXContext ctx, Config pxConfiguration)
            throws PXCookieDecryptionException {
        AbstractPXCookie result = null;
        String errorMessage = null;
        PXRawCookie token = ctx.getOriginalToken();
        String cookieOrig = null;
        if (token != null) {
            String cookie = token.getPayload();
            String version = token.getCookieVersion();
            cookieOrig = cookie;
            AbstractPXCookie selectedCookie = buildPxCookie(ctx, pxConfiguration, cookie, version);
            errorMessage = evaluateOriginalTokenCookie(selectedCookie);
            if (StringUtils.isEmpty(errorMessage)) {
                result = selectedCookie;
            }
        }

        ctx.setPxCookieRaw(cookieOrig);
        ctx.setOriginalTokenError(errorMessage);
        return result;
    }

    private static String evaluateCookie(AbstractPXCookie selectedCookie, String cookie) {
        String s2SCallReason = "none";
        if (selectedCookie == null && StringUtils.isEmpty(cookie)) {
            logger.debug("Cookie is null");
            s2SCallReason = "no_cookie";
        } else {
            try {
                if (!selectedCookie.deserialize()) {
                    logger.debug("Cookie decryption failed, value: {}", cookie);
                    s2SCallReason = "cookie_decryption_failed";
                }
            } catch (Exception e) {
                logger.debug("Cookie decryption failed with exception, value: {}", cookie, e);
                s2SCallReason = "cookie_decryption_failed";
            }
        }
        return s2SCallReason;
    }

    private static AbstractPXCookie buildPxCookie(PXContext ctx, Config pxConfiguration, String cookie,
            String cookieVersion) {
        CookieData cookieData = new CookieData(cookie, ctx.getUserAgent(), ctx.getIP(), ctx.isMobileToken(), cookie,
                cookieVersion);

        AbstractPXCookie selectedCookie = PXCookieFactory.create(pxConfiguration, cookieData);
        logger.debug("Cookie found, Evaluating");

        return selectedCookie;
    }

    private static String evaluateOriginalTokenCookie(AbstractPXCookie selectedCookie) {
        String error = "";
        if (selectedCookie == null) {
            logger.debug("Original token is null");
            error = "original_token_missing";
        } else {
            try {
                if (!selectedCookie.deserialize()) {
                    logger.debug("Original token decryption failed, value: {}", selectedCookie.getPxCookie());
                    error = "decryption_failed";
                }
            } catch (PXCookieDecryptionException e) {
                logger.debug("Original token decryption failed with exception, value: {}", selectedCookie.getPxCookie(),
                        e);
                error = "decryption_failed";
            }
        }
        return error;
    }
}
