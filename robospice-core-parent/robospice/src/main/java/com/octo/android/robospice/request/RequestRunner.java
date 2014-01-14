package com.octo.android.robospice.request;

/**
 * Processes requests. 
 * @author SNI
 * @author Andrew Clark
 * @author Isuski
 */
public interface RequestRunner {

    /**
     * Executes a request asynchronously.
     * @param request
     *            the request to execute.
     */
    void executeRequest(CachedSpiceRequest<?> request);

    /** @return whether or not the runner has to fail on cache errors. */
    boolean isFailOnCacheError();

    /**
     * @param whether
     *            or not the runner has to fail on cache errors.
     */
    void setFailOnCacheError(boolean failOnCacheError);

    /**
     * Will be called to notify implementations that no further request
     * processing is needed anymore. Use this method to clean up resources.
     */
    void shouldStop();
}
