package com.octo.android.robospice.stub;

import com.octo.android.robospice.request.CachedContentRequest;

public class CachedContentRequestStub<T> extends CachedContentRequest<T> {

	public CachedContentRequestStub(ContentRequestStub<T> contentRequest, String requestCacheKey, long cacheDuration) {
		super(contentRequest, requestCacheKey, cacheDuration);
	}

	public boolean isLoadDataFromNetworkCalled() {
		return ((ContentRequestStub<?>) getContentRequest()).isLoadDataFromNetworkCalled();
	}

	public void await(long millisecond) throws InterruptedException {
		((ContentRequestStub<?>) getContentRequest()).await(millisecond);
	}

}