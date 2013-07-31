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
    public void onRequestSucceeded(CachedSpiceRequest<?> request, Thread thread) {
    }

    @Override
    public void onRequestFailed(CachedSpiceRequest<?> request, Thread thread) {
    }

    @Override
    public void onRequestCancelled(CachedSpiceRequest<?> request, Thread thread) {
    }

    @Override
    public void onRequestProgressUpdated(CachedSpiceRequest<?> request, Thread thread) {
    }

    @Override
    public void onRequestAdded(CachedSpiceRequest<?> request, Thread thread) {
    }

    @Override
    public void onRequestNotFound(CachedSpiceRequest<?> request, Thread thread) {
    }

    @Override
    public void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest) {
    }

    @Override
    public void onServiceStopped() {
    }
}
