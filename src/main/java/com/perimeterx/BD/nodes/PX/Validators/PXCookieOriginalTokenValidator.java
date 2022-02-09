package com.perimeterx.BD.nodes.PX.Validators;

import java.util.Set;

import com.perimeterx.BD.nodes.pxVerificationNode.Config;
import com.perimeterx.BD.nodes.PX.PXContext;
import com.perimeterx.BD.nodes.PX.PXLogger;
import com.perimeterx.BD.nodes.PX.Cookie.AbstractPXCookie;
import com.perimeterx.BD.nodes.PX.Cookie.CookieSelector;
import com.perimeterx.BD.nodes.PX.Cookie.PXRawCookie;
import com.perimeterx.BD.nodes.PX.Exceptions.PXCookieDecryptionException;
import com.perimeterx.BD.nodes.PX.Exceptions.PXException;
import com.perimeterx.BD.nodes.PX.Utils.StringUtils;



public class PXCookieOriginalTokenValidator implements PXValidator {
    private static final PXLogger logger = PXLogger.getLogger(PXCookieOriginalTokenValidator.class);

    private Config pxConfiguration;

    public PXCookieOriginalTokenValidator(Config pxConfiguration) {
        this.pxConfiguration = pxConfiguration;
    }

    public boolean verify(PXContext ctx) {
        try {

            AbstractPXCookie originalCookie = CookieSelector.getOriginalToken(ctx, pxConfiguration);
            if (!StringUtils.isEmpty(ctx.getOriginalTokenError()) || originalCookie == null) {
                return false;
            }
            String decodedOriginalCookie = originalCookie.getDecodedCookie().toString();
            ctx.setOriginalTokenCookie(originalCookie.getCookieOrig());
            ctx.setDecodedOriginalToken(decodedOriginalCookie);
            if (ctx.getVid() == null) {
                ctx.setVid(originalCookie.getVID());
            }
            ctx.setPxCookieRaw(originalCookie.getCookieOrig());
            //ctx.setCookieVersion(originalCookie.getCookieVersion());
            ctx.setOriginalUuid(originalCookie.getUUID());

            if (!originalCookie.isSecured()) {
                logger.debug("Original token HMAC validation failed, value: " + decodedOriginalCookie + " user-agent: "
                        + ctx.getUserAgent());
                ctx.setOriginalTokenError("validation_failed");
                return false;
            }
        } catch (PXException | PXCookieDecryptionException e) {
            logger.debug("Received an error while decrypting perimeterx original token:" + e.getMessage());
            ctx.setOriginalTokenError("decryption_failed");
            return false;
        }
        return true;
    }

    private boolean isErrorMobileHeader(String authHeader) {
        return authHeader.matches("^\\d+$") && authHeader.length() == 1;
    }

    public String getMobileError(PXContext ctx) {
        String mobileError = "";
        Set<PXRawCookie> tokensCookie = ctx.getCookies();
        if (!tokensCookie.isEmpty()) {
            PXRawCookie token = tokensCookie.stream().filter(c -> c.getCookieVersion() == "UNDEFINED").findFirst().get();
            if (isErrorMobileHeader(token.getPayload())) {
                mobileError = token.getPayload();
            }
        }
        return mobileError;
    }
}

