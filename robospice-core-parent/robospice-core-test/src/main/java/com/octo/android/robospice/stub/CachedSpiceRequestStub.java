package com.octo.android.robospice.stub;

import com.octo.android.robospice.request.CachedSpiceRequest;

/**
 * A {@link CachedSpiceRequest} that is state-testable. Wraps a
 * {@link SpiceRequestStub}.
 * @author sni
 * @param <T>
 *            the type of the result of the request.
 */
public class CachedSpiceRequestStub<T> extends CachedSpiceRequest<T> {

    /**
     * Builds a {@link CachedSpiceRequestStub}.
     * @param contentRequest
     *            the wrapped {@link SpiceRequestStub}.
     * @param requestCacheKey
     *            the cachekey that identifies this
     *            {@link CachedSpiceRequestStub}
     * @param cacheDuration
     *            the expiry delay of potential cache content. If content is
     *            expired, data is loaded from network.
     */
    public CachedSpiceRequestStub(SpiceRequestStub<T> contentRequest, Object requestCacheKey, long cacheDuration) {
        super(contentRequest, requestCacheKey, cacheDuration);
    }

    /**
     * @see {@link SpiceRequestStub#isLoadDataFromNetworkCalled()}.
     * @return
     */
    public boolean isLoadDataFromNetworkCalled() {
        return ((SpiceRequestStub<?>) getSpiceRequest()).isLoadDataFromNetworkCalled();
    }

    /**
     * @see {@link SpiceRequestStub#awaitForLoadDataFromNetworkIsCalled(long)}.
     * @param millisecond
     * @throws InterruptedException
     */
    public void await(long millisecond) throws InterruptedException {
        ((SpiceRequestStub<?>) getSpiceRequest()).awaitForLoadDataFromNetworkIsCalled(millisecond);
    }
}
