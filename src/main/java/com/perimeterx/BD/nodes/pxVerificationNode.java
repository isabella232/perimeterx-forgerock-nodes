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

import java.util.Set;

import javax.inject.Inject;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.AbstractDecisionNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.realms.Realm;
import org.forgerock.openam.sm.annotations.adapters.Password;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.assistedinject.Assisted;
import com.perimeterx.BD.nodes.PX.PXContext;
import com.perimeterx.BD.nodes.PX.PXLogger;
import com.perimeterx.BD.nodes.PX.Activities.ActivityHandler;
import com.perimeterx.BD.nodes.PX.Exceptions.PXException;
import com.perimeterx.BD.nodes.PX.Template.PXTemplateFactory;
import com.perimeterx.BD.nodes.PX.Validators.PXCookieValidator;
import com.perimeterx.BD.nodes.PX.Validators.PXS2SValidator;
import com.perimeterx.BD.nodes.PX.Verification.VerificationHandler;

/**
 * A node that checks to see if zero-page login headers have specified username
 * and whether that username is in a group
 * permitted to use zero-page login headers.
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class, configClass = pxVerificationNode.Config.class)
public class pxVerificationNode extends AbstractDecisionNode {
    private static final PXLogger logger = PXLogger.getLogger(pxVerificationNode.class);
    private final Config config;
    private final Realm realm;
    private PXCookieValidator cookieValidator;
    private PXS2SValidator s2sValidator;

    /**
     * Configuration for the node.
     */
    public interface Config {
        /**
         * The PerimeterX application id.
         */
        @Attribute(order = 10)
        String pxAppId();

        /**
         * The PerimeterX authentication token.
         */

        @Attribute(order = 11)
        @Password
        char[] pxAuthToken();

        /**
         * The PerimeterX cookie secret value.
         */
        @Attribute(order = 12)
        @Password
        char[] pxCookieSecret();

        /**
         * A list of user-agents that the module should always allow.
         */
        @Attribute(order = 13)
        Set<String> pxWhitelistUAs();

        /**
         * A list of IP addresses that the module should always allow.
         */
        @Attribute(order = 14)
        Set<String> pxWhitelistIPs();

        /**
         * The score of which PerimeterX module will block the request.
         */
        @Attribute(order = 15)
        default int pxBlockingScore() {
            return 100;
        }

        /**
         * The PerimeterX module mode flag. 0 = monitor, 1 = blocking
         */
        @Attribute(order = 16)
        default int pxModuleMode() {
            return 0;
        }

        /**
         * Connection timeout to PerimeterX servers
         */
        @Attribute(order = 17)
        default int pxConnectionTimeout() {
            return 1000;
        }

        /**
         * API Connection timeout to PerimeterX servers
         */
        @Attribute(order = 18)
        default int pxAPITimeout() {
            return 1000;
        }

        /**
         * A list of sensitive routes for the module.
         */
        @Attribute(order = 19)
        Set<String> pxSensitiveRoutes();

        /**
         * The bypass monitor header name
         */
        @Attribute(order = 20)
        default String pxBypassMonitorHeader() {
            return "";
        }
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
    public pxVerificationNode(@Assisted Config config, @Assisted Realm realm) throws NodeProcessException {
        this.config = config;
        this.realm = realm;
        this.cookieValidator = new PXCookieValidator(config);
        this.s2sValidator = new PXS2SValidator(config);
    }

    /**
     * Entry point for the PerimeterX node.
     */
    @Override
    public Action process(TreeContext context) throws NodeProcessException {

        /**
         * Initialization of the required handler
         * Activity Handler deals with the async activities sent to PerimeterX servers
         * after the request validation.
         * Verification Handler verifies the incoming request.
         */
        ActivityHandler activityHandler = new ActivityHandler(config);
        VerificationHandler verificationHandler = new VerificationHandler(config, activityHandler);

        String userAgent = context.request.headers.containsKey("User-Agent")
                ? context.request.headers.get("User-Agent").get(0)
                : "";
        String ipAddress = context.request.clientIp;

        logger.debug("Starting request verification");

        /**
         * Check current user-agent against the user-agent allow-list provided in the
         * node configuration.
         */
        if (config.pxWhitelistUAs().contains(userAgent)) {
            logger.debug("Skipping verification for filtered user agent " + userAgent);
            return goTo(true).build();
        }

        /**
         * Check current IP address against the IP address allow-list provided in the
         * node configuration.
         */
        if (config.pxWhitelistIPs().contains(ipAddress)) {
            logger.debug("Skipping verification for filtered ip address " + ipAddress);
            return goTo(true).build();
        }

        /**
         * Creation of the request context.
         * The Context contains information about the request such as its headers,
         * cookies, method etc.
         */
        PXContext ctx = new PXContext(context.request, config, logger);
        logger.debug("Request context created successfully");

        /**
         * Verify the PerimeterX cookies and update the context with the score and
         * action.
         * If verification fails - make a syncronous call to PerimeterX cloud for the
         * score and action.
         */
        verifyCookie(ctx);

        try {
            /**
             * Handles the verification result.
             * If verificationResult is `false` - block the request
             * If verificationResult is `true` - send the request downstream.
             */
            boolean verificationResult = verificationHandler.handleVerification(ctx);
            if (!verificationResult) {
                JsonValue stateWithProps = PXTemplateFactory.getProps(context, ctx, config);
                return goTo(false).replaceSharedState(stateWithProps).build();
            }
            return goTo(true).build();

        } catch (PXException e) {
            /**
             * In case of an exection - PerimeterX will fail-open and send the request
             * downstream.
             */
            logger.error("Error in verification result: {}", e);
            return goTo(true).build();
        }
    }

    private void verifyCookie(PXContext ctx) {
        if (cookieValidator.verify(ctx)) {
            return;
        }
        if (!s2sValidator.verify(ctx)) {
            logger.debug("Risk score is higher or equal to blocking score. score: {}, blocking score: {}.",
                    ctx.getRiskScore(), config.pxBlockingScore());
        }
    }
}
