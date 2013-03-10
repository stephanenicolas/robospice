package com.octo.android.robospice.request.listener;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.CachedSpiceRequest;

/**
 * Defines the behavior of a listener that will be notified of request
 * processing by the {@link SpiceService}.
 * @author sni
 */
public interface SpiceServiceServiceListener {
    void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest);
}
