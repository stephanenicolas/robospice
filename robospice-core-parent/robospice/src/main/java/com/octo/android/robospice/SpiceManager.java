package com.octo.android.robospice;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
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
import com.octo.android.robospice.command.AddSpiceServiceListenerCommand;
import com.octo.android.robospice.command.GetAllCacheKeysCommand;
import com.octo.android.robospice.command.GetAllDataFromCacheCommand;
import com.octo.android.robospice.command.GetDataFromCacheCommand;
import com.octo.android.robospice.command.GetDateOfDataInCacheCommand;
import com.octo.android.robospice.command.IsDataInCacheCommand;
import com.octo.android.robospice.command.PutDataInCacheCommand;
import com.octo.android.robospice.command.RemoveAllDataFromCacheCommand;
import com.octo.android.robospice.command.RemoveDataClassFromCacheCommand;
import com.octo.android.robospice.command.RemoveDataFromCacheCommand;
import com.octo.android.robospice.command.RemoveSpiceServiceListenerCommand;
import com.octo.android.robospice.command.SetFailOnCacheErrorCommand;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.SpiceServiceAdapter;
import com.octo.android.robospice.request.listener.SpiceServiceListener;

/**
 * The instances of this class allow to acces the {@link SpiceService}. <br/>
 * They are tied to activities and obtain a local binding to the
 * {@link SpiceService}. When binding occurs, the {@link SpiceManager} will send
 * commadnds to the {@link SpiceService}, to execute requests, clear cache,
 * prevent listeners from being called and so on. Basically, all features of the
 * {@link SpiceService} are accessible from the {@link SpiceManager}. It acts as
 * an asynchronous proxy : every call to a {@link SpiceService} method is
 * asynchronous and will occur as soon as possible when the {@link SpiceManager}
 * successfully binds to the service.
 * @author jva
 * @author sni
 * @author mwa
 */

/*
 * Note to maintainers : This class is quite complex and requires background
 * knowledge in multi-threading & local service binding in android. Thx to Henri
 * Tremblay (from EasyMock) for his happy code review.
 */
public class SpiceManager implements Runnable {

    // ============================================================================================
    // CONSTANTS
    // ============================================================================================

    /**
     * The prefix of SpiceManager threads (used to send requests to the
     * service).
     */
    protected static final String SPICE_MANAGER_THREAD_NAME_PREFIX = "SpiceManagerThread ";
    /** Number of threads used to execute internal commands. */
    private static final int DEFAULT_THREAD_COUNT = 3;
    /** Delay to let runner stop properly (in ms). */
    private static final int DELAY_WAIT_FOR_RUNNER_TO_STOP = 500;

    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================

    /** The class of the {@link SpiceService} to bind to. */
    private final Class<? extends SpiceService> spiceServiceClass;

    /** A reference on the {@link SpiceService} obtained by local binding. */
    private SpiceService spiceService;
    /** {@link SpiceService} binder. */
    private SpiceServiceConnection spiceServiceConnection = new SpiceServiceConnection();

    /** The contextWeakReference used to bind to the service from. */
    private WeakReference<Context> contextWeakReference;

    /**
     * Whether or not {@link SpiceManager} is started. Must be volatile to
     * ensure multi-thread consistency.
     */
    private volatile boolean isStopped = true;

    /** The queue of requests to be sent to the service. */
    protected final BlockingQueue<CachedSpiceRequest<?>> requestQueue = new PriorityBlockingQueue<CachedSpiceRequest<?>>();

    /**
     * The list of all requests that have not yet been passed to the service.
     * All iterations must be synchronized. This is an identity list as we want
     * to keep every request.
     */
    private final Map<CachedSpiceRequest<?>, Set<RequestListener<?>>> mapRequestToLaunchToRequestListener = Collections
        .synchronizedMap(new IdentityHashMap<CachedSpiceRequest<?>, Set<RequestListener<?>>>());

    /**
     * The list of all requests that have already been passed to the service.
     * All iterations must be synchronized. This is *NOT* an identity list as we
     * want to take aggregation into account.
     */
    private final Map<CachedSpiceRequest<?>, Set<RequestListener<?>>> mapPendingRequestToRequestListener = Collections.synchronizedMap(new HashMap<CachedSpiceRequest<?>, Set<RequestListener<?>>>());

    /** Will execute internal commands of the SpiceManager. */
    private ExecutorService executorService;

    /**
     * Lock used to synchronize binding to / unbinding from the
     * {@link SpiceService}.
     */
    private final ReentrantLock lockAcquireService = new ReentrantLock();
    /** A monitor to ensure service is bound before accessing it. */
    private final Condition conditionServiceBound = lockAcquireService.newCondition();
    /** A monitor to ensure service is unbound. */
    private final Condition conditionServiceUnbound = lockAcquireService.newCondition();

