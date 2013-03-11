package com.octo.android.robospice.request;

import java.lang.reflect.Modifier;
import java.util.concurrent.Future;

import android.content.Context;

import com.octo.android.robospice.request.listener.RequestCancellationListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;

/**
 * Base class for writing requests in RoboSpice. Simply override {@link #loadDataFromNetwork()} to define the network operation of a request. REST Requests are easier using the Request class proposed
 * by the spring-android module of RoboSpice.
 * @author sni
 * @param <RESULT>
 */
public abstract class SpiceRequest<RESULT> {

    private final Class<RESULT> resultType;
    private boolean isCanceled = false;
    private Future<?> future;
    private RequestProgressListener requestProgressListener;
    private boolean isAggregatable = true;
    private RequestProgress progress = new RequestProgress(RequestStatus.PENDING);
    private RequestCancellationListener requestCancellationListener;

    public SpiceRequest(final Class<RESULT> clazz) {
        checkInnerClassDeclarationToPreventMemoryLeak();
        this.resultType = clazz;
    }

    private void checkInnerClassDeclarationToPreventMemoryLeak() {
        // thanx to Cyril Mottier for this contribution
        // prevent devs from creating memory leaks by using inner
        // classes of contexts
        if (getClass().isMemberClass() && Context.class.isAssignableFrom(getClass().getDeclaringClass())
            && !Modifier.isStatic(getClass().getModifiers())) {
            throw new IllegalArgumentException("Requests must be either non-inner classes or a static inner member class of Context : " + getClass());
        }
    }

    public abstract RESULT loadDataFromNetwork() throws Exception;

    public Class<RESULT> getResultType() {
        return resultType;
    }

    public void cancel() {
        this.isCanceled = true;

        if (future != null) {
            future.cancel(true);
        }

        if (this.requestCancellationListener != null) {
            this.requestCancellationListener.onRequestCancelled();
        }
    }

    /* package private */void setStatus(final RequestStatus status) {
        this.progress = new RequestProgress(status);
        publishProgress();
    }

    /* package private */RequestProgress getProgress() {
        return progress;
    }

    public boolean isCancelled() {
        return this.isCanceled;
    }

    public boolean isAggregatable() {
        return isAggregatable;
    }

    public void setAggregatable(final boolean isAggregatable) {
        this.isAggregatable = isAggregatable;
    }

    protected void setFuture(final Future<?> future) {
        this.future = future;
    }

    protected void setRequestProgressListener(final RequestProgressListener requestProgressListener) {
        this.requestProgressListener = requestProgressListener;
    }

    protected void publishProgress() {
        if (requestProgressListener != null) {
            // TODO SIDE_EFFECT ?
            requestProgressListener.onRequestProgressUpdate(progress);
        }
    }

    protected void publishProgress(final float progress) {
        this.progress.setStatus(RequestStatus.LOADING_FROM_NETWORK);
        this.progress.setProgress(progress);
        publishProgress();
    }

    public void setRequestCancellationListener(final RequestCancellationListener requestCancellationListener) {
        this.requestCancellationListener = requestCancellationListener;
    }

}
