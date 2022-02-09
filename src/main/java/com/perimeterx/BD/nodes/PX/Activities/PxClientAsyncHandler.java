package com.perimeterx.BD.nodes.PX.Activities;

import com.perimeterx.BD.nodes.PX.PXLogger;

import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

public class PxClientAsyncHandler implements FutureCallback<HttpResponse> {

    private static final PXLogger logger = PXLogger.getLogger(PxClientAsyncHandler.class);

    @Override
    public void completed(HttpResponse httpResponse) {
        logger.debug("Response completed {}", httpResponse != null ? httpResponse.getEntity() : "");
    }

    @Override
    public void failed(Exception e) {
        logger.error("Response failed {}", e.getMessage());
    }

    @Override
    public void cancelled() {
        logger.debug("Response was canceled");
    }
}