    /**
     * Lock used to synchronize transmission of requests to the
     * {@link SpiceService}.
     */
    private final ReentrantLock lockSendRequestsToService = new ReentrantLock();

    /** Thread running runnable code. */
    protected Thread runner;

    /** Reacts to service processing of requests. */
    private final PendingRequestHandlerSpiceServiceListener removerSpiceServiceListener = new PendingRequestHandlerSpiceServiceListener();

    /**
     * Whether or not we are unbinding (to prevent unbinding twice. Must be
     * volatile to ensure multi-thread consistency.
     */
    private volatile boolean isUnbinding = false;

    /**
     * Use to give a distinct name to each instance SpiceManager Threads, used
     * to send request to the service.
     */
    private int spiceManagerThreadIndex;

    // ============================================================================================
    // THREAD BEHAVIOR
    // ============================================================================================

    /**
     * Creates a {@link SpiceManager}. Typically this occurs in the construction
     * of an Activity or Fragment. This method will check if the service to bind
     * to has been properly declared in AndroidManifest.
     * @param spiceServiceClass
     *            the service class to bind to.
     */
    public SpiceManager(final Class<? extends SpiceService> spiceServiceClass) {
        this.spiceServiceClass = spiceServiceClass;
    }

    /**
     * Number of threads used internally by this spice manager to communicate
     * commands to the SpiceService it is bound to
     * @return the thread count. Defaults to {@link #DEFAULT_THREAD_COUNT}.
     */
    protected int getThreadCount() {
        return DEFAULT_THREAD_COUNT;
    }

