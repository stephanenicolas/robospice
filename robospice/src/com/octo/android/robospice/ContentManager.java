package com.octo.android.robospice;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.webkit.CacheManager;

import com.octo.android.robospice.ContentService.ContentServiceBinder;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedContentRequest;
import com.octo.android.robospice.request.ContentRequest;
import com.octo.android.robospice.request.RequestListener;

/**
 * The instances of this class allow to acces the {@link ContentService}. <br/>
 * 
 * They are tied to activities and obtain a local binding to the
 * {@link ContentService}. When binding occurs, the {@link ContentManager} will
 * send commadnds to the {@link ContentService}, to execute requests, clear
 * cache, prevent listeners from beeing called and so on.
 * 
 * Basically, all features of the {@link ContentService} are accessible from the
 * {@link ContentManager}. It acts as an asynchronous proxy : every call to a
 * {@link ContentService} method is asynchronous and will occur as soon as
 * possible when the {@link ContentManager} successfully binds to the service.
 * 
 * @author jva
 * @author sni
 * @author mwa
 * 
 */

/*
 * Note to maintainers : This class is quite complex and requires background
 * knowledge in multi-threading & local service binding in android.
 */
public class ContentManager implements Runnable {

	private static final String LOG_TAG = ContentManager.class.getSimpleName();

	/** The class of the {@link ContentService} to bind to. */
	private Class<? extends ContentService> contentServiceClass;

	/** A reference on the {@link ContentService} obtained by local binding. */
	private ContentService contentService;
	/** {@link ContentService} binder. */
	private ContentServiceConnection contentServiceConnection = new ContentServiceConnection();

	/** The context used to bind to the service from. */
	private Context context;

	/** Wether or not {@link ContentManager} is started. */
	private boolean isStopped = true;

	/** The queue of requests to be sent to the service. */
	private BlockingQueue<CachedContentRequest<?>> requestQueue = new LinkedBlockingQueue<CachedContentRequest<?>>();

	/**
	 * The list of all requests that have not yet been passed to the service.
	 * All iterations must be synchronized.
	 */
	private Map<CachedContentRequest<?>, Set<RequestListener<?>>> mapRequestToLaunchToRequestListener = Collections
			.synchronizedMap(new IdentityHashMap<CachedContentRequest<?>, Set<RequestListener<?>>>());
	/**
	 * The list of all requests that have already been passed to the service.
	 * All iterations must be synchronized.
	 */
	private Map<CachedContentRequest<?>, Set<RequestListener<?>>> mapPendingRequestToRequestListener = Collections
			.synchronizedMap(new IdentityHashMap<CachedContentRequest<?>, Set<RequestListener<?>>>());

	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	/**
	 * Lock used to synchronize binding to / unbing from the
	 * {@link ContentService}.
	 */
	private ReentrantLock lockAcquireService = new ReentrantLock();
	/** A monitor to ensure service is bound before accessing it. */
	private Condition conditionServiceAcquired = lockAcquireService.newCondition();

	/**
	 * Lock used to synchronize transmission of requests to the
	 * {@link ContentService}.
	 */
	private ReentrantLock lockSendRequestsToService = new ReentrantLock();

	/** Thread running runnable code. */
	private Thread runner;

	/** Reacts to service processing of requests. */
	private RequestRemoverContentServiceListener removerContentServiceListener = new RequestRemoverContentServiceListener();

	/** Whether or not we are unbinding (to prevent unbinding twice. */
	public boolean isUnbinding = false;

	// ============================================================================================
	// THREAD BEHAVIOR
	// ============================================================================================

	/**
	 * Creates a {@link ContentManager}. Typically this occurs in the
	 * construction of an Activity or Fragment.
	 * 
	 * This method will check if the service to bind to has been properly
	 * declared in AndroidManifest.
	 * 
	 * @param contentServiceClass
	 *            the service class to bind to.
	 */
	public ContentManager(Class<? extends ContentService> contentServiceClass) {
		this.contentServiceClass = contentServiceClass;
	}

