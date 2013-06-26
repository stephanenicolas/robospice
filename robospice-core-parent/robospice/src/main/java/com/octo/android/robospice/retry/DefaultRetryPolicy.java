package com.octo.android.robospice.retry;

import com.octo.android.robospice.persistence.exception.SpiceException;

/**
 * Default {@link RetryPolicy} implementation. Proposes an exponential back off
 * algorithm. When {@link #getRetryCount()} returns 0, the request is not
 * retried anymore and will fail. Between each retry attempt, the request
 * processor will sleep for {@link #getDelayBeforeRetry()} milliseconds.
 * @author SNI
 */
public class DefaultRetryPolicy implements RetryPolicy {

    /** The default number of retry attempts. */
    public static final int DEFAULT_RETRY_COUNT = 3;

    /** The default delay before retry a request (in ms). */
    public static final long DEFAULT_DELAY_BEFORE_RETRY = 2500;

    /** The default backoff multiplier. */
    public static final float DEFAULT_BACKOFF_MULT = 1f;

    /** The number of retry attempts. */
    private int retryCount = DEFAULT_RETRY_COUNT;

    /**
     * The delay to wait before next retry attempt. Will be multiplied by
     * {@link #backOffMultiplier} between every retry attempt.
     */
    private long delayBeforeRetry = DEFAULT_DELAY_BEFORE_RETRY;

    /**
     * The backoff multiplier. Will be multiplied by {@link #delayBeforeRetry}
     * between every retry attempt.
     */
    private float backOffMultiplier = DEFAULT_BACKOFF_MULT;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------
    public DefaultRetryPolicy(int retryCount, long delayBeforeRetry, float backOffMultiplier) {
        this.retryCount = retryCount;
        this.delayBeforeRetry = delayBeforeRetry;
        this.backOffMultiplier = backOffMultiplier;
    }

    public DefaultRetryPolicy() {
        this(DEFAULT_RETRY_COUNT, DEFAULT_DELAY_BEFORE_RETRY, DEFAULT_BACKOFF_MULT);
    }

    // ----------------------------------
    // PUBLIC API
    // ----------------------------------

    @Override
    public int getRetryCount() {
        return retryCount;
    }

    @Override
    public void retry(SpiceException e) {
        retryCount--;
        delayBeforeRetry = (long) (delayBeforeRetry * backOffMultiplier);
    }

    @Override
    public long getDelayBeforeRetry() {
        return delayBeforeRetry;
    }

}
