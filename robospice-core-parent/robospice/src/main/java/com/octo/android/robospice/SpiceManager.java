package com.octo.android.robospice;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import roboguice.util.temp.Ln;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.octo.android.robospice.SpiceService.SpiceServiceBinder;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.SpiceServiceServiceListener;

/**
 * The instances of this class allow to acces the {@link SpiceService}. <br/>
 * They are tied to activities and obtain a local binding to the {@link SpiceService}. When binding occurs, the {@link SpiceManager} will send commadnds to the {@link SpiceService}, to execute
 * requests, clear cache, prevent listeners from being called and so on. Basically, all features of the {@link SpiceService} are accessible from the {@link SpiceManager}. It acts as an asynchronous
 * proxy : every call to a {@link SpiceService} method is asynchronous and will occur as soon as possible when the {@link SpiceManager} successfully binds to the service.
 * @author jva
 * @author sni
 * @author mwa
 */

/*
 * Note to maintainers : This class is quite complex and requires background knowledge in multi-threading & local service binding in android. Thx to Henri Tremblay (from EasyMock) for his happy code
 * review.
 */
public class SpiceManager implements Runnable {

    /** The class of the {@link SpiceService} to bind to. */
    private final Class<? extends SpiceService> spiceServiceClass;

    /** A reference on the {@link SpiceService} obtained by local binding. */
    private SpiceService spiceService;
    /** {@link SpiceService} binder. */
    private SpiceServiceConnection spiceServiceConnection = new SpiceServiceConnection();

    /** The contextWeakReference used to bind to the service from. */
    private WeakReference<Context> contextWeakReference;

    /**
     * Whether or not {@link SpiceManager} is started. Must be volatile to ensure multi-thread consistency.
     */
    private volatile boolean isStopped = true;

    /** The queue of requests to be sent to the service. */
    private final BlockingQueue<CachedSpiceRequest<?>> requestQueue = new LinkedBlockingQueue<CachedSpiceRequest<?>>();

    /**
     * The list of all requests that have not yet been passed to the service. All iterations must be synchronized.
     */
    private final Map<CachedSpiceRequest<?>, Set<RequestListener<?>>> mapRequestToLaunchToRequestListener = Collections
        .synchronizedMap(new IdentityHashMap<CachedSpiceRequest<?>, Set<RequestListener<?>>>());

    /**
     * The list of all requests that have already been passed to the service. All iterations must be synchronized.
     */
    private final Map<CachedSpiceRequest<?>, Set<RequestListener<?>>> mapPendingRequestToRequestListener = Collections
        .synchronizedMap(new IdentityHashMap<CachedSpiceRequest<?>, Set<RequestListener<?>>>());

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Lock used to synchronize binding to / unbinding from the {@link SpiceService}.
     */
    private final ReentrantLock lockAcquireService = new ReentrantLock();
    /** A monitor to ensure service is bound before accessing it. */
    private final Condition conditionServiceBound = lockAcquireService.newCondition();
    /** A monitor to ensure service is unbound. */
    private final Condition conditionServiceUnbound = lockAcquireService.newCondition();

    /**
     * Lock used to synchronize transmission of requests to the {@link SpiceService}.
     */
    private final ReentrantLock lockSendRequestsToService = new ReentrantLock();

    /** Thread running runnable code. */
    protected Thread runner;

    /** Reacts to service processing of requests. */
    private final RequestRemoverSpiceServiceListener removerSpiceServiceListener = new RequestRemoverSpiceServiceListener();

    /**
     * Whether or not we are unbinding (to prevent unbinding twice. Must be volatile to ensure multi-thread consistency.
     */
    private volatile boolean isUnbinding = false;

    // ============================================================================================
    // THREAD BEHAVIOR
    // ============================================================================================

    /**
     * Creates a {@link SpiceManager}. Typically this occurs in the construction of an Activity or Fragment. This method will check if the service to bind to has been properly declared in
     * AndroidManifest.
     * @param spiceServiceClass
     *            the service class to bind to.
     */
    public SpiceManager(final Class<? extends SpiceService> spiceServiceClass) {
        this.spiceServiceClass = spiceServiceClass;
    }

