package com.octo.android.robospice.request;

import java.util.concurrent.Future;

public class CachedContentRequest<RESULT> extends ContentRequest<RESULT> {

	private String requestCacheKey;
	private long cacheDuration;
	private ContentRequest<RESULT> contentRequest;

	public CachedContentRequest(ContentRequest<RESULT> contentRequest, String requestCacheKey, long cacheDuration) {
		super(contentRequest.getResultType());
		this.requestCacheKey = requestCacheKey;
		this.cacheDuration = cacheDuration;
		this.contentRequest = contentRequest;
	}

	@Override
	public RESULT loadDataFromNetwork() throws Exception {
		return contentRequest.loadDataFromNetwork();
	}

	@Override
	public Class<RESULT> getResultType() {
		return contentRequest.getResultType();
	}

	/**
	 * Sets the future of this request, used to cancel it.
	 * 
	 * @param future
	 *            the future result of this request.
	 */
	@Override
	protected void setFuture(Future<?> future) {
		contentRequest.setFuture(future);
	}

	@Override
	public void cancel() {
		contentRequest.cancel();
	}

	@Override
	public boolean isCancelled() {
		return contentRequest.isCancelled();
	}

	public String getRequestCacheKey() {
		return requestCacheKey;
	}

	public long getCacheDuration() {
		return cacheDuration;
	}

	public ContentRequest<RESULT> getContentRequest() {
		return contentRequest;
	}

	@Override
	public String toString() {
		return "CachedContentRequest [requestCacheKey=" + requestCacheKey + ", cacheDuration=" + cacheDuration + ", contentRequest=" + contentRequest
				+ "]";
	}

}