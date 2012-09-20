package com.octo.android.robospice;

import java.util.Collection;
import java.util.Set;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.request.CachedContentRequest;
import com.octo.android.robospice.request.ContentRequest;
import com.octo.android.robospice.request.RequestListener;
import com.octo.android.robospice.request.RequestProcessor;

/**
 * This is an abstract class used to manage the cache and provide web service
 * result to an activity. <br/>
 * 
 * Extends this class to provide a service able to load content from web service
 * or cache (if available and enabled). You will have to implement
 * {@link #createCacheManager(Application)} to configure the
 * {@link CacheManager} used by all requests to persist their results in the
 * cache (and load them from cache if possible).
 * 
 * @author jva
 * @author sni
 */
public abstract class ContentService extends Service {

	private final static String LOG_CAT = "ContentService";

	private static final int DEFAULT_THREAD_COUNT = 1;
	private static final boolean DEFAULT_FAIL_ON_CACHE_ERROR = false;

	// ============================================================================================
	// ATTRIBUTES
	// ============================================================================================
	public ContentServiceBinder mContentServiceBinder;

	/** Responsible for persisting data. */

	private RequestProcessor requestProcessor;

	// ============================================================================================
	// CONSTRUCTOR
	// ============================================================================================
	/**
	 * Basic constructor
	 * 
	 * @param name
	 */
	public ContentService() {
		mContentServiceBinder = new ContentServiceBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		requestProcessor = new RequestProcessor(getApplicationContext(), createCacheManager(getApplication()), getThreadCount());
		requestProcessor.setFailOnCacheError(DEFAULT_FAIL_ON_CACHE_ERROR);

		Log.d(LOG_CAT, "Content Service instance created.");
	}

	// ============================================================================================
	// DELEGATE METHODS (delegation is used to ease tests)
	// ============================================================================================

	public abstract CacheManager createCacheManager(Application application);

	public int getThreadCount() {
		return DEFAULT_THREAD_COUNT;
	}

	public void addRequest(final CachedContentRequest<?> request, Set<RequestListener<?>> listRequestListener) {
		requestProcessor.addRequest(request, listRequestListener);
	}

	public boolean removeDataFromCache(Class<?> clazz, Object cacheKey) {
		return requestProcessor.removeDataFromCache(clazz, cacheKey);
	}

	public void removeAllDataFromCache(Class<?> clazz) {
		requestProcessor.removeAllDataFromCache(clazz);
	}

	public void removeAllDataFromCache() {
		requestProcessor.removeAllDataFromCache();
	}

	public boolean isFailOnCacheError() {
		return requestProcessor.isFailOnCacheError();
	}

	public void setFailOnCacheError(boolean failOnCacheError) {
		requestProcessor.setFailOnCacheError(failOnCacheError);
	}

	public void dontNotifyRequestListenersForRequest(ContentRequest<?> request, Collection<RequestListener<?>> listRequestListener) {
		requestProcessor.dontNotifyRequestListenersForRequest(request, listRequestListener);
	}

	// ============================================================================================
	// SERVICE METHODS
	// ============================================================================================

	@Override
	public IBinder onBind(Intent intent) {
		return mContentServiceBinder;
	}

	public class ContentServiceBinder extends Binder {
		public ContentService getContentService() {
			return ContentService.this;
		}
	}

	public void dumpState() {
		Log.v(LOG_CAT, requestProcessor.toString());
	}

	public void addContentServiceListener(ContentServiceListener contentServiceListener) {
		requestProcessor.addContentServiceListener(contentServiceListener);
	}

	public void removeContentServiceListener(ContentServiceListener contentServiceListener) {
		requestProcessor.removeContentServiceListener(contentServiceListener);
	}
}