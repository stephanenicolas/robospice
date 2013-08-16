package com.octo.android.robospice.request.listener;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.CachedSpiceRequest;

/**
 * Defines the behavior of a listener that will be notified of request
 * processing by the {@link SpiceService}.
 * @author sni
 */
public interface SpiceServiceListener {
    void onRequestSucceeded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

    void onRequestFailed(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

    void onRequestCancelled(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

    void onRequestProgressUpdated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

    void onRequestAdded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

    void onRequestNotFound(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

    void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest);

    void onServiceStopped();
    
    // ----------------------------------
    //  INNER CLASS
    // ----------------------------------
    
    public static class RequestProcessingContext {
        private Thread executionThread;
        private RequestProgress requestProgress;

        public void setExecutionThread(Thread executionThread) {
            this.executionThread = executionThread;
        }

        public Thread getExecutionThread() {
            return executionThread;
        }

        public void setRequestProgress(RequestProgress requestProgress) {
            this.requestProgress = requestProgress;
        }

        public RequestProgress getRequestProgress() {
            return requestProgress;
        }
    }
}
