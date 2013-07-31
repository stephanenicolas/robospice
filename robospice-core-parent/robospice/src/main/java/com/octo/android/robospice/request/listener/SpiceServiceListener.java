package com.octo.android.robospice.request.listener;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.CachedSpiceRequest;

/**
 * Defines the behavior of a listener that will be notified of request
 * processing by the {@link SpiceService}.
 * @author sni
 */
public interface SpiceServiceListener {
    void onRequestSucceeded(CachedSpiceRequest<?> request, Thread thread);

    void onRequestFailed(CachedSpiceRequest<?> request, Thread thread);

    void onRequestCancelled(CachedSpiceRequest<?> request, Thread thread);

    void onRequestProgressUpdated(CachedSpiceRequest<?> request, Thread thread);

    void onRequestAdded(CachedSpiceRequest<?> request, Thread thread);

    void onRequestNotFound(CachedSpiceRequest<?> request, Thread thread);

    void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest);

    void onServiceStopped();
}
