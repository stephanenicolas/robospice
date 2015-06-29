package com.octo.android.robospice.request;

import java.lang.reflect.Modifier;
import java.util.concurrent.Future;

import android.content.Context;

import com.octo.android.robospice.request.listener.RequestCancellationListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;
import com.octo.android.robospice.retry.DefaultRetryPolicy;
import com.octo.android.robospice.retry.RetryPolicy;

/**
 * Base class for writing requests in RoboSpice. Simply override
 * {@link #loadDataFromNetwork()} to define the network operation of a request.
 * REST Requests are easier using the Request class proposed by the
 * spring-android module of RoboSpice.
 * @author sni
 * @param <RESULT>
 */
public abstract class SpiceRequest<RESULT> implements Comparable<SpiceRequest<RESULT>> {

    public static final int PRIORITY_HIGH = 0;
    public static final int PRIORITY_NORMAL = 50;
    public static final int PRIORITY_LOW = 100;

    private final Class<RESULT> resultType;
    private boolean isCanceled = false;
    private Future<?> future;
    private RequestProgressListener requestProgressListener;
    private boolean isAggregatable = true;
    private int priority = PRIORITY_NORMAL;
    private RequestProgress progress = new RequestProgress(RequestStatus.PENDING);
    private RequestCancellationListener requestCancellationListener;

    private RetryPolicy retryPolicy = new DefaultRetryPolicy();

    public SpiceRequest(final Class<RESULT> clazz) {
        checkInnerClassDeclarationToPreventMemoryLeak();
        this.resultType = clazz;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    /**
     * Set a {@link RetryPolicy} that will be responsible to coordinate retry
     * attempts by the RequestProcessor. Can be null (no retry). Retry policy only
     * applies when network is on. If network is down, requests are "tried" only once.
     * @param retryPolicy the new retry policy
     * @see {@link com.octo.android.robospice.networkstate.NetworkStateChecker.NetworkStateChecker}
     */
    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    /**
     * Sets the priority of the request. Use priority constants or a non-negative
     * integer. Will have no effect on a request after it starts being executed.
     * @param priority
     *            the priority of request. Defaults to {@link #PRIORITY_NORMAL}.
     * @see #PRIORITY_LOW
     * @see #PRIORITY_NORMAL
     * @see #PRIORITY_HIGH
     */
    public void setPriority(int priority) {
        if (priority < 0) {
            throw new IllegalArgumentException("Priority must be non-negative.");
        }
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    private void checkInnerClassDeclarationToPreventMemoryLeak() {
        // thanx to Cyril Mottier for this contribution
        // prevent devs from creating memory leaks by using inner
        // classes of contexts
        if (getClass().isMemberClass() && Context.class.isAssignableFrom(getClass().getDeclaringClass()) && !Modifier.isStatic(getClass().getModifiers())) {
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

    @Override
    public int compareTo(SpiceRequest<RESULT> other) {
        if (this == other) {
            return 0;
        }

        return this.priority - other.priority;
    }

}
