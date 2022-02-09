package com.perimeterx.BD.nodes.PX.Activities;

import java.io.IOException;
import java.nio.charset.Charset;

import com.perimeterx.BD.nodes.pxVerificationNode.Config;
import com.perimeterx.BD.nodes.PX.PXConstants;
import com.perimeterx.BD.nodes.PX.PXContext;
import com.perimeterx.BD.nodes.PX.PXLogger;
import com.perimeterx.BD.nodes.PX.Exceptions.PXException;
import com.perimeterx.BD.nodes.PX.Utils.JsonUtils;
import com.perimeterx.BD.nodes.PX.Utils.PXUtils;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.IOReactorExceptionHandler;

public class ActivityHandler {
    private Config pxConfiguration;
    private CloseableHttpAsyncClient asyncHttpClient;
    private PoolingNHttpClientConnectionManager nHttpConnectionManager;

    private static final PXLogger logger = PXLogger.getLogger(ActivityHandler.class);
    private static final Charset UTF_8 = Charset.forName("utf-8");

    public ActivityHandler(Config configuration) {
        this.pxConfiguration = configuration;
        try {
            initAsyncHttpClient();
        } catch (IOReactorException e) {
            logger.error("Cant create activty handler: {}", e);
        }
    }

    public void handleBlockActivity(PXContext ctx) throws PXException {
        Activity activity = ActivityFactory.createActivity(PXConstants.ACTIVITY_BLOCKED, pxConfiguration.pxAppId(),
                ctx);
        try {
            this.sendActivity(activity);
        } catch (IOException e) {
            throw new PXException(e);
        }
    }

    public void handlePageRequestedActivity(PXContext context) throws PXException {
        Activity activity = ActivityFactory.createActivity(PXConstants.ACTIVITY_PAGE_REQUESTED,
                pxConfiguration.pxAppId(), context);
        try {
            this.sendActivity(activity);
        } catch (IOException e) {
            throw new PXException(e);
        }
    }

    private void sendActivity(Activity activity) throws IOException {
        HttpAsyncRequestProducer producer = null;
        BasicAsyncResponseConsumer basicAsyncResponseConsumer = null;
        try {
            String requestBody = JsonUtils.writer.writeValueAsString(activity);
            logger.debug("Sending activity: {}", requestBody);
            String serverUrl = String.format(PXConstants.SERVER_URL, this.pxConfiguration.pxAppId());
            HttpPost post = new HttpPost(serverUrl + PXConstants.API_ACTIVITIES);

            post.setEntity(new StringEntity(requestBody, UTF_8));
            post.setConfig(PXUtils.getRequestConfig(pxConfiguration));
            post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + pxConfiguration.pxAuthToken().toString());
            producer = HttpAsyncMethods.create(post);
            basicAsyncResponseConsumer = new BasicAsyncResponseConsumer();
            asyncHttpClient.execute(producer, basicAsyncResponseConsumer, new PxClientAsyncHandler());
        } catch (Exception e) {
            logger.debug("Sending activity failed. Error: {}", e.getMessage());
        } finally {
            if (producer != null) {
                producer.close();
            }
            if (basicAsyncResponseConsumer != null) {
                basicAsyncResponseConsumer.close();
            }
        }
    }

    private void initAsyncHttpClient() throws IOReactorException {
        DefaultConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();

        ioReactor.setExceptionHandler(new IOReactorExceptionHandler() {
            @Override
            public boolean handle(IOException ex) {
                logger.error("IO Reactor encountered an IOException, shutting down reactor. {}", ex);
                return false;
            }

            @Override
            public boolean handle(RuntimeException ex) {
                logger.error("IO Reactor encountered a RuntimeException, shutting down reactor. {}", ex);
                return false;
            }
        });

        nHttpConnectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
        CloseableHttpAsyncClient closeableHttpAsyncClient = HttpAsyncClients.custom()
                .setConnectionManager(nHttpConnectionManager).build();
        closeableHttpAsyncClient.start();
        asyncHttpClient = closeableHttpAsyncClient;
    }
}
