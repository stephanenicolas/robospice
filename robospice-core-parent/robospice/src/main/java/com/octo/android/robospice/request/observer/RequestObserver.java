package com.octo.android.robospice.request.observer;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestProgress;

/**
 * An observer which receives request event updates
 * @author Andrew.Clark
 *
 */
public interface RequestObserver {

    /**
     * Request completed
     * @param request
     * @param result
     */
    <RESULT> void onRequestCompleted(CachedSpiceRequest<RESULT> request, RESULT result);

    /**
     * Request has failed
     * @param request
     * @param Exception
     */
    void onRequestFailed(CachedSpiceRequest<?> request, SpiceException e);

    /**
     * Request has been cancelled
     * @param request
     */
    void onRequestCancelled(CachedSpiceRequest<?> request);

    /**
     * Request progress has been updated
     * @param request
     * @param progress
     */
    void onRequestProgressUpdated(CachedSpiceRequest<?> request,
            RequestProgress progress);

    /**
     * Request has been added
     * @param request
     */
    void onRequestAdded(CachedSpiceRequest<?> request);
}
