package com.octo.android.robospice.request;

import java.util.concurrent.Future;

import com.octo.android.robospice.request.listener.RequestCancellationListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;
import com.octo.android.robospice.retry.RetryPolicy;

/**
 * Decorates {@link SpiceRequest} and provides additional information used by
 * RoboSpice. There are very few chances that you should use this class directly, though you can.
 * But generally speaking, 
 * {@link com.octo.android.robospice.SpiceManager#execute(SpiceRequest, Object, long, com.octo.android.robospice.request.listener.RequestListener)}
 * is considered to be more clear.
 * @author SNI
 * @param <RESULT>
 */
public class CachedSpiceRequest<RESULT> extends SpiceRequest<RESULT> {

    private Object requestCacheKey;
    private final long cacheDuration;
    private final SpiceRequest<RESULT> spiceRequest;
    private boolean isProcessable = true;
    private boolean isAcceptingDirtyCache;
    private boolean isOffline;

    public CachedSpiceRequest(final SpiceRequest<RESULT> spiceRequest, final Object requestCacheKey, final long cacheDuration) {
        super(spiceRequest.getResultType());
        this.requestCacheKey = requestCacheKey;
        this.cacheDuration = cacheDuration;
        this.spiceRequest = spiceRequest;
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        return spiceRequest.getRetryPolicy();
    }

    @Override
    public void setRetryPolicy(RetryPolicy retryPolicy) {
        spiceRequest.setRetryPolicy(retryPolicy);
    }

    @Override
    public RESULT loadDataFromNetwork() throws Exception {
        return spiceRequest.loadDataFromNetwork();
    }

    @Override
    public Class<RESULT> getResultType() {
        return spiceRequest.getResultType();
    }

    @Override
    public boolean isAggregatable() {
        return spiceRequest.isAggregatable();
    }

    @Override
    public void setAggregatable(final boolean isAggregatable) {
        spiceRequest.setAggregatable(isAggregatable);
    }

    public boolean isProcessable() {
        return isProcessable;
    }

    public void setProcessable(final boolean isProcessable) {
        this.isProcessable = isProcessable;
    }

    /**
     * Sets the future of this request, used to cancel it.
     * @param future
     *            the future result of this request.
     */
    @Override
    protected void setFuture(final Future<?> future) {
        spiceRequest.setFuture(future);
    }

    @Override
    public void cancel() {
        spiceRequest.cancel();
    }

    @Override
    public boolean isCancelled() {
        return spiceRequest.isCancelled();
    }

    @Override
    protected void setRequestProgressListener(final RequestProgressListener requestProgressListener) {
        spiceRequest.setRequestProgressListener(requestProgressListener);
    }

    @Override
    public void setRequestCancellationListener(final RequestCancellationListener requestCancellationListener) {
        spiceRequest.setRequestCancellationListener(requestCancellationListener);
    }

    @Override
    protected void publishProgress(final float progress) {
        spiceRequest.publishProgress(progress);
    }

    public Object getRequestCacheKey() {
        return requestCacheKey;
    }

    public long getCacheDuration() {
        return cacheDuration;
    }

    public SpiceRequest<RESULT> getSpiceRequest() {
        return spiceRequest;
    }
    
    /* package private */@Override
    void setStatus(final RequestStatus status) {
        spiceRequest.setStatus(status);
    }

    /* package private */@Override
    RequestProgress getProgress() {
        return spiceRequest.getProgress();
    }

    @Override
    public void setPriority(int priority) {
        spiceRequest.setPriority(priority);
    }

    @Override
    public int getPriority() {
        return spiceRequest.getPriority();
    }

    public boolean isAcceptingDirtyCache() {
        return isAcceptingDirtyCache;
    }

    public void setAcceptingDirtyCache(boolean isAcceptingDirtyCache) {
        this.isAcceptingDirtyCache = isAcceptingDirtyCache;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean isOffline) {
        this.isOffline = isOffline;
    }

    @Override
    public String toString() {
        return "CachedSpiceRequest [requestCacheKey=" + requestCacheKey + ", cacheDuration=" + cacheDuration + ", spiceRequest=" + spiceRequest + "]";
    }

    // --------------------------------------------------------------------
    //  COMPARISON METHODS : THEY DEFINE AGGREGATION OF SPICE REQUESTS.
    // --------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (spiceRequest.getResultType() == null ? 0 : spiceRequest.getResultType().hashCode());
        result = prime * result + (requestCacheKey == null ? 0 : requestCacheKey.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        // http://stackoverflow.com/q/596462/693752
        if (!(obj instanceof CachedSpiceRequest)) {
            return false;
        }
        final CachedSpiceRequest<?> other = (CachedSpiceRequest<?>) obj;
        if (spiceRequest.getResultType() == null && other.spiceRequest.getResultType() != null) {
            return false;
        }
        if (!spiceRequest.getResultType().equals(other.spiceRequest.getResultType())) {
            return false;
        }
        if (spiceRequest.isAggregatable() != other.spiceRequest.isAggregatable()) {
            return false;
        }
        if (requestCacheKey == null || !requestCacheKey.equals(other.requestCacheKey)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(SpiceRequest<RESULT> other) {
        if (this == other) {
            return 0;
        }
        if (other == null) {
            return -1;
        }

        return this.spiceRequest.compareTo(other);
    }

}
