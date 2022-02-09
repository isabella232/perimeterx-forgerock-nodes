package com.perimeterx.BD.nodes.PX.Validators;

import com.google.common.base.Strings;
import com.perimeterx.BD.nodes.pxVerificationNode.Config;
import com.perimeterx.BD.nodes.PX.PXContext;
import com.perimeterx.BD.nodes.PX.PXLogger;
import com.perimeterx.BD.nodes.PX.ActivitiesData.BlockReason;
import com.perimeterx.BD.nodes.PX.ActivitiesData.PassReason;
import com.perimeterx.BD.nodes.PX.ActivitiesData.VidSource;
import com.perimeterx.BD.nodes.PX.Cookie.AbstractPXCookie;
import com.perimeterx.BD.nodes.PX.Cookie.CookieSelector;
import com.perimeterx.BD.nodes.PX.Exceptions.PXException;
import com.perimeterx.BD.nodes.PX.Utils.StringUtils;


public class PXCookieValidator implements PXValidator {
    private static final PXLogger logger = PXLogger.getLogger(PXCookieValidator.class);

    private Config pxConfiguration;

    public PXCookieValidator(Config pxConfiguration) {
        this.pxConfiguration = pxConfiguration;
    }

    public boolean verify(PXContext ctx) {
        AbstractPXCookie pxCookie = null;
        if (Strings.isNullOrEmpty(ctx.getPxCookie())) {
            logger.debug("Cookie is missing");
            ctx.setS2SCallReason("no_cookie");
            return false;
        }

        try {
            String mobileError;
            if (ctx.isMobileToken()) {
                PXCookieOriginalTokenValidator mobileVerifier = new PXCookieOriginalTokenValidator(pxConfiguration);
                mobileError = mobileVerifier.getMobileError(ctx);
                mobileVerifier.verify(ctx);
                if (!StringUtils.isEmpty(mobileError)) {
                    ctx.setS2SCallReason("mobile_error_" + mobileError);
                    return false;
                }
            }
            pxCookie = CookieSelector.selectFromTokens(ctx, pxConfiguration);
            if (isBadPXCookie(ctx, pxCookie) || pxCookie == null) {
                return false;
            }

            ctx.setPxCookieRaw(pxCookie.getCookieOrig());
            // ctx.setCookieVersion(pxCookie.getCookieVersion());
            ctx.setRiskCookie(pxCookie);
            ctx.setVid(pxCookie.getVID());
            ctx.setVidSource(VidSource.RISK_COOKIE);
            ctx.setUuid(pxCookie.getUUID());
            ctx.setRiskScore(pxCookie.getScore());
            ctx.setBlockAction(pxCookie.getBlockAction());
            ctx.setCookieHmac(pxCookie.getHmac());

            if (pxCookie.isExpired()) {
                logger.debug("Cookie TTL is expired, value: {}, age: {}", pxCookie.getPxCookie(),
                        System.currentTimeMillis() - pxCookie.getTimestamp());
                ctx.setS2SCallReason("cookie_expired");
                return false;
            }

            if (pxCookie.isHighScore()) {
                ctx.setBlockReason(BlockReason.COOKIE);
                return true;
            }

            if (!pxCookie.isSecured()) {
                ctx.setS2SCallReason("cookie_validation_failed");
                return false;
            }

            if (ctx.isSensitiveRoute()) {
                logger.debug("Sensitive route match, sending Risk API. path: {}", ctx.getFullUrl());
                ctx.setS2SCallReason("sensitive_route");
                return false;
            }
            ctx.setPassReason(PassReason.COOKIE);
            ctx.setS2SCallReason("none");
            logger.debug("Cookie evaluation ended successfully, risk score: {}", ctx.getRiskScore());
            return true;

        } catch (PXException e) {

        }

        return false;
    }

    private boolean isBadPXCookie(PXContext ctx, AbstractPXCookie pxCookie) {
        if (StringUtils.isEmpty(ctx.getS2SCallReason()) && pxCookie == null) {
            ctx.setS2SCallReason("no_cookie");
        }
        return "cookie_decryption_failed".equals(ctx.getS2SCallReason()) || "no_cookie".equals(ctx.getS2SCallReason());
    }
}

