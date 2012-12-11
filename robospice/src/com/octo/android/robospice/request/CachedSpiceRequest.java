package com.octo.android.robospice.request;

import java.util.UUID;
import java.util.concurrent.Future;

import com.octo.android.robospice.request.listener.RequestCancellationListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;

public class CachedSpiceRequest< RESULT > extends SpiceRequest< RESULT > {

    private Object requestCacheKey = UUID.randomUUID().toString();
    private long cacheDuration;
    private SpiceRequest< RESULT > spiceRequest;
    private boolean isProcessable = true;

    public CachedSpiceRequest( SpiceRequest< RESULT > contentRequest, Object requestCacheKey, long cacheDuration ) {
        super( contentRequest.getResultType() );
        this.requestCacheKey = requestCacheKey;
        this.cacheDuration = cacheDuration;
        this.spiceRequest = contentRequest;
    }

    @Override
    public RESULT loadDataFromNetwork() throws Exception {
        return spiceRequest.loadDataFromNetwork();
    }

    @Override
    public Class< RESULT > getResultType() {
        return spiceRequest.getResultType();
    }

    @Override
    public boolean isAggregatable() {
        return spiceRequest.isAggregatable();
    }

    @Override
    public void setAggregatable( boolean isAggregatable ) {
        spiceRequest.setAggregatable( isAggregatable );
    }

    public boolean isProcessable() {
        return isProcessable;
    }

    public void setProcessable( boolean isProcessable ) {
        this.isProcessable = isProcessable;
    }

    /**
     * Sets the future of this request, used to cancel it.
     * 
     * @param future
     *            the future result of this request.
     */
    @Override
    protected void setFuture( Future< ? > future ) {
        spiceRequest.setFuture( future );
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
    public void setRequestProgressListener( RequestProgressListener requestProgressListener ) {
        spiceRequest.setRequestProgressListener( requestProgressListener );
    }

    @Override
    public void setRequestCancellationListener( RequestCancellationListener requestCancellationListener ) {
        spiceRequest.setRequestCancellationListener( requestCancellationListener );
    }

    @Override
    protected void publishProgress( float progress ) {
        spiceRequest.publishProgress( progress );
    }

    public Object getRequestCacheKey() {
        return requestCacheKey;
    }

    public long getCacheDuration() {
        return cacheDuration;
    }

    public SpiceRequest< RESULT > getContentRequest() {
        return spiceRequest;
    }

    @Override
    public String toString() {
        return "CachedSpiceRequest [requestCacheKey=" + requestCacheKey + ", cacheDuration=" + cacheDuration + ", spiceRequest=" + spiceRequest + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( spiceRequest.getResultType() == null ? 0 : spiceRequest.getResultType().hashCode() );
        result = prime * result + ( requestCacheKey == null ? 0 : requestCacheKey.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        CachedSpiceRequest< ? > other = (CachedSpiceRequest< ? >) obj;
        if ( spiceRequest.getResultType() == null ) {
            if ( other.spiceRequest.getResultType() != null ) {
                return false;
            }
        } else if ( !spiceRequest.getResultType().equals( other.spiceRequest.getResultType() ) ) {
            return false;
        }
        if ( spiceRequest.isAggregatable() != other.spiceRequest.isAggregatable() ) {
            return false;
        }
        if ( requestCacheKey == null ) {
            if ( other.requestCacheKey != null ) {
                return false;
            }
        } else if ( !requestCacheKey.equals( other.requestCacheKey ) ) {
            return false;
        }
        return true;
    }

    /* package private */@Override
    void setStatus( RequestStatus status ) {
        spiceRequest.setStatus( status );
    }

    /* package private */@Override
    RequestProgress getProgress() {
        return spiceRequest.getProgress();
    }

}