    /**
     * Start the {@link SpiceManager}. It will bind asynchronously to the {@link SpiceService}.
     * @param contextWeakReference
     *            a contextWeakReference that will be used to bind to the service. Typically, the Activity or Fragment that needs to interact with the {@link SpiceService}.
     */
    public synchronized void start(final Context context) {
        this.contextWeakReference = new WeakReference<Context>(context);
        if (runner != null) {
            throw new IllegalStateException("Already started.");
        } else {

            // start the binding to the service
            runner = new Thread(this);
            isStopped = false;
            runner.start();

            Ln.d("SpiceManager started.");
        }
    }

    /**
     * Method is synchronized with {@link #start(Context)}.
     * @return whether or not the {@link SpiceManager} is started.
     */
    public synchronized boolean isStarted() {
        return !isStopped;
    }

    @Override
    public void run() {
        // start the service it is not started yet.
        Context context = contextWeakReference.get();
        if (context != null) {
            checkServiceIsProperlyDeclaredInAndroidManifest(context);
            final Intent intent = new Intent(context, spiceServiceClass);
            context.startService(intent);
        } else {
            Ln.d("Service was not started as Activity died prematurely");
            isStopped = true;
            return;
        }

        bindToService(contextWeakReference.get());

        try {
            waitForServiceToBeBound();
            if (spiceService == null) {
                return;
            }
            while (!isStopped) {
                try {
                    sendRequestToService(requestQueue.take());
                } catch (final InterruptedException ex) {
                    Ln.d("Interrupted while waiting for new request.");
                }
            }
        } catch (final InterruptedException e) {
            Ln.d(e, "Interrupted while waiting for acquiring service.");
        } finally {
            unbindFromService(contextWeakReference.get());
        }
    }

    private void sendRequestToService(final CachedSpiceRequest<?> spiceRequest) {
        lockSendRequestsToService.lock();
        try {
            if (spiceRequest != null) {
                final Set<RequestListener<?>> listRequestListener = mapRequestToLaunchToRequestListener.remove(spiceRequest);
                mapPendingRequestToRequestListener.put(spiceRequest, listRequestListener);
                Ln.d("Sending request to service : " + spiceRequest.getClass().getSimpleName());
                spiceService.addRequest(spiceRequest, listRequestListener);
            }
        } finally {
            lockSendRequestsToService.unlock();
        }
    }

    /**
     * Stops the {@link SpiceManager}. It will unbind from {@link SpiceService}. All request listeners that had been registered to listen to {@link SpiceRequest}s sent from this {@link SpiceManager}
     * will be unregistered. None of them will be notified with the results of their {@link SpiceRequest}s. Unbinding will occur asynchronously.
     */
    public synchronized void shouldStop() {
        if (!isStarted()) {
            throw new IllegalStateException("Not started yet");
        }
        Ln.d("SpiceManager stopping.");
        dontNotifyAnyRequestListenersInternal();
        isUnbinding = false;
        unbindFromService(contextWeakReference.get());
        spiceServiceConnection = null;
        this.isStopped = true;
        this.runner.interrupt();
        this.runner = null;
        this.contextWeakReference.clear();
        Ln.d("SpiceManager stopped.");
    }

    /**
     * This is mostly a testing method. Stops the {@link SpiceManager}. It will unbind from {@link SpiceService}. All request listeners that had been registered to listen to {@link SpiceRequest}s sent
     * from this {@link SpiceManager} will be unregistered. None of them will be notified with the results of their {@link SpiceRequest}s. Unbinding will occur syncrhonously : the method returns when
     * all events have been unregistered and when main processing thread stops.
     */
    public synchronized void shouldStopAndJoin(final long timeOut) throws InterruptedException {
        if (this.runner == null) {
            throw new IllegalStateException("Not started yet");
        }

        Ln.d("SpiceManager stopping. Joining");
        dontNotifyAnyRequestListenersInternal();
        unbindFromService(contextWeakReference.get());
        this.isStopped = true;
        this.runner.interrupt();
        this.runner.join(timeOut);
        this.runner = null;
        this.contextWeakReference.clear();
        Ln.d("SpiceManager stopped.");
    }

    // ============================================================================================
    // PUBLIC EXPOSED METHODS : requests executions
    // ============================================================================================

