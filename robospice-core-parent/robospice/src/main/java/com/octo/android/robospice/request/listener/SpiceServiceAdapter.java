package com.octo.android.robospice.request.listener;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.CachedSpiceRequest;

/**
 * Defines the behavior of a listener that will be notified of request
 * processing by the {@link SpiceService}.
 * @author sni
 */
public class SpiceServiceAdapter implements SpiceServiceListener {
    @Override
    public void onRequestSucceeded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
    }

    @Override
    public void onRequestFailed(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
    }

    @Override
    public void onRequestCancelled(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
    }

    @Override
    public void onRequestProgressUpdated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
    }

    @Override
    public void onRequestAdded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
    }

    @Override
    public void onRequestAggregated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
    }

    @Override
    public void onRequestNotFound(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
    }

    @Override
    public void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {
    }

    @Override
    public void onServiceStopped() {
    }
}