    /**
     * Start the {@link SpiceManager}. It will bind asynchronously to the
     * {@link SpiceService}.
     * @param context
     *            a context that will be used to bind to the service. Typically,
     *            the Activity or Fragment that needs to interact with the
     *            {@link SpiceService}.
     */
    public synchronized void start(final Context context) {
        this.contextWeakReference = new WeakReference<Context>(context);
        if (isStarted()) {
            throw new IllegalStateException("Already started.");
        } else {
            executorService = Executors.newFixedThreadPool(getThreadCount(), new MinPriorityThreadFactory());
            // start the binding to the service
            runner = new Thread(this, SPICE_MANAGER_THREAD_NAME_PREFIX + spiceManagerThreadIndex++);
            runner.setPriority(Thread.MIN_PRIORITY);
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

    /**
     * @return the number of current requests that should be launched ASAP (when
     *         the spice service is bound).
     */
    public int getRequestToLaunchCount() {
        return mapRequestToLaunchToRequestListener.size();
    }

    /**
     * @return the number of current request that are currently pending and
     *         being processed by the spice service.
     */
    public int getPendingRequestCount() {
        return mapPendingRequestToRequestListener.size();
    }

    private Context getContextReference() {
        return contextWeakReference.get();
    }

    @Override
    public void run() {

        if (!tryToStartService()) {
            Ln.d("Service was not started as Activity died prematurely");
            isStopped = true;
            return;
        }

        bindToService();

        try {
            waitForServiceToBeBound();
            if (spiceService == null) {
                Ln.d("No spice service bound.");
                return;
            }
            while (!requestQueue.isEmpty() || !isStopped && !Thread.interrupted()) {
                try {
                    sendRequestToService(requestQueue.take());
                } catch (final InterruptedException ex) {
                    Ln.d("Interrupted while waiting for new request.");
                    // we receive an interrupted exception while waiting
                    // see java spec : http://stackoverflow.com/a/6699006/693752
                    break;
                }
            }
            Ln.d("SpiceManager request runner terminated. Requests count: %d, stopped %b, interrupted %b", requestQueue.size(), isStopped, Thread.interrupted());
        } catch (final InterruptedException e) {
            Ln.d(e, "Interrupted while waiting for acquiring service.");
        }
    }

    private void sendRequestToService(final CachedSpiceRequest<?> spiceRequest) {
        lockSendRequestsToService.lock();
        try {
            if (spiceRequest != null && spiceService != null) {
                if (isStopped) {
                    Ln.d("Sending request to service without listeners : " + spiceRequest.getClass().getSimpleName());
                    spiceService.addRequest(spiceRequest, null);
                } else {
                    final Set<RequestListener<?>> listRequestListener = mapRequestToLaunchToRequestListener.get(spiceRequest);
                    Ln.d("Sending request to service : " + spiceRequest.getClass().getSimpleName());
                    spiceService.addRequest(spiceRequest, listRequestListener);
                }
            } else {
                Ln.d("Service or request was null");
            }
        } finally {
            lockSendRequestsToService.unlock();
        }
    }

    /**
     * Stops the {@link SpiceManager}. It will unbind from {@link SpiceService}.
     * All request listeners that had been registered to listen to
     * {@link SpiceRequest}s sent from this {@link SpiceManager} will be
     * unregistered. None of them will be notified with the results of their
     * {@link SpiceRequest}s. Unbinding will occur asynchronously.
     */
    public synchronized void shouldStop() {
        try {
            shouldStopAndJoin(DELAY_WAIT_FOR_RUNNER_TO_STOP);
        } catch (InterruptedException e) {
            Ln.e(e, "Exception when joining the runner that was stopping.");
        }
    }

    /**
     * This is mostly a testing method. Stops the {@link SpiceManager}. It will
     * unbind from {@link SpiceService}. All request listeners that had been
     * registered to listen to {@link SpiceRequest}s sent from this
     * {@link SpiceManager} will be unregistered. None of them will be notified
     * with the results of their {@link SpiceRequest}s. Unbinding will occur
     * syncrhonously : the method returns when all events have been unregistered
     * and when main processing thread stops.
     */
    public synchronized void shouldStopAndJoin(final long timeOut) throws InterruptedException {
        if (!isStarted()) {
            throw new IllegalStateException("Not started yet");
        }

        Ln.d("SpiceManager stopping. Joining");
        this.isStopped = true;
        dontNotifyAnyRequestListenersInternal();
        if (requestQueue.isEmpty()) {
            this.runner.interrupt();
        }
        long start = System.currentTimeMillis();
        try {
            this.runner.join(timeOut);
        } catch (InterruptedException e) {
            throw e;
        } finally {
            long end = System.currentTimeMillis();
            Ln.d("Runner join time (ms) when should stop %d", end - start);
        }
        unbindFromService();
        this.runner = null;
        this.executorService.shutdown();
        this.contextWeakReference.clear();
        Ln.d("SpiceManager stopped.");
    }

    // ============================================================================================
    // PUBLIC EXPOSED METHODS : requests executions
    // ============================================================================================

    /**
     * Get some data previously saved in cache with key <i>requestCacheKey</i>
     * with maximum time in cache : <i>cacheDuration</i> millisecond and
     * register listeners to notify when request is finished. This method
     * executes a SpiceRequest with no network processing. It just checks
     * whatever is in the cache and return it, including null if there is no
     * such data found in cache.
     * @param clazz
     *            the class of the result to retrieve from cache.
     * @param requestCacheKey
     *            the key used to store and retrieve the result of the request
     *            in the cache
     * @param cacheExpiryDuration
     *            duration in milliseconds after which the content of the cache
     *            will be considered to be expired.
     *            {@link DurationInMillis#ALWAYS_RETURNED} means data in cache
     *            is always returned if it exists.
     *            {@link DurationInMillis#ALWAYS_EXPIRED} means data in cache is
     *            never returned.(see {@link DurationInMillis})
     * @param requestListener
     *            the listener to notify when the request will finish. If
     *            nothing is found in cache, listeners will receive a null
     *            result on their
     *            {@link RequestListener#onRequestSuccess(Object)} method. If
     *            something is found in cache, they will receive it in this
     *            method. If an error occurs, they will be notified via their
     *            {@link RequestListener#onRequestFailure(com.octo.android.robospice.persistence.exception.SpiceException)}
     *            method.
     */
    public <T> void getFromCache(final Class<T> clazz, final Object requestCacheKey, final long cacheExpiryDuration, final RequestListener<T> requestListener) {
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
        cachedSpiceRequest.setOffline(true);
        execute(cachedSpiceRequest, requestListener);
    }

    /**
     * @See #addListenerIfPending(Class, Object, PendingRequestListener)
     */
    @Deprecated
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
     * Add listener to a pending request if it exists. If no such request
     * exists, this method calls onRequestNotFound on the listener. If a request
     * identified by clazz and requestCacheKey, it will receive an additional
     * listener.
     * @param clazz
     *            the class of the result of the pending request to look for.
     * @param requestCacheKey
     *            the key used to store and retrieve the result of the request
     *            in the cache
     * @param requestListener
     *            the listener to notify when the request will finish.
     */
    public <T> void addListenerIfPending(final Class<T> clazz, final Object requestCacheKey, final PendingRequestListener<T> requestListener) {
        addListenerIfPending(clazz, requestCacheKey, (RequestListener<T>) requestListener);
    }

    /**
     * Execute a request, without using cache. No result from cache will be
     * returned. The method {@link SpiceRequest#loadDataFromNetwork()} will
     * always be invoked. The result will not be stored in cache.
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
     * Execute a request. Before invoking the method
     * {@link SpiceRequest#loadDataFromNetwork()}, the cache will be checked :
     * if a result has been cached with the cache key <i>requestCacheKey</i>,
     * RoboSpice will consider the parameter <i>cacheExpiryDuration</i> to
     * determine whether the result in the cache is expired or not. If it is not
     * expired, then listeners will receive the data in cache. Otherwise, the
     * method {@link SpiceRequest#loadDataFromNetwork()} will be invoked and the
     * result will be stored in cache using the cache key
     * <i>requestCacheKey</i>.
     * @param request
     *            the request to execute
     * @param requestCacheKey
     *            the key used to store and retrieve the result of the request
     *            in the cache
     * @param cacheExpiryDuration
     *            duration in milliseconds after which the content of the cache
     *            will be considered to be expired.
     *            {@link DurationInMillis#ALWAYS_RETURNED} means data in cache
     *            is always returned if it exists.
     *            {@link DurationInMillis#ALWAYS_EXPIRED} means data in cache is
     *            never returned.(see {@link DurationInMillis})
     * @param requestListener
     *            the listener to notify when the request will finish
     */
    public <T> void execute(final SpiceRequest<T> request, final Object requestCacheKey, final long cacheExpiryDuration, final RequestListener<T> requestListener) {
        final CachedSpiceRequest<T> cachedSpiceRequest = new CachedSpiceRequest<T>(request, requestCacheKey, cacheExpiryDuration);
        execute(cachedSpiceRequest, requestListener);
    }

    /**
     * Execute a request, put the result in cache and register listeners to
     * notify when request is finished.
     * @param cachedSpiceRequest
     *            the request to execute. {@link CachedSpiceRequest} is a
     *            wrapper of {@link SpiceRequest} that contains cache key and
     *            cache duration
     * @param requestListener
     *            the listener to notify when the request will finish
     */
    public <T> void execute(final CachedSpiceRequest<T> cachedSpiceRequest, final RequestListener<T> requestListener) {
        addRequestListenerToListOfRequestListeners(cachedSpiceRequest, requestListener);
        Ln.d("adding request to request queue");
        this.requestQueue.add(cachedSpiceRequest);
    }

    /**
     * Gets data from cache, expired or not, and executes a request normaly.
     * Before invoking the method {@link SpiceRequest#loadDataFromNetwork()},
     * the cache will be checked : if a result has been cached with the cache
     * key <i>requestCacheKey</i>, RoboSpice will consider the parameter
     * <i>cacheExpiryDuration</i> to determine whether the result in the cache
     * is expired or not. If it is not expired, then listeners will receive the
     * data in cache only. If the result is absent or expired, then
     * {@link SpiceRequest#loadDataFromNetwork()} will be invoked and the result
     * will be stored in cache using the cache key <i>requestCacheKey</i>.
     * @param request
     *            the request to execute
     * @param requestCacheKey
     *            the key used to store and retrieve the result of the request
     *            in the cache
     * @param cacheExpiryDuration
     *            duration in milliseconds after which the content of the cache
     *            will be considered to be expired.
     *            {@link DurationInMillis#ALWAYS_RETURNED} means data in cache
     *            is always returned if it exists.
     *            {@link DurationInMillis#ALWAYS_EXPIRED} doesn't make much
     *            sense here.
     * @param requestListener
     *            the listener to notify when the request will finish
     */
    public <T> void getFromCacheAndLoadFromNetworkIfExpired(final SpiceRequest<T> request, final Object requestCacheKey, final long cacheExpiryDuration, final RequestListener<T> requestListener) {
        final CachedSpiceRequest<T> cachedSpiceRequest = new CachedSpiceRequest<T>(request, requestCacheKey, cacheExpiryDuration);
        cachedSpiceRequest.setAcceptingDirtyCache(true);
        execute(cachedSpiceRequest, requestListener);
    }

    /**
     * Adds some data to the cache, asynchronously.
     * @param clazz
     *            a super class or the class of data.
     * @param requestCacheKey
     *            the request cache key that data will be stored in.
     * @param data
     *            the data to store. Maybe null if supported by underlying
     *            ObjectPersister.
     * @param listener
     *            a listener that will be notified of this request's success or
     *            failure. May be null.
     */
    public <U, T extends U> void putInCache(final Class<U> clazz, final Object requestCacheKey, final T data, RequestListener<U> listener) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final SpiceRequest<U> spiceRequest = new SpiceRequest(clazz) {
            @Override
            public U loadDataFromNetwork() throws Exception {
                return data;
            }
        };
        CachedSpiceRequest<U> cachedSpiceRequest = new CachedSpiceRequest<U>(spiceRequest, requestCacheKey, DurationInMillis.ALWAYS_EXPIRED);
        cachedSpiceRequest.setOffline(true);
        execute(cachedSpiceRequest, listener);
    }

    /**
     * Adds some data to the cache, asynchronously. Same as
     * {@link #putInCache(Class, Object, Object, RequestListener)} with a null
     * listener. Operation will take place but you won't be notified.
     * @param clazz
     *            a super class or the class of data.
     * @param requestCacheKey
     *            the request cache key that data will be stored in.
     * @param data
     *            the data to store. Maybe null if supported by underlying
     *            ObjectPersister.
     */
    public <T> void putInCache(final Class<? super T> clazz, final Object requestCacheKey, final T data) {
        putInCache(clazz, requestCacheKey, data, null);
    }

    /**
     * Adds some data to the cache, asynchronously. Same as
     * {@link #putInCache(Class, Object, Object, RequestListener)} where the
     * class used to identify the request is data.getClass().
     * @param requestCacheKey
     *            the request cache key that data will be stored in.
     * @param data
     *            the data to store. Maybe null if supported by underlying
     *            ObjectPersister.
     * @param listener
     *            a listener that will be notified of this request's success or
     *            failure. May be null.
     */
    @SuppressWarnings("unchecked")
    public <T> void putInCache(final Object requestCacheKey, final T data, RequestListener<T> listener) {
        putInCache((Class<T>) data.getClass(), requestCacheKey, data, listener);
    }

    /**
     * Adds some data to the cache, asynchronously. Same as
     * {@link #putInCache(Class, Object, Object, RequestListener)} where the
     * class used to identify the request is data.getClass() and with a null
     * listener. Operation will take place but you won't be notified.
     * @param requestCacheKey
     *            the request cache key that data will be stored in.
     * @param data
     *            the data to store. Maybe null if supported by underlying
     *            ObjectPersister.
     */
    @SuppressWarnings("unchecked")
    public <T> void putInCache(final Object requestCacheKey, final T data) {
        putInCache((Class<T>) data.getClass(), requestCacheKey, data);
    }

    /**
     * Cancel a pending request if it exists. If no such request exists, this
     * method does nothing. If a request identified by clazz and requestCacheKey
     * exists, it will be cancelled and its associated listeners will get
     * notified. This method is asynchronous.
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
        cachedSpiceRequest.setOffline(true);
        cachedSpiceRequest.cancel();
        execute(cachedSpiceRequest, null);
    }

    // ============================================================================================
    // PUBLIC EXPOSED METHODS : unregister listeners
    // ============================================================================================

    /**
     * Disable request listeners notifications for a specific request.<br/>
     * None of the listeners associated to this request will be called when
     * request will finish.<br/>
     * This method will ask (asynchronously) to the {@link SpiceService} to
     * remove listeners if requests have already been sent to the
     * {@link SpiceService} if the request has already been sent to the service.
     * Otherwise, it will just remove listeners before passing the request to
     * the {@link SpiceService}. Calling this method doesn't prevent request
     * from being executed (and put in cache) but will remove request's
     * listeners notification.
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
     * Internal method to remove requests. If request has not been passed to the
     * {@link SpiceService} yet, all listeners are unregistered locally before
     * being passed to the service. Otherwise, it will asynchronously ask to the
     * {@link SpiceService} to remove the listeners of the request being
     * processed.
     * @param request
     *            Request for which listeners are to unregistered.
     */
    protected void dontNotifyRequestListenersForRequestInternal(final SpiceRequest<?> request) {
        lockSendRequestsToService.lock();
        try {

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
     * Remove all listeners of a request that has not yet been passed to the
     * {@link SpiceService}.
     * @param request
     *            the request for which listeners must be unregistered.
     * @return a boolean indicating if the request could be found inside the
     *         list of requests to be launched. If false, the request was
     *         already passed to the service.
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
     * Remove all listeners of a request that may have already been passed to
     * the {@link SpiceService}. If the request has already been passed to the
     * {@link SpiceService}, the method will bind to the service and ask it to
     * remove listeners.
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
     * Remove all listeners of requests. All requests that have not been yet
     * passed to the service will see their of listeners cleaned. For all
     * requests that have been passed to the service, we ask the service to
     * remove their listeners.
     */
    protected void dontNotifyAnyRequestListenersInternal() {
        lockSendRequestsToService.lock();
        try {
            if (spiceService == null) {
                return;
            }
            synchronized (mapRequestToLaunchToRequestListener) {
                if (!mapRequestToLaunchToRequestListener.isEmpty()) {
                    for (final CachedSpiceRequest<?> cachedSpiceRequest : mapRequestToLaunchToRequestListener.keySet()) {
                        final Set<RequestListener<?>> setRequestListeners = mapRequestToLaunchToRequestListener.get(cachedSpiceRequest);
                        if (setRequestListeners != null) {
                            Ln.d("Removing listeners of request to launch : " + cachedSpiceRequest.toString() + " : " + setRequestListeners.size());
                            spiceService.dontNotifyRequestListenersForRequest(cachedSpiceRequest, setRequestListeners);
                        }
                    }
                }
                mapRequestToLaunchToRequestListener.clear();
            }
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
                for (final CachedSpiceRequest<?> cachedSpiceRequest : mapPendingRequestToRequestListener.keySet()) {

                    final Set<RequestListener<?>> setRequestListeners = mapPendingRequestToRequestListener.get(cachedSpiceRequest);
                    if (setRequestListeners != null) {
                        Ln.d("Removing listeners of pending request : " + cachedSpiceRequest.toString() + " : " + setRequestListeners.size());
                        spiceService.dontNotifyRequestListenersForRequest(cachedSpiceRequest, setRequestListeners);
                    }
                }
                mapPendingRequestToRequestListener.clear();
            }
        }
        Ln.v("Cleared listeners of all pending requests");
    }

    /**
     * Wether or not a given {@link CachedSpiceRequest} matches a
     * {@link SpiceRequest}.
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
     * Cancel a specific request. Synchronous.
     * @param request
     *            the request to cancel
     */
    public void cancel(final SpiceRequest<?> request) {
        request.cancel();
    }

    /**
     * Cancel all requests. Asynchronous.
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
        lockSendRequestsToService.lock();
        try {
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

    public void addSpiceServiceListener(SpiceServiceListener spiceServiceListener) {
        executeCommand(new AddSpiceServiceListenerCommand(this, spiceServiceListener));
    }

    public void removeSpiceServiceListener(SpiceServiceListener spiceServiceListener) {
        executeCommand(new RemoveSpiceServiceListenerCommand(this, spiceServiceListener));
    }

    public Future<List<Object>> getAllCacheKeys(final Class<?> clazz) {
        return executeCommand(new GetAllCacheKeysCommand(this, clazz));
    }

    public <T> Future<List<T>> getAllDataFromCache(final Class<T> clazz) throws CacheLoadingException {
        return executeCommand(new GetAllDataFromCacheCommand<T>(this, clazz));
    }

    /**
     * Get some data previously saved in cache with key <i>requestCacheKey</i>.
     * This method doesn't perform any network processing, it just checks if
     * there are previously saved data. Don't call this method in the main
     * thread because you could block it. Instead, use the asynchronous version
     * of this method:
     * {@link #getFromCache(Class, Object, long, RequestListener)}.
     * @param clazz
     *            the class of the result to retrieve from cache.
     * @param cacheKey
     *            the key used to store and retrieve the result of the request
     *            in the cache
     * @return a future object that will hold data in cache. Calling get on this
     *         future will block until the data is actually effectively taken
     *         from cache.
     * @throws CacheLoadingException
     *             Exception thrown when a problem occurs while loading data
     *             from cache.
     */
    public <T> Future<T> getDataFromCache(final Class<T> clazz, final Object cacheKey) throws CacheLoadingException {
        return executeCommand(new GetDataFromCacheCommand<T>(this, clazz, cacheKey));
    }

    /**
     * Put some new data in cache using cache key <i>requestCacheKey</i>. This
     * method doesn't perform any network processing, it just data in cache,
     * erasing any previsouly saved date in cache using the same class and key.
     * Don't call this method in the main thread because you could block it.
     * Instead, use the asynchronous version of this method:
     * {@link #putInCache(Class, Object, Object)}.
     * @param cacheKey
     *            the key used to store and retrieve the result of the request
     *            in the cache
     * @param data
     *            the data to be saved in cache.
     * @return the data has it has been saved by an ObjectPersister in cache.
     * @throws CacheLoadingException
     *             Exception thrown when a problem occurs while loading data
     *             from cache.
     */
    public <T> Future<T> putDataInCache(final Object cacheKey, final T data) throws CacheSavingException, CacheCreationException {
        return executeCommand(new PutDataInCacheCommand<T>(this, data, cacheKey));
    }

    /**
     * Tests whether some data is present in cache or not.
     * @param clazz
     *            the class of the result to retrieve from cache.
     * @param cacheKey
     *            the key used to store and retrieve the result of the request
     *            in the cache
     * @param cacheExpiryDuration
     *            duration in milliseconds after which the content of the cache
     *            will be considered to be expired.
     *            {@link DurationInMillis#ALWAYS_RETURNED} means data in cache
     *            is always returned if it exists.
     *            {@link DurationInMillis#ALWAYS_EXPIRED} means data in cache is
     *            never returned.(see {@link DurationInMillis})
     * @return the data has it has been saved by an ObjectPersister in cache.
     * @throws CacheCreationException
     *             Exception thrown when a problem occurs while looking for data
     *             in cache.
     */
    public Future<Boolean> isDataInCache(Class<?> clazz, final Object cacheKey, long cacheExpiryDuration) throws CacheCreationException {
        return executeCommand(new IsDataInCacheCommand(this, clazz, cacheKey, cacheExpiryDuration));
    }

    /**
     * Returns the last date of storage of a given data into the cache.
     * @param clazz
     *            the class of the result to retrieve from cache.
     * @param cacheKey
     *            the key used to store and retrieve the result of the request
     *            in the cache
     * @return the date at which data has been stored in cache. Null if no such
     *         data is in Cache.
     * @throws CacheLoadingException
     *             Exception thrown when a problem occurs while loading data
     *             from cache.
     */
    public Future<Date> getDateOfDataInCache(Class<?> clazz, final Object cacheKey) throws CacheCreationException {
        return executeCommand(new GetDateOfDataInCacheCommand(this, clazz, cacheKey));
    }

    /**
     * Remove some specific content from cache
     * @param clazz
     *            the Type of data you want to remove from cache
     * @param cacheKey
     *            the key of the object in cache
     */
    public <T> Future<?> removeDataFromCache(final Class<T> clazz, final Object cacheKey) {
        if (clazz == null || cacheKey == null) {
            throw new IllegalArgumentException("Both parameters must be non null.");
        }

        return executeCommand(new RemoveDataFromCacheCommand(this, clazz, cacheKey));
    }

    /**
     * Remove some specific content from cache
     * @param clazz
     *            the type of data you want to remove from cache.
     */
    public <T> Future<?> removeDataFromCache(final Class<T> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Clazz must be non null.");
        }
        return executeCommand(new RemoveDataClassFromCacheCommand(this, clazz));
    }

    /**
     * Remove all data from cache. This will clear all data stored by the
     * {@link CacheManager} of the {@link SpiceService}.
     */
    public Future<?> removeAllDataFromCache() {
        return executeCommand(new RemoveAllDataFromCacheCommand(this));
    }

    /**
     * Configure the behavior in case of error during reading/writing cache. <br/>
     * Specify wether an error on reading/writing cache must fail the process.
     * @param failOnCacheError
     *            true if an error must fail the process
     */
    public void setFailOnCacheError(final boolean failOnCacheError) {
        executeCommand(new SetFailOnCacheErrorCommand(this, failOnCacheError));
    }

    private <T> void addRequestListenerToListOfRequestListeners(final CachedSpiceRequest<T> cachedSpiceRequest, final RequestListener<T> requestListener) {
        synchronized (mapRequestToLaunchToRequestListener) {
            Set<RequestListener<?>> listeners = mapRequestToLaunchToRequestListener.get(cachedSpiceRequest);
            if (listeners == null) {
                listeners = Collections.synchronizedSet(new HashSet<RequestListener<?>>());
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

    private static final class MinPriorityThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable arg0) {
            Thread t = new Thread(arg0);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    }

    /** Reacts to binding/unbinding with {@link SpiceService}. */
    public class SpiceServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            lockAcquireService.lock();
            try {
                if (service instanceof SpiceServiceBinder) {
                    spiceService = ((SpiceServiceBinder) service).getSpiceService();
                    spiceService.addSpiceServiceListener(removerSpiceServiceListener);
                    Ln.d("Bound to service : " + spiceService.getClass().getSimpleName());
                    conditionServiceBound.signalAll();
                } else {
                    Ln.e("Unexpected IBinder service at onServiceConnected :%s ", service.getClass().getName());
                }
            } finally {
                lockAcquireService.unlock();
            }
        }

        /** Called only for unexpected unbinding. */
        @Override
        public void onServiceDisconnected(final ComponentName name) {
            lockAcquireService.lock();
            try {
                if (spiceService != null) {
                    Ln.d("Unbound from service start : " + spiceService.getClass().getSimpleName());
                    spiceService = null;
                    isUnbinding = false;
                    conditionServiceUnbound.signalAll();
                }
            } finally {
                lockAcquireService.unlock();
            }
        }
    }

    /**
     * Called when a request has been processed by the {@link SpiceService}.
     */
    private class PendingRequestHandlerSpiceServiceListener extends SpiceServiceAdapter {
        @Override
        public void onRequestAdded(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {
            Set<RequestListener<?>> listeners = mapRequestToLaunchToRequestListener.remove(cachedSpiceRequest);
            if (listeners != null) {
                mapPendingRequestToRequestListener.put(cachedSpiceRequest, listeners);
            }
        }

        @Override
        public void onRequestAggregated(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {
            Set<RequestListener<?>> listeners = mapPendingRequestToRequestListener.get(cachedSpiceRequest);
            if (listeners == null) {
                listeners = Collections.synchronizedSet(new HashSet<RequestListener<?>>());
                mapPendingRequestToRequestListener.put(cachedSpiceRequest, listeners);
            }
            Set<RequestListener<?>> listenersToLaunch = mapRequestToLaunchToRequestListener.remove(cachedSpiceRequest);
            if (listenersToLaunch != null) {
                synchronized (mapPendingRequestToRequestListener) {
                    listeners.addAll(listenersToLaunch);
                }
            }
        }

        @Override
        public void onRequestNotFound(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {
            mapRequestToLaunchToRequestListener.remove(cachedSpiceRequest);
        }

        @Override
        public void onRequestProcessed(final CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {
            mapPendingRequestToRequestListener.remove(cachedSpiceRequest);
        }
    }

    // ============================================================================================
    // PRIVATE METHODS : SpiceService binding management.
    // ============================================================================================

    /** For testing purpose. */
    protected boolean isBound() {
        return spiceService != null;
    }

    private boolean tryToStartService() {
        boolean success = false;

        // start the service it is not started yet.
        Context context = getContextReference();
        if (context != null) {
            checkServiceIsProperlyDeclaredInAndroidManifest(context);
            final Intent intent = new Intent(context, spiceServiceClass);
            context.startService(intent);
            success = true;
        }

        return success;
    }

    private void bindToService() {
        Context context = getContextReference();
        if (context == null || requestQueue.isEmpty() && isStopped) {
            // fix issue 40. Thx Shussu
            // fix issue 246.
            return;
        }

        lockAcquireService.lock();
        lockSendRequestsToService.lock();
        try {

            if (spiceService == null) {
                final Intent intentService = new Intent(context, spiceServiceClass);
                Ln.v("Binding to service.");
                spiceServiceConnection = new SpiceServiceConnection();
                boolean bound = context.getApplicationContext().bindService(intentService, spiceServiceConnection, Context.BIND_AUTO_CREATE);
                if (!bound) {
                    Ln.v("Binding to service failed.");
                } else {
                    Ln.v("Binding to service succeeded.");
                }
            }
        } catch (Exception t) {
            // this should not happen in apps, but can happen during tests.
            Ln.d(t, "Binding to service failed.");
            Ln.d("Context is" + context);
            Ln.d("ApplicationContext is " + context.getApplicationContext());
        } finally {
            lockSendRequestsToService.unlock();
            lockAcquireService.unlock();
        }
    }

    private void unbindFromService() {
        Context context = getContextReference();
        if (context == null) {
            return;
        }

        lockAcquireService.lock();
        // fix issue 144 and 86
        lockSendRequestsToService.lock();
        try {
            Ln.v("Unbinding from service start.");
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
            lockSendRequestsToService.unlock();
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
            while (spiceService == null && (!requestQueue.isEmpty() || !isStopped)) {
                conditionServiceBound.await();
            }
            Ln.d("Bound ok.");
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

    protected <T> Future<T> executeCommand(SpiceManagerCommand<T> spiceManagerCommand) {
        if (executorService == null || executorService.isShutdown()) {
            return null;
        }
        return executorService.submit(spiceManagerCommand);
    }

    private void checkServiceIsProperlyDeclaredInAndroidManifest(final Context context) {
        final Intent intentCheck = new Intent(context, spiceServiceClass);
        if (context.getPackageManager().queryIntentServices(intentCheck, 0).isEmpty()) {
            shouldStop();
            throw new RuntimeException("Impossible to start SpiceManager as no service of class : " + spiceServiceClass.getName() + " is registered in AndroidManifest.xml file !");
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

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------
    public abstract static class SpiceManagerCommand<T> implements Callable<T> {
        protected SpiceManager spiceManager;
        private boolean successFull;
        private Exception exception;

        public SpiceManagerCommand(SpiceManager spiceManager) {
            this.spiceManager = spiceManager;
        }

        @Override
        public T call() {
            try {
                spiceManager.waitForServiceToBeBound();
                if (spiceManager.spiceService == null) {
                    return null;
                }

                spiceManager.lockSendRequestsToService.lock();
                try {
                    if (spiceManager.spiceService == null || spiceManager.isStopped) {
                        return null;
                    }
                    T result = executeWhenBound(spiceManager.spiceService);
                    successFull = true;
                    return result;
                } catch (Exception e) {
                    Ln.e(e);
                    this.exception = e;
                    return null;
                } finally {
                    spiceManager.lockSendRequestsToService.unlock();
                }
            } catch (InterruptedException e) {
                Ln.e(e, "Spice command %s couldn't bind to service.", getClass().getName());
                return null;
            }

        }

        protected abstract T executeWhenBound(SpiceService service) throws Exception;

        public boolean isSuccessFull() {
            return successFull;
        }

        public Exception getException() {
            return exception;
        }

    }

}