	/**
	 * Start the {@link ContentManager}. It will bind asynchronously to the
	 * {@link ContentService}.
	 * 
	 * @param context
	 *            a context that will be used to bind to the service. Typically,
	 *            the Activity or Fragment that needs to interact with the
	 *            {@link ContentService}.
	 */
	public synchronized void start(Context context) {
		this.context = context;
		if (runner != null) {
			throw new IllegalStateException("Already started.");
		} else {
			checkServiceIsProperlyDeclaredInAndroidManifest(context);
			Log.d(LOG_TAG, "Content manager started.");
			runner = new Thread(this);
			isStopped = false;
			runner.start();
		}
	}

	/**
	 * Method is synchronized with {@link #start(Context)}.
	 * 
	 * @return whether or not the {@link ContentManager} is started.
	 */
	public synchronized boolean isStarted() {
		return !isStopped;
	}

	public void run() {
		bindToService(context);

		try {
			waitForServiceToBeBound();
			while (!isStopped) {
				try {
					lockSendRequestsToService.lock();
					if (!requestQueue.isEmpty()) {
						CachedContentRequest<?> restRequest;
						restRequest = requestQueue.take();
						Set<RequestListener<?>> listRequestListener = mapRequestToLaunchToRequestListener.get(restRequest);
						mapRequestToLaunchToRequestListener.remove(restRequest);
						mapPendingRequestToRequestListener.put(restRequest, listRequestListener);
						Log.d(LOG_TAG, "Sending request to service : " + restRequest.getClass().getSimpleName());
						contentService.addRequest(restRequest, listRequestListener);
					}
				} finally {
					lockSendRequestsToService.unlock();
				}
			}
		} catch (InterruptedException e) {
			Log.e(LOG_TAG, "Interrupted while waiting for acquiring service.");
		} finally {
			unbindFromService(context);
		}
	}

	/**
	 * Stops the {@link ContentManager}. It will unbind from
	 * {@link ContentService}. All request listeners that had been registered to
	 * listen to {@link ContentRequest}s sent from this {@link ContentManager}
	 * will be unregistered. None of them will be notified with the results of
	 * their {@link ContentRequest}s.
	 * 
	 * Unbinding will occur asynchronously.
	 */
	public synchronized void shouldStop() {
		if (this.runner == null) {
			throw new IllegalStateException("Not started yet");
		}
		Log.d(LOG_TAG, "Content manager stopping.");
		dontNotifyAnyRequestListenersInternal();
		unbindFromService(context);
		this.isStopped = true;
		this.runner = null;
		Log.d(LOG_TAG, "Content manager stopped.");
	}

	/**
	 * This is mostly a testing method.
	 * 
	 * Stops the {@link ContentManager}. It will unbind from
	 * {@link ContentService}. All request listeners that had been registered to
	 * listen to {@link ContentRequest}s sent from this {@link ContentManager}
	 * will be unregistered. None of them will be notified with the results of
	 * their {@link ContentRequest}s.
	 * 
	 * Unbinding will occur syncrhonously : the method returns when all events
	 * have been unregistered and when main processing thread stops.
	 * 
	 */
	public synchronized void shouldStopAndJoin(long timeOut) throws InterruptedException {
		if (this.runner == null) {
			throw new IllegalStateException("Not started yet");
		}

		Log.d(LOG_TAG, "Content manager stopping. Joining");
		dontNotifyAnyRequestListenersInternal();
		unbindFromService(context);
		this.isStopped = true;

		this.runner.join(timeOut);
		this.runner = null;
		Log.d(LOG_TAG, "Content manager stopped.");
	}

	// ============================================================================================
	// PUBLIC EXPOSED METHODS : requests executions
	// ============================================================================================

	/**
	 * Execute a request, without using cache.
	 * 
	 * @param request
	 *            the request to execute.
	 * @param requestListener
	 *            the listener to notify when the request will finish.
	 */
	public <T> void execute(ContentRequest<T> request, RequestListener<T> requestListener) {
		CachedContentRequest<T> cachedContentRequest = new CachedContentRequest<T>(request, null, DurationInMillis.ALWAYS);
		execute(cachedContentRequest, requestListener);
	}

