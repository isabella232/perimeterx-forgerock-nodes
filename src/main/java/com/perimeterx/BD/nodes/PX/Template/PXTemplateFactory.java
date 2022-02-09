package com.perimeterx.BD.nodes.PX.Template;

import com.perimeterx.BD.nodes.pxVerificationNode.Config;
import com.perimeterx.BD.nodes.PX.PXConstants;
import com.perimeterx.BD.nodes.PX.PXContext;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.TreeContext;


public class PXTemplateFactory {
    public static JsonValue getProps(TreeContext context, PXContext pxContext, Config config) {
        JsonValue copyState = context.sharedState.copy();
        copyState.put("appId", config.pxAppId());
        copyState.put("refId", pxContext.getUuid());
        copyState.put("vid", pxContext.getVid());
        copyState.put("uuid", pxContext.getUuid());


        String urlVid = pxContext.getVid() != null ? pxContext.getVid() : "";
        
        String blockScript = "//" + PXConstants.CAPTCHA_HOST + "/" + config.pxAppId() + "/captcha.js?a=" + pxContext.getBlockAction().getCode() + "&u=" + pxContext.getUuid() + "&v=" + urlVid + "&m=" + (pxContext.isMobileToken() ? "1" : "0");
        String jsClientSrc = "//" + PXConstants.CLIENT_HOST + "/" + config.pxAppId() + "/main.min.js";
        String hostUrl = String.format(PXConstants.COLLECTOR_URL, config.pxAppId());

        copyState.put("hostUrl", hostUrl);
        copyState.put("blockScript", blockScript);
        copyState.put("jsClientSrc", jsClientSrc);
        copyState.put("firstPartyEnabled", false);
        
        return copyState;
    }
}
