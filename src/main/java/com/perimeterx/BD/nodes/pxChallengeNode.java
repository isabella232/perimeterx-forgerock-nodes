/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2017-2018 ForgeRock AS.
 */

package com.perimeterx.BD.nodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.SingleOutcomeNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;

import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.realms.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;
import com.perimeterx.BD.nodes.PX.PXLogger;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;

/**
 * A node that checks to see if zero-page login headers have specified username
 * and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider = pxChallengeNode.OutcomeProvider.class, configClass = pxChallengeNode.Config.class)
public class pxChallengeNode extends SingleOutcomeNode {

    private static final PXLogger logger = PXLogger.getLogger(pxChallengeNode.class);
    private static final String VALIGN_NEUTRAL_ANCHOR = "HTMLMessageNode_vAlign_Neutral";
    private final Config config;

    /**
     * Configuration for the node.
     */
    public interface Config {
        @Attribute(order = 10)
        default String pxCssRef() {
            return "";
        }

        @Attribute(order = 20)
        default String pxJsRef() {
            return "";
        }
    }

    public static String readFileString(String path) {
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            String data = readAllLines(in);
            in.close();
            return data;
        } catch (NullPointerException | IOException e) {
            logger.error("Can't read file " + path, e);
            return null;
        }
    }

    public static String readAllLines(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        return reader.lines().parallel().collect(Collectors.joining("\n"));
    }

    /**
     * Create the node using Guice injection. Just-in-time bindings can be used to
     * obtain instances of other classes
     * from the plugin.
     *
     * @param config The service config.
     * @param realm  The realm the node is in.
     * @throws NodeProcessException If the configuration was not valid.
     */
    @Inject
    public pxChallengeNode(@Assisted Config config, @Assisted Realm realm) throws NodeProcessException {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        if (context.hasCallbacks()) {
            logger.debug("Entered hasCallbacks. Done.");
            return goToNext().build();
        }
        logger.debug("About to get ScriptTextOutputCallback 7");
        ScriptTextOutputCallback scriptAndSelfSubmitCallback = new ScriptTextOutputCallback(
                createClientSideScriptExecutorFunction(getScript(context)));
        logger.debug("About to get ScriptTextOutputCallback 8");

        return Action.send(Arrays.asList(scriptAndSelfSubmitCallback, new HiddenValueCallback(VALIGN_NEUTRAL_ANCHOR)))
                .build();
    }

    private String getScript(TreeContext context) {

        String refId = context.sharedState.get("refId").asString();

        String appId = context.sharedState.get("appId").asString();
        String jsClientSrc = context.sharedState.get("jsClientSrc").asString();
        boolean firstPartyEnabled = context.sharedState.get("firstPartyEnabled").asBoolean();
        String vid = context.sharedState.get("vid").asString();
        String uuid = context.sharedState.get("uuid").asString();
        String hostUrl = context.sharedState.get("hostUrl").asString();
        String blockScript = context.sharedState.get("blockScript").asString();

        String cssRef = this.config.pxCssRef();
        String jsRef = this.config.pxJsRef();

        String script = readFileString("/js/challenge-script.js");
        script = String.format(script, refId, appId, jsClientSrc, firstPartyEnabled, vid, uuid, hostUrl, blockScript);
        script = script + getCssRefValue(cssRef) + getJSRefValue(jsRef);
        return script;
    }

    private String getCssRefValue(String cssRef) {
        logger.debug("reached getCssRefValue: {}", cssRef);
        if (cssRef != null && !cssRef.isEmpty()) {
            StringBuffer css = new StringBuffer()
                    .append("const style = document.createElement('link');\n")
                    .append("style.setAttribute('type', 'text/css');\n")
                    .append("style.setAttribute('rel', 'stylesheet');\n")
                    .append("style.setAttribute('href', '" + cssRef + "');\n")
                    .append("document.getElementsByTagName('head')[0].appendChild(style);\n");
            return css.toString();
        }
        return "\n";
    }

    private String getJSRefValue(String jsRef) {
        if (jsRef != null && !jsRef.isEmpty()) {
            StringBuffer js = new StringBuffer()
                    .append("const js = document.createElement('script');\n")
                    .append("style.setAttribute('src', '" + jsRef + "');\n")
                    .append("document.getElementsByTagName('head')[0].appendChild(js);\n");
            return js.toString();
        }
        return "\n";
    }

    public static String createClientSideScriptExecutorFunction(String script) {
        return String.format(
                "(function(output) {\n" +
                        "    var autoSubmitDelay = 0,\n" +
                        "        submitted = false;\n" +
                        "    function submit() {\n" +
                        "        if (submitted) {\n" +
                        "            return;\n" +
                        "        }" +
                        "        document.forms[0].submit();\n" +
                        "        submitted = true;\n" +
                        "    }\n" +
                        "    %s\n" + // script
                        "    setTimeout(submit, autoSubmitDelay);\n" +
                        "}) (document.forms[0].elements['nada']);\n",
                script);
    }

}