	/**
	 * Execute a request, put the result in cache with key
	 * <i>requestCacheKey</i> during <i>cacheDuration</i> millisecond and
	 * register listeners to notify when request is finished.
	 * 
	 * @param request
	 *            the request to execute
	 * @param requestCacheKey
	 *            the key used to store and retrieve the result of the request
	 *            in the cache
	 * @param cacheDuration
	 *            the time in millisecond to keep cache alive (see
	 *            {@link DurationInMillis})
	 * @param requestListener
	 *            the listener to notify when the request will finish
	 */
	public <T> void execute(ContentRequest<T> request, String requestCacheKey, long cacheDuration, RequestListener<T> requestListener) {
		CachedContentRequest<T> cachedContentRequest = new CachedContentRequest<T>(request, requestCacheKey, cacheDuration);
		execute(cachedContentRequest, requestListener);
	}

	/**
	 * Execute a request, put the result in cache and register listeners to
	 * notify when request is finished.
	 * 
	 * @param request
	 *            the request to execute. {@link CachedContentRequest} is a
	 *            wrapper of {@link ContentRequest} that contains cache key and
	 *            cache duration
	 * @param requestListener
	 *            the listener to notify when the request will finish
	 */
	public <T> void execute(CachedContentRequest<T> cachedContentRequest, RequestListener<T> requestListener) {
		addRequestListenerToListOfRequestListeners(cachedContentRequest, requestListener);
		this.requestQueue.add(cachedContentRequest);
	}

	// ============================================================================================
	// PUBLIC EXPOSED METHODS : unregister listeners
	// ============================================================================================

	/**
	 * Disable request listeners notifications for a specific request.<br/>
	 * None of the listeners associated to this request will be called when
	 * request will finish.<br/>
	 * 
	 * This method will ask (asynchronously) to the {@link ContentService} to
	 * remove listeners if requests have already been sent to the
	 * {@link ContentService} if the request has already been sent to the
	 * service. Otherwise, it will just remove listeners before passing the
	 * request to the {@link ContentService}.
	 * 
	 * Calling this method doesn't prevent request from beeing executed (and put
	 * in cache) but will remove request's listeners notification.
	 * 
	 * @param request
	 *            Request for which listeners are to unregistered.
	 */
	public void dontNotifyRequestListenersForRequest(final ContentRequest<?> request) {
		executorService.execute(new Runnable() {
			public void run() {
				dontNotifyRequestListenersForRequestInternal(request);
			}
		});
	}