    /**
     * Get some data previously saved in cache with key <i>requestCacheKey</i> with maximum time in cache : <i>cacheDuration</i> millisecond and register listeners to notify when request is finished.
     * This method executes a SpiceRequest with no network processing. It just checks whatever is in the cache and return it, including null if there is no such data found in cache.
     * @param clazz
     *            the class of the result to retrieve from cache.
     * @param requestCacheKey
     *            the key used to store and retrieve the result of the request in the cache
     * @param cacheExpiryDuration
     *            duration in milliseconds after which the content of the cache will be considered to be expired. {@link DurationInMillis#ALWAYS_RETURNED} means data in cache is always returned if it
     *            exists. {@link DurationInMillis#ALWAYS_EXPIRED} means data in cache is never returned.(see {@link DurationInMillis})
     * @param requestListener
     *            the listener to notify when the request will finish. If nothing is found in cache, listeners will receive a null result on their {@link RequestListener#onRequestSuccess(Object)}
     *            method. If something is found in cache, they will receive it in this method. If an error occurs, they will be notified via their
     *            {@link RequestListener#onRequestFailure(com.octo.android.robospice.persistence.exception.SpiceException)} method.
     */
    public <T> void getFromCache(final Class<T> clazz, final Object requestCacheKey, final long cacheExpiryDuration,
        final RequestListener<T> requestListener) {
        final SpiceRequest<T> request = new SpiceRequest<T>(clazz) {

            @Override
            public T loadDataFromNetwork() throws Exception {
                return null;
            }

            @Override
            public boolean isAggregatable() {
                return false;
            }
        };
        final CachedSpiceRequest<T> cachedSpiceRequest = new CachedSpiceRequest<T>(request, requestCacheKey, cacheExpiryDuration);
        execute(cachedSpiceRequest, requestListener);
    }

    /**
     * Add listener to a pending request if it exists. If no such request exists, this method does nothing. If a request identified by clazz and requestCacheKey, it will receive an additional
     * listener.
     * @param clazz
     *            the class of the result of the pending request to look for.
     * @param requestCacheKey
     *            the key used to store and retrieve the result of the request in the cache
     * @param requestListener
     *            the listener to notify when the request will finish.
     */
    public <T> void addListenerIfPending(final Class<T> clazz, final Object requestCacheKey, final RequestListener<T> requestListener) {
        final SpiceRequest<T> request = new SpiceRequest<T>(clazz) {

            @Override
            public T loadDataFromNetwork() throws Exception {
                return null;
            }
        };
        final CachedSpiceRequest<T> cachedSpiceRequest = new CachedSpiceRequest<T>(request, requestCacheKey, DurationInMillis.ALWAYS_EXPIRED);
        cachedSpiceRequest.setProcessable(false);
        execute(cachedSpiceRequest, requestListener);
    }

    /**
     * Execute a request, without using cache. No result from cache will be returned. The method {@link SpiceRequest#loadDataFromNetwork()} will always be invoked. The result will not be stored in
     * cache.
     * @param request
     *            the request to execute.
     * @param requestListener
     *            the listener to notify when the request will finish.
     */
    public <T> void execute(final SpiceRequest<T> request, final RequestListener<T> requestListener) {
        final CachedSpiceRequest<T> cachedSpiceRequest = new CachedSpiceRequest<T>(request, null, DurationInMillis.ALWAYS_RETURNED);
        execute(cachedSpiceRequest, requestListener);
    }

    /**
     * Execute a request. Before invoking the method {@link SpiceRequest#loadDataFromNetwork()}, the cache will be checked : if a result has been cached with the cache key <i>requestCacheKey</i>,
     * RoboSpice will consider the parameter <i>cacheExpiryDuration</i> to determine whether the result in the cache is expired or not. If it is not expired, then listeners will receive the data in
     * cache. Otherwise, the method {@link SpiceRequest#loadDataFromNetwork()} will be invoked and the result will be stored in cache using the cache key <i>requestCacheKey</i>.
     * @param request
     *            the request to execute
     * @param requestCacheKey
     *            the key used to store and retrieve the result of the request in the cache
     * @param cacheExpiryDuration
     *            duration in milliseconds after which the content of the cache will be considered to be expired. {@link DurationInMillis#ALWAYS_RETURNED} means data in cache is always returned if it
     *            exists. {@link DurationInMillis#ALWAYS_EXPIRED} means data in cache is never returned.(see {@link DurationInMillis})
     * @param requestListener
     *            the listener to notify when the request will finish
     */
    public <T> void execute(final SpiceRequest<T> request, final Object requestCacheKey, final long cacheExpiryDuration,
        final RequestListener<T> requestListener) {
        final CachedSpiceRequest<T> cachedSpiceRequest = new CachedSpiceRequest<T>(request, requestCacheKey, cacheExpiryDuration);
        execute(cachedSpiceRequest, requestListener);
    }

