package com.octo.android.robospice.retry;

import com.octo.android.robospice.persistence.exception.SpiceException;

/**
 * Defines the behavior of a retry policy.
 * @author SNI
 */
public interface RetryPolicy {

    /**
     * @return the remaining number of retry attempts. When this method returns
     *         0, request is not retried anymore.
     */
    int getRetryCount();

    /** @return the delay to sleep between each retry attempt (in ms). */
    void retry(SpiceException e);

    /** @return the delay to sleep between each retry attempt (in ms). */
    long getDelayBeforeRetry();
}