	/**
	 * Internal method to remove requests. If request has not been passed to the
	 * {@link ContentService} yet, all listeners are unregistered locally before
	 * beeing passed to the service. Otherwise, it will asynchronously ask to
	 * the {@link ContentService} to remove the listeners of the request beeing
	 * processed.
	 * 
	 * @param request
	 *            Request for which listeners are to unregistered.
	 */
	protected void dontNotifyRequestListenersForRequestInternal(final ContentRequest<?> request) {
		try {
			lockSendRequestsToService.lock();

			boolean requestNotPassedToServiceYet = removeListenersOfCachedRequestToLaunch(request);
			Log.v(LOG_TAG, "Removed from requests to launch list : " + requestNotPassedToServiceYet);

			// if the request was already passed to service, bind to service and
			// unregister listeners.
			if (!requestNotPassedToServiceYet) {
				removeListenersOfPendingCachedRequest(request);
				Log.v(LOG_TAG, "Removed from pending requests list");
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lockSendRequestsToService.unlock();
		}
	}

	/**
	 * Remove all listeners of a request that has not yet been passed to the
	 * {@link ContentService}.
	 * 
	 * @param request
	 *            the request for which listeners must be unregistered.
	 * @return a boolean indicating if the request could be found inside the
	 *         list of requests to be launched. If false, the request was
	 *         already passed to the service.
	 */
	private boolean removeListenersOfCachedRequestToLaunch(final ContentRequest<?> request) {
		synchronized (mapRequestToLaunchToRequestListener) {
			for (CachedContentRequest<?> cachedContentRequest : mapRequestToLaunchToRequestListener.keySet()) {
				if (match(cachedContentRequest, request)) {
					final Set<RequestListener<?>> setRequestListeners = mapRequestToLaunchToRequestListener.get(cachedContentRequest);
					setRequestListeners.clear();
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Remove all listeners of a request that may have already been passed to
	 * the {@link ContentService}. If the request has already been passed to the
	 * {@link ContentService}, the method will bind to the service and ask it to
	 * remove listeners.
	 * 
	 * @param request
	 *            the request for which listeners must be unregistered.
	 */
	private void removeListenersOfPendingCachedRequest(final ContentRequest<?> request) throws InterruptedException {
		synchronized (mapPendingRequestToRequestListener) {
			for (CachedContentRequest<?> cachedContentRequest : mapPendingRequestToRequestListener.keySet()) {
				if (match(cachedContentRequest, request)) {
					waitForServiceToBeBound();
					final Set<RequestListener<?>> setRequestListeners = mapPendingRequestToRequestListener.get(cachedContentRequest);
					contentService.dontNotifyRequestListenersForRequest(cachedContentRequest.getContentRequest(), setRequestListeners);
					mapPendingRequestToRequestListener.remove(cachedContentRequest);
					break;
				}
			}
		}
	}

	/**
	 * Disable request listeners notifications for all requests. <br/>
	 * Should be called in {@link Activity#onStop}
	 */
	public void dontNotifyAnyRequestListeners() {
		executorService.execute(new Runnable() {
			public void run() {
				dontNotifyAnyRequestListenersInternal();
			}
		});
	}

	/**
	 * Remove all listeners of requests.
	 * 
	 * All requests that have not been yet passed to the service will see their
	 * of listeners cleaned. For all requests that have been passed to the
	 * service, we ask the service to remove their listeners.
	 */
	protected void dontNotifyAnyRequestListenersInternal() {
		try {
			lockSendRequestsToService.lock();

			mapRequestToLaunchToRequestListener.clear();
			Log.v(LOG_TAG, "Cleared listeners of all requests to launch");

			removeListenersOfAllPendingCachedRequests();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lockSendRequestsToService.unlock();
		}
	}

	/**
	 * Asynchronously ask service to remove all listeners of pending requests.
	 * 
	 * @throws InterruptedException
	 *             in case service binding fails.
	 */
	private void removeListenersOfAllPendingCachedRequests() throws InterruptedException {
		synchronized (mapPendingRequestToRequestListener) {
			if (!mapPendingRequestToRequestListener.isEmpty()) {
				waitForServiceToBeBound();
			}
			for (CachedContentRequest<?> cachedContentRequest : mapPendingRequestToRequestListener.keySet()) {
				final ContentRequest<?> request = cachedContentRequest.getContentRequest();
				final Set<RequestListener<?>> setRequestListeners = mapPendingRequestToRequestListener.get(cachedContentRequest);
				contentService.dontNotifyRequestListenersForRequest(request, setRequestListeners);
			}
			mapPendingRequestToRequestListener.clear();
			Log.v(LOG_TAG, "Cleared listeners of all pending requests");
		}
	}

	/**
	 * Wether or not a given {@link CachedContentRequest} matches a
	 * {@link ContentRequest}.
	 * 
	 * @param cachedContentRequest
	 *            the request know by the {@link ContentManager}.
	 * @param contentRequest
	 *            the request that we wish to remove notification for.
	 * @return true if {@link CachedContentRequest} matches contentRequest.
	 */
	private boolean match(CachedContentRequest<?> cachedContentRequest, ContentRequest<?> contentRequest) {
		if (contentRequest instanceof CachedContentRequest) {
			return contentRequest == cachedContentRequest;
		} else {
			return cachedContentRequest.getContentRequest() == contentRequest;
		}
	}

	// ============================================================================================
	// PUBLIC EXPOSED METHODS : content service driving.
	// ============================================================================================

	/**
	 * Cancel a specific request
	 * 
	 * @param request
	 *            the request to cancel
	 */
	public void cancel(ContentRequest<?> request) {
		request.cancel();
	}

	/**
	 * Cancel all requests
	 */
	public void cancelAllRequests() {
		try {
			lockSendRequestsToService.lock();
			// cancel each request that to be sent to service, and keep
			// listening for
			// cancellation.
			synchronized (mapRequestToLaunchToRequestListener) {
				for (CachedContentRequest<?> cachedContentRequest : mapRequestToLaunchToRequestListener.keySet()) {
					cachedContentRequest.cancel();
				}
			}

			// cancel each request that has been sent to service, and keep
			// listening for
			// cancellation.
			synchronized (mapPendingRequestToRequestListener) {
				for (CachedContentRequest<?> cachedContentRequest : mapPendingRequestToRequestListener.keySet()) {
					cachedContentRequest.cancel();
				}
			}
		} finally {
			lockSendRequestsToService.unlock();
		}
	}

	/**
	 * Remove some specific content from cache
	 * 
	 * @param clazz
	 *            the Type of data you want to remove from cache
	 * @param cacheKey
	 *            the key of the object in cache
	 * @return true if the data has been deleted from cache
	 */
	public <T> void removeDataFromCache(final Class<T> clazz, final Object cacheKey) {
		executorService.execute(new Runnable() {

			public void run() {
				try {
					waitForServiceToBeBound();
					contentService.removeDataFromCache(clazz, cacheKey);
				} catch (InterruptedException e) {
					Log.e(LOG_TAG, "Interrupted while waiting for acquiring service.");
				}
			}
		});
	}

	/**
	 * Remove all data from cache. This will clear all data stored by the
	 * {@link CacheManager} of the {@link ContentService}.
	 */
	public void removeAllDataFromCache() {
		executorService.execute(new Runnable() {

			public void run() {
				try {
					waitForServiceToBeBound();
					contentService.removeAllDataFromCache();
				} catch (InterruptedException e) {
					Log.e(LOG_TAG, "Interrupted while waiting for acquiring service.");
				}
			}
		});
	}

	/**
	 * Configure the behavior in case of error during reading/writing cache. <br/>
	 * Specify wether an error on reading/writing cache must fail the process.
	 * 
	 * @param failOnCacheError
	 *            true if an error must fail the process
	 */
	public void setFailOnCacheError(final boolean failOnCacheError) {
		executorService.execute(new Runnable() {

			public void run() {
				try {
					waitForServiceToBeBound();
					contentService.setFailOnCacheError(failOnCacheError);
				} catch (InterruptedException e) {
					Log.e(LOG_TAG, "Interrupted while waiting for acquiring service.");
				}
			}
		});
	}

	private <T> void addRequestListenerToListOfRequestListeners(CachedContentRequest<T> cachedContentRequest, RequestListener<T> requestListener) {
		Set<RequestListener<?>> listeners = mapRequestToLaunchToRequestListener.get(cachedContentRequest);
		if (listeners == null) {
			listeners = new HashSet<RequestListener<?>>();
			this.mapRequestToLaunchToRequestListener.put(cachedContentRequest, listeners);
		}

		if (!listeners.contains(requestListener)) {
			listeners.add(requestListener);
		}
	}

	// -------------------------------
	// -------Listeners notification
	// -------------------------------

	/**
	 * Dumps request processor state.
	 */
	public void dumpState() {

		executorService.execute(new Runnable() {
			public void run() {
				try {
					lockSendRequestsToService.lock();
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append("[ContentManager : ");

					stringBuilder.append("Requests to be launched : \n");
					dumpMap(stringBuilder, mapPendingRequestToRequestListener);

					stringBuilder.append("Pending requests : \n");
					dumpMap(stringBuilder, mapPendingRequestToRequestListener);

					stringBuilder.append(']');

					waitForServiceToBeBound();
					contentService.dumpState();
				} catch (InterruptedException e) {
					Log.e(LOG_TAG, "Interrupted while waiting for acquiring service.");
				} finally {
					lockSendRequestsToService.unlock();
				}
			}
		});
	}

	// ============================================================================================
	// INNER CLASS
	// ============================================================================================

	/** Reacts to binding/unbinding with {@link ContentService}. */
	public class ContentServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName name, IBinder service) {
			try {
				lockAcquireService.lock();

				contentService = ((ContentServiceBinder) service).getContentService();
				contentService.addContentServiceListener(new RequestRemoverContentServiceListener());
				Log.d(LOG_TAG, "Bound to service : " + contentService.getClass().getSimpleName());
				conditionServiceAcquired.signalAll();
			} finally {
				lockAcquireService.unlock();
			}
		}

		public void onServiceDisconnected(ComponentName name) {
			try {
				lockAcquireService.lock();
				contentService = null;
				Log.d(LOG_TAG, "Unbound from service : " + contentService.getClass().getSimpleName());
				isUnbinding = false;
				conditionServiceAcquired.signalAll();
			} finally {
				lockAcquireService.unlock();
			}
		}
	}

	/** Called when a request has been processed by the {@link ContentService}. */
	private class RequestRemoverContentServiceListener implements ContentServiceListener {
		public void onRequestProcessed(CachedContentRequest<?> contentRequest) {
			try {
				lockSendRequestsToService.lock();
				mapPendingRequestToRequestListener.remove(contentRequest);
			} finally {
				lockSendRequestsToService.unlock();
			}
		}
	}

	// ============================================================================================
	// PRIVATE METHODS : ContentService binding management.
	// ============================================================================================

	private void bindToService(Context context) {
		try {
			lockAcquireService.lock();

			if (contentService == null) {
				Intent intentService = new Intent(context, contentServiceClass);
				Log.v(LOG_TAG, "Binding to service.");
				contentServiceConnection = new ContentServiceConnection();
				context.bindService(intentService, contentServiceConnection, Context.BIND_AUTO_CREATE);
			}
		} finally {
			lockAcquireService.unlock();
		}
	}

	private void unbindFromService(Context context) {
		try {
			lockAcquireService.lock();
			if (contentService != null && !isUnbinding) {
				isUnbinding = true;
				contentService.removeContentServiceListener(removerContentServiceListener);
				Log.v(LOG_TAG, "Unbinding from service.");
				context.unbindService(this.contentServiceConnection);
			}
		} finally {
			lockAcquireService.unlock();
		}
	}

	/**
	 * Wait for acquiring binding to {@link ContentService}.
	 * 
	 * @throws InterruptedException
	 *             in case the binding is interrupted.
	 */
	protected void waitForServiceToBeBound() throws InterruptedException {
		Log.d(LOG_TAG, "Waiting for service to be bound.");

		lockAcquireService.lock();
		try {
			while (contentService == null && !isStopped) {
				conditionServiceAcquired.await();
			}
		} finally {
			lockAcquireService.unlock();
		}
	}

	private void checkServiceIsProperlyDeclaredInAndroidManifest(Context context) {
		Intent intentCheck = new Intent(context, contentServiceClass);
		if (context.getPackageManager().queryIntentServices(intentCheck, 0).isEmpty()) {
			throw new RuntimeException("Impossible to start content manager as no service of class : " + contentServiceClass.getName()
					+ " is registered in AndroidManifest.xml file !");
		}
	}

	private void dumpMap(StringBuilder stringBuilder, Map<CachedContentRequest<?>, Set<RequestListener<?>>> map) {
		synchronized (map) {
			stringBuilder.append(" request count= ");
			stringBuilder.append(mapRequestToLaunchToRequestListener.keySet().size());

			stringBuilder.append(", listeners per requests = [");
			for (Map.Entry<CachedContentRequest<?>, Set<RequestListener<?>>> entry : map.entrySet()) {
				stringBuilder.append(entry.getKey().getClass().getName());
				stringBuilder.append(":");
				stringBuilder.append(entry.getKey());
				stringBuilder.append(" --> ");
				if (entry.getValue() == null) {
					stringBuilder.append(entry.getValue());
				} else {
					stringBuilder.append(entry.getValue().size());
				}
				stringBuilder.append(" listeners");
				stringBuilder.append('\n');
			}
			stringBuilder.append(']');
			stringBuilder.append('\n');
		}
	}
}