    /**
     * Execute a request, put the result in cache and register listeners to notify when request is finished.
     * @param cachedSpiceRequest
     *            the request to execute. {@link CachedSpiceRequest} is a wrapper of {@link SpiceRequest} that contains cache key and cache duration
     * @param requestListener
     *            the listener to notify when the request will finish
     */
    public <T> void execute(final CachedSpiceRequest<T> cachedSpiceRequest, final RequestListener<T> requestListener) {
        addRequestListenerToListOfRequestListeners(cachedSpiceRequest, requestListener);
        this.requestQueue.add(cachedSpiceRequest);
    }

    /**
     * Gets data from cache, expired or not, and executes a request normaly. Before invoking the method {@link SpiceRequest#loadDataFromNetwork()}, the cache will be checked : if a result has been
     * cached with the cache key <i>requestCacheKey</i>, RoboSpice will consider the parameter <i>cacheExpiryDuration</i> to determine whether the result in the cache is expired or not. If it is not
     * expired, then listeners will receive the data in cache only. If the result is absent or expired, then {@link SpiceRequest#loadDataFromNetwork()} will be invoked and the result will be stored in
     * cache using the cache key <i>requestCacheKey</i>.
     * @param request
     *            the request to execute
     * @param requestCacheKey
     *            the key used to store and retrieve the result of the request in the cache
     * @param cacheExpiryDuration
     *            duration in milliseconds after which the content of the cache will be considered to be expired. {@link DurationInMillis#ALWAYS_RETURNED} means data in cache is always returned if it
     *            exists. {@link DurationInMillis#ALWAYS_EXPIRED} doesn't make much sense here.
     * @param requestListener
     *            the listener to notify when the request will finish
     */
    public <T> void getFromCacheAndLoadFromNetworkIfExpired(final SpiceRequest<T> request, final Object requestCacheKey,
        final long cacheExpiryDuration, final RequestListener<T> requestListener) {
        final CachedSpiceRequest<T> cachedSpiceRequest = new CachedSpiceRequest<T>(request, requestCacheKey, cacheExpiryDuration);
        cachedSpiceRequest.setAcceptingDirtyCache(true);
        execute(cachedSpiceRequest, requestListener);
    }

    /**
     * Cancel a pending request if it exists. If no such request exists, this method does nothing. If a request identified by clazz and requestCacheKey exists, it will be cancelled and its associated
     * listeners will get notified.
     * @param clazz
     *            the class of the result of the pending request to look for.
     * @param requestCacheKey
     *            the cache key associated to the request's results.
     */
    public <T> void cancel(final Class<T> clazz, final Object requestCacheKey) {
        final SpiceRequest<T> request = new SpiceRequest<T>(clazz) {

            @Override
            public T loadDataFromNetwork() throws Exception {
                return null;
            }
        };
        final CachedSpiceRequest<T> cachedSpiceRequest = new CachedSpiceRequest<T>(request, requestCacheKey, DurationInMillis.ALWAYS_EXPIRED);
        cachedSpiceRequest.setProcessable(false);
        cachedSpiceRequest.cancel();
        execute(cachedSpiceRequest, null);
    }

    // ============================================================================================
    // PUBLIC EXPOSED METHODS : unregister listeners
    // ============================================================================================

    /**
     * Disable request listeners notifications for a specific request.<br/>
     * None of the listeners associated to this request will be called when request will finish.<br/>
     * This method will ask (asynchronously) to the {@link SpiceService} to remove listeners if requests have already been sent to the {@link SpiceService} if the request has already been sent to the
     * service. Otherwise, it will just remove listeners before passing the request to the {@link SpiceService}. Calling this method doesn't prevent request from being executed (and put in cache) but
     * will remove request's listeners notification.
     * @param request
     *            Request for which listeners are to unregistered.
     */
    public void dontNotifyRequestListenersForRequest(final SpiceRequest<?> request) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                dontNotifyRequestListenersForRequestInternal(request);
            }
        });
    }

    /**
     * Internal method to remove requests. If request has not been passed to the {@link SpiceService} yet, all listeners are unregistered locally before being passed to the service. Otherwise, it will
     * asynchronously ask to the {@link SpiceService} to remove the listeners of the request being processed.
     * @param request
     *            Request for which listeners are to unregistered.
     */
    protected void dontNotifyRequestListenersForRequestInternal(final SpiceRequest<?> request) {
        try {
            lockSendRequestsToService.lock();

            final boolean requestNotPassedToServiceYet = removeListenersOfCachedRequestToLaunch(request);
            Ln.v("Removed from requests to launch list : " + requestNotPassedToServiceYet);

            // if the request was already passed to service, bind to
            // service and
            // unregister listeners.
            if (!requestNotPassedToServiceYet) {
                removeListenersOfPendingCachedRequest(request);
                Ln.v("Removed from pending requests list");
            }

        } catch (final InterruptedException e) {
            Ln.e(e, "Interrupted while removing listeners.");
        } finally {
            lockSendRequestsToService.unlock();
        }
    }

    /**
     * Remove all listeners of a request that has not yet been passed to the {@link SpiceService}.
     * @param request
     *            the request for which listeners must be unregistered.
     * @return a boolean indicating if the request could be found inside the list of requests to be launched. If false, the request was already passed to the service.
     */
    private boolean removeListenersOfCachedRequestToLaunch(final SpiceRequest<?> request) {
        synchronized (mapRequestToLaunchToRequestListener) {
            for (final CachedSpiceRequest<?> cachedSpiceRequest : mapRequestToLaunchToRequestListener.keySet()) {
                if (match(cachedSpiceRequest, request)) {
                    final Set<RequestListener<?>> setRequestListeners = mapRequestToLaunchToRequestListener.get(cachedSpiceRequest);
                    setRequestListeners.clear();
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Remove all listeners of a request that may have already been passed to the {@link SpiceService}. If the request has already been passed to the {@link SpiceService}, the method will bind to the
     * service and ask it to remove listeners.
     * @param request
     *            the request for which listeners must be unregistered.
     */
    private void removeListenersOfPendingCachedRequest(final SpiceRequest<?> request) throws InterruptedException {
        synchronized (mapPendingRequestToRequestListener) {
            for (final CachedSpiceRequest<?> cachedSpiceRequest : mapPendingRequestToRequestListener.keySet()) {
                if (match(cachedSpiceRequest, request)) {
                    waitForServiceToBeBound();
                    if (spiceService == null) {
                        return;
                    }
                    final Set<RequestListener<?>> setRequestListeners = mapPendingRequestToRequestListener.get(cachedSpiceRequest);
                    spiceService.dontNotifyRequestListenersForRequest(cachedSpiceRequest, setRequestListeners);
                    mapPendingRequestToRequestListener.remove(cachedSpiceRequest);
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
            @Override
            public void run() {
                dontNotifyAnyRequestListenersInternal();
            }
        });
    }

    /**
     * Remove all listeners of requests. All requests that have not been yet passed to the service will see their of listeners cleaned. For all requests that have been passed to the service, we ask
     * the service to remove their listeners.
     */
    protected void dontNotifyAnyRequestListenersInternal() {
        try {
            lockSendRequestsToService.lock();

            mapRequestToLaunchToRequestListener.clear();
            Ln.v("Cleared listeners of all requests to launch");

            removeListenersOfAllPendingCachedRequests();
        } catch (final InterruptedException e) {
            Ln.e(e, "Interrupted while removing listeners.");
        } finally {
            lockSendRequestsToService.unlock();
        }
    }

    /**
     * Asynchronously ask service to remove all listeners of pending requests.
     * @throws InterruptedException
     *             in case service binding fails.
     */
    private void removeListenersOfAllPendingCachedRequests() throws InterruptedException {
        synchronized (mapPendingRequestToRequestListener) {
            if (!mapPendingRequestToRequestListener.isEmpty()) {
                if (spiceService == null) {
                    return;
                }
                for (final CachedSpiceRequest<?> cachedSpiceRequest : mapPendingRequestToRequestListener.keySet()) {

                    final Set<RequestListener<?>> setRequestListeners = mapPendingRequestToRequestListener.get(cachedSpiceRequest);
                    if (setRequestListeners != null) {
                        Ln.d("Removing listeners of request : " + cachedSpiceRequest.toString() + " : " + setRequestListeners.size());
                        spiceService.dontNotifyRequestListenersForRequest(cachedSpiceRequest, setRequestListeners);
                    }
                }
                mapPendingRequestToRequestListener.clear();
            }
            Ln.v("Cleared listeners of all pending requests");
        }
    }

    /**
     * Wether or not a given {@link CachedSpiceRequest} matches a {@link SpiceRequest}.
     * @param cachedSpiceRequest
     *            the request know by the {@link SpiceManager}.
     * @param spiceRequest
     *            the request that we wish to remove notification for.
     * @return true if {@link CachedSpiceRequest} matches contentRequest.
     */
    private boolean match(final CachedSpiceRequest<?> cachedSpiceRequest, final SpiceRequest<?> spiceRequest) {
        if (spiceRequest instanceof CachedSpiceRequest) {
            return spiceRequest == cachedSpiceRequest;
        } else {
            return cachedSpiceRequest.getSpiceRequest() == spiceRequest;
        }
    }

    // ============================================================================================
    // PUBLIC EXPOSED METHODS : content service driving.
    // ============================================================================================

    /**
     * Cancel a specific request
     * @param request
     *            the request to cancel
     */
    public void cancel(final SpiceRequest<?> request) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                request.cancel();
            }
        });
    }

    /**
     * Cancel all requests
     */
    public void cancelAllRequests() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                cancelAllRequestsInternal();
            }
        });
    }

    private void cancelAllRequestsInternal() {
        try {
            lockSendRequestsToService.lock();
            // cancel each request that to be sent to service, and
            // keep
            // listening for
            // cancellation.
            synchronized (mapRequestToLaunchToRequestListener) {
                for (final CachedSpiceRequest<?> cachedSpiceRequest : mapRequestToLaunchToRequestListener.keySet()) {
                    cachedSpiceRequest.cancel();
                }
            }

            // cancel each request that has been sent to service,
            // and keep
            // listening for cancellation.
            // we must duplicate the list as each call to cancel
            // will, by a listener of request processing
            // remove the request from our list.
            final List<CachedSpiceRequest<?>> listDuplicate = new ArrayList<CachedSpiceRequest<?>>(mapPendingRequestToRequestListener.keySet());
            for (final CachedSpiceRequest<?> cachedSpiceRequest : listDuplicate) {
                cachedSpiceRequest.cancel();
            }
        } finally {
            lockSendRequestsToService.unlock();
        }
    }

    public <T> Future<List<Object>> getAllCacheKeys(final Class<T> clazz) {
        return executorService.submit(new Callable<List<Object>>() {

            @Override
            public List<Object> call() throws Exception {
                waitForServiceToBeBound();
                if (spiceService == null) {
                    return new ArrayList<Object>();
                }
                return spiceService.getAllCacheKeys(clazz);
            }
        });
    }

    public <T> Future<List<T>> getAllDataFromCache(final Class<T> clazz) throws CacheLoadingException {
        return executorService.submit(new Callable<List<T>>() {

            @Override
            public List<T> call() throws Exception {
                waitForServiceToBeBound();
                if (spiceService == null) {
                    return new ArrayList<T>();
                }
                return spiceService.loadAllDataFromCache(clazz);
            }
        });
    }

    /**
     * Get some data previously saved in cache with key <i>requestCacheKey</i>. This method doesn't perform any network processing, it just check if there are previously saved data. Don't call this
     * method in the main thread because you could block it. Instead, use the asynchronous version of this method: {@link #getFromCache(final Class<T>, final Object, final long, final
     * RequestListener<T>) getFromCache}.
     * @param clazz
     *            the class of the result to retrieve from cache.
     * @param cacheKey
     *            the key used to store and retrieve the result of the request in the cache
     * @return
     * @throws CacheLoadingException
     *             Exception thrown when a problem occurs while loading data from cache.
     */
    public <T> Future<T> getDataFromCache(final Class<T> clazz, final Object cacheKey) throws CacheLoadingException {
        return executorService.submit(new Callable<T>() {

            @Override
            public T call() throws Exception {
                waitForServiceToBeBound();
                if (spiceService == null) {
                    return null;
                }
                return spiceService.getDataFromCache(clazz, cacheKey);
            }
        });
    }

    /**
     * Remove some specific content from cache
     * @param clazz
     *            the Type of data you want to remove from cache
     * @param cacheKey
     *            the key of the object in cache
     */
    public <T> void removeDataFromCache(final Class<T> clazz, final Object cacheKey) {
        executorService.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    waitForServiceToBeBound();
                    if (spiceService == null) {
                        return;
                    }
                    spiceService.removeDataFromCache(clazz, cacheKey);
                } catch (final InterruptedException e) {
                    Ln.e(e, "Interrupted while waiting for acquiring service.");
                }
            }
        });
    }

    /**
     * Remove some specific content from cache
     * @param clazz
     *            the type of data you want to remove from cache.
     */
    public <T> void removeDataFromCache(final Class<T> clazz) {
        executorService.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    waitForServiceToBeBound();
                    if (spiceService == null) {
                        return;
                    }
                    spiceService.removeAllDataFromCache(clazz);
                } catch (final InterruptedException e) {
                    Ln.e(e, "Interrupted while waiting for acquiring service.");
                }
            }
        });
    }

    /**
     * Remove all data from cache. This will clear all data stored by the {@link CacheManager} of the {@link SpiceService}.
     */
    public void removeAllDataFromCache() {
        executorService.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    waitForServiceToBeBound();
                    if (spiceService == null) {
                        return;
                    }
                    spiceService.removeAllDataFromCache();
                } catch (final InterruptedException e) {
                    Ln.e(e, "Interrupted while waiting for acquiring service.");
                }
            }
        });
    }

    /**
     * Configure the behavior in case of error during reading/writing cache. <br/>
     * Specify wether an error on reading/writing cache must fail the process.
     * @param failOnCacheError
     *            true if an error must fail the process
     */
    public void setFailOnCacheError(final boolean failOnCacheError) {
        executorService.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    waitForServiceToBeBound();
                    if (spiceService == null) {
                        return;
                    }
                    spiceService.setFailOnCacheError(failOnCacheError);
                } catch (final InterruptedException e) {
                    Ln.e(e, "Interrupted while waiting for acquiring service.");
                }
            }
        });
    }

    private <T> void addRequestListenerToListOfRequestListeners(final CachedSpiceRequest<T> cachedSpiceRequest,
        final RequestListener<T> requestListener) {
        synchronized (mapRequestToLaunchToRequestListener) {
            Set<RequestListener<?>> listeners = mapRequestToLaunchToRequestListener.get(cachedSpiceRequest);
            if (listeners == null) {
                listeners = new HashSet<RequestListener<?>>();
                this.mapRequestToLaunchToRequestListener.put(cachedSpiceRequest, listeners);
            }
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
            @Override
            public void run() {
                lockSendRequestsToService.lock();
                try {
                    final StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("[SpiceManager : ");

                    stringBuilder.append("Requests to be launched : \n");
                    dumpMap(stringBuilder, mapRequestToLaunchToRequestListener);

                    stringBuilder.append("Pending requests : \n");
                    dumpMap(stringBuilder, mapPendingRequestToRequestListener);

                    stringBuilder.append(']');

                    waitForServiceToBeBound();
                    if (spiceService == null) {
                        return;
                    }
                    spiceService.dumpState();
                } catch (final InterruptedException e) {
                    Ln.e(e, "Interrupted while waiting for acquiring service.");
                } finally {
                    lockSendRequestsToService.unlock();
                }
            }
        });
    }

    // ============================================================================================
    // INNER CLASS
    // ============================================================================================

    /** Reacts to binding/unbinding with {@link SpiceService}. */
    public class SpiceServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            lockAcquireService.lock();
            try {
                spiceService = ((SpiceServiceBinder) service).getSpiceService();
                spiceService.addSpiceServiceListener(new RequestRemoverSpiceServiceListener());
                Ln.d("Bound to service : " + spiceService.getClass().getSimpleName());
                conditionServiceBound.signalAll();
            } finally {
                lockAcquireService.unlock();
            }
        }

        /** Called only for unexpected unbinding. */
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            lockAcquireService.lock();
            try {
                Ln.d("Unbound from service start : " + spiceService.getClass().getSimpleName());
                spiceService = null;
                isUnbinding = false;
                conditionServiceUnbound.signalAll();
            } finally {
                lockAcquireService.unlock();
            }
        }
    }

    /**
     * Called when a request has been processed by the {@link SpiceService}.
     */
    private class RequestRemoverSpiceServiceListener implements SpiceServiceServiceListener {
        @Override
        public void onRequestProcessed(final CachedSpiceRequest<?> cachedSpiceRequest) {
            synchronized (mapPendingRequestToRequestListener) {
                mapPendingRequestToRequestListener.remove(cachedSpiceRequest);
            }
        }
    }

    // ============================================================================================
    // PRIVATE METHODS : SpiceService binding management.
    // ============================================================================================

    /** For testing purpose. */
    protected boolean isBound() {
        return spiceService != null;
    }

    private void bindToService(final Context context) {
        if (context == null || isStopped) {
            // fix issue 40. Thx Shussu
            return;
        }
        try {
            lockAcquireService.lock();

            if (spiceService == null) {
                final Intent intentService = new Intent(context, spiceServiceClass);
                Ln.v("Binding to service.");
                spiceServiceConnection = new SpiceServiceConnection();
                context.getApplicationContext().bindService(intentService, spiceServiceConnection, Context.BIND_AUTO_CREATE);
            }
        } finally {
            lockAcquireService.unlock();
        }
    }

    private void unbindFromService(final Context context) {
        if (context == null) {
            return;
        }

        try {
            Ln.v("Unbinding from service start.");
            lockAcquireService.lock();
            if (spiceService != null && !isUnbinding) {
                isUnbinding = true;
                spiceService.removeSpiceServiceListener(removerSpiceServiceListener);
                Ln.v("Unbinding from service.");
                context.getApplicationContext().unbindService(this.spiceServiceConnection);
                Ln.d("Unbound from service : " + spiceService.getClass().getSimpleName());
                spiceService = null;
                isUnbinding = false;
            }
        } catch (final Exception e) {
            Ln.e(e, "Could not unbind from service.");
        } finally {
            lockAcquireService.unlock();
        }
    }

    /**
     * Wait for acquiring binding to {@link SpiceService}.
     * @throws InterruptedException
     *             in case the binding is interrupted.
     */
    protected void waitForServiceToBeBound() throws InterruptedException {
        Ln.d("Waiting for service to be bound.");

        lockAcquireService.lock();
        try {
            while (spiceService == null && !isStopped) {
                conditionServiceBound.await();
            }
        } finally {
            lockAcquireService.unlock();
        }
    }

    /**
     * Wait for acquiring binding to {@link SpiceService}.
     * @throws InterruptedException
     *             in case the binding is interrupted.
     */
    protected void waitForServiceToBeUnbound() throws InterruptedException {
        Ln.d("Waiting for service to be unbound.");

        lockAcquireService.lock();
        try {
            while (spiceService != null) {
                conditionServiceUnbound.await();
            }
        } finally {
            lockAcquireService.unlock();
        }
    }

    private void checkServiceIsProperlyDeclaredInAndroidManifest(final Context context) {
        final Intent intentCheck = new Intent(context, spiceServiceClass);
        if (context.getPackageManager().queryIntentServices(intentCheck, 0).isEmpty()) {
            shouldStop();
            throw new RuntimeException("Impossible to start SpiceManager as no service of class : " + spiceServiceClass.getName()
                + " is registered in AndroidManifest.xml file !");
        }
    }

    private void dumpMap(final StringBuilder stringBuilder, final Map<CachedSpiceRequest<?>, Set<RequestListener<?>>> map) {
        synchronized (map) {
            stringBuilder.append(" request count= ");
            stringBuilder.append(mapRequestToLaunchToRequestListener.keySet().size());

            stringBuilder.append(", listeners per requests = [");
            for (final Map.Entry<CachedSpiceRequest<?>, Set<RequestListener<?>>> entry : map.entrySet()) {
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
