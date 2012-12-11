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

/**
 * The instances of this class allow to acces the {@link SpiceService}. <br/>
 * 
 * They are tied to activities and obtain a local binding to the {@link SpiceService}. When binding occurs, the
 * {@link SpiceManager} will send commadnds to the {@link SpiceService}, to execute requests, clear cache, prevent
 * listeners from beeing called and so on.
 * 
 * Basically, all features of the {@link SpiceService} are accessible from the {@link SpiceManager}. It acts as an
 * asynchronous proxy : every call to a {@link SpiceService} method is asynchronous and will occur as soon as possible
 * when the {@link SpiceManager} successfully binds to the service.
 * 
 * @author jva
 * @author sni
 * @author mwa
 * 
 */

/*
 * Note to maintainers : This class is quite complex and requires background knowledge in multi-threading & local
 * service binding in android.
 */
public class SpiceManager implements Runnable {

    /** The class of the {@link SpiceService} to bind to. */
    private Class< ? extends SpiceService > contentServiceClass;

    /** A reference on the {@link SpiceService} obtained by local binding. */
    private SpiceService spiceService;
    /** {@link SpiceService} binder. */
    private SpiceServiceConnection spiceServiceConnection = new SpiceServiceConnection();

    /** The context used to bind to the service from. */
    private WeakReference< Context > context;

    /** Wether or not {@link SpiceManager} is started. */
    private boolean isStopped = true;

    /** The queue of requests to be sent to the service. */
    private BlockingQueue< CachedSpiceRequest< ? >> requestQueue = new LinkedBlockingQueue< CachedSpiceRequest< ? >>();

    /**
     * The list of all requests that have not yet been passed to the service. All iterations must be synchronized.
     */
    private Map< CachedSpiceRequest< ? >, Set< RequestListener< ? >>> mapRequestToLaunchToRequestListener = Collections
            .synchronizedMap( new IdentityHashMap< CachedSpiceRequest< ? >, Set< RequestListener< ? >>>() );
    /**
     * The list of all requests that have already been passed to the service. All iterations must be synchronized.
     */
    private Map< CachedSpiceRequest< ? >, Set< RequestListener< ? >>> mapPendingRequestToRequestListener = Collections
            .synchronizedMap( new IdentityHashMap< CachedSpiceRequest< ? >, Set< RequestListener< ? >>>() );

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Lock used to synchronize binding to / unbing from the {@link SpiceService}.
     */
    private ReentrantLock lockAcquireService = new ReentrantLock();
    /** A monitor to ensure service is bound before accessing it. */
    private Condition conditionServiceBound = lockAcquireService.newCondition();
    /** A monitor to ensure service is unbound. */
    private Condition conditionServiceUnbound = lockAcquireService.newCondition();

    /**
     * Lock used to synchronize transmission of requests to the {@link SpiceService}.
     */
    private ReentrantLock lockSendRequestsToService = new ReentrantLock();

    /** Thread running runnable code. */
    protected Thread runner;

    /** Reacts to service processing of requests. */
    private RequestRemoverContentServiceListener removerContentServiceListener = new RequestRemoverContentServiceListener();

    /** Whether or not we are unbinding (to prevent unbinding twice. */
    public boolean isUnbinding = false;

    // ============================================================================================
    // THREAD BEHAVIOR
    // ============================================================================================

    /**
     * Creates a {@link SpiceManager}. Typically this occurs in the construction of an Activity or Fragment.
     * 
     * This method will check if the service to bind to has been properly declared in AndroidManifest.
     * 
     * @param contentServiceClass
     *            the service class to bind to.
     */
    public SpiceManager( Class< ? extends SpiceService > contentServiceClass ) {
        this.contentServiceClass = contentServiceClass;
    }

    /**
     * Start the {@link SpiceManager}. It will bind asynchronously to the {@link SpiceService}.
     * 
     * @param context
     *            a context that will be used to bind to the service. Typically, the Activity or Fragment that needs to
     *            interact with the {@link SpiceService}.
     */
    public synchronized void start( Context context ) {
        this.context = new WeakReference< Context >( context );
        if ( runner != null ) {
            throw new IllegalStateException( "Already started." );
        } else {

            // start the binding to the service
            runner = new Thread( this );
            isStopped = false;
            runner.start();

            Ln.d( "Content manager started." );
        }
    }

    /**
     * Method is synchronized with {@link #start(Context)}.
     * 
     * @return whether or not the {@link SpiceManager} is started.
     */
    public synchronized boolean isStarted() {
        return !isStopped;
    }

    public void run() {
        checkServiceIsProperlyDeclaredInAndroidManifest( context.get() );
        // start the service it is not started yet.
        if ( !SpiceService.isStarted() ) {
            Intent intent = new Intent( context.get(), contentServiceClass );
            context.get().startService( intent );
        }

        bindToService( context.get() );

        try {
            waitForServiceToBeBound();
            while ( !isStopped ) {
                CachedSpiceRequest< ? > spiceRequest = requestQueue.take();
                try {
                    lockSendRequestsToService.lock();
                    if ( spiceRequest != null ) {
                        Set< RequestListener< ? >> listRequestListener = mapRequestToLaunchToRequestListener.get( spiceRequest );
                        mapRequestToLaunchToRequestListener.remove( spiceRequest );
                        mapPendingRequestToRequestListener.put( spiceRequest, listRequestListener );
                        Ln.d( "Sending request to service : " + spiceRequest.getClass().getSimpleName() );
                        spiceService.addRequest( spiceRequest, listRequestListener );
                    }
                } finally {
                    lockSendRequestsToService.unlock();
                }
            }
        } catch ( InterruptedException e ) {
            Ln.d( e, "Interrupted while waiting for acquiring service." );
        } finally {
            unbindFromService( context.get() );
        }
    }

    /**
     * Stops the {@link SpiceManager}. It will unbind from {@link SpiceService}. All request listeners that had been
     * registered to listen to {@link SpiceRequest}s sent from this {@link SpiceManager} will be unregistered. None of
     * them will be notified with the results of their {@link SpiceRequest}s.
     * 
     * Unbinding will occur asynchronously.
     */
    public synchronized void shouldStop() {
        if ( this.runner == null ) {
            throw new IllegalStateException( "Not started yet" );
        }
        Ln.d( "Content manager stopping." );
        dontNotifyAnyRequestListenersInternal();
        isUnbinding = false;
        unbindFromService( context.get() );
        spiceServiceConnection = null;
        this.isStopped = true;
        this.runner.interrupt();
        this.runner = null;
        this.context.clear();
        Ln.d( "Content manager stopped." );
    }

    /**
     * This is mostly a testing method.
     * 
     * Stops the {@link SpiceManager}. It will unbind from {@link SpiceService}. All request listeners that had been
     * registered to listen to {@link SpiceRequest}s sent from this {@link SpiceManager} will be unregistered. None of
     * them will be notified with the results of their {@link SpiceRequest}s.
     * 
     * Unbinding will occur syncrhonously : the method returns when all events have been unregistered and when main
     * processing thread stops.
     * 
     */
    public synchronized void shouldStopAndJoin( long timeOut ) throws InterruptedException {
        if ( this.runner == null ) {
            throw new IllegalStateException( "Not started yet" );
        }

        Ln.d( "Content manager stopping. Joining" );
        dontNotifyAnyRequestListenersInternal();
        unbindFromService( context.get() );
        this.isStopped = true;
        this.runner.interrupt();
        this.runner.join( timeOut );
        this.runner = null;
        this.context.clear();
        Ln.d( "Content manager stopped." );
    }

    // ============================================================================================
    // PUBLIC EXPOSED METHODS : requests executions
    // ============================================================================================

    /**
     * Get some data previously saved in cache with key <i>requestCacheKey</i> with maximum time in cache :
     * <i>cacheDuration</i> millisecond and register listeners to notify when request is finished. This method executes
     * a SpiceRequest with no network processing. It just checks whatever is in the cache and return it, including null
     * if there is no such data found in cache.
     * 
     * @param clazz
     *            the class of the result to retrieve from cache.
     * @param requestCacheKey
     *            the key used to store and retrieve the result of the request in the cache
     * @param cacheDuration
     *            the time in millisecond to keep cache alive (see {@link DurationInMillis})
     * @param requestListener
     *            the listener to notify when the request will finish. If nothing is found in cache, listeners will
     *            receive a null result on their {@link RequestListener#onRequestSuccess(Object)} method. If something
     *            is found in cache, they will receive it in this method. If an error occurs, they will be notified via
     *            their {@link RequestListener#onRequestFailure(com.octo.android.robospice.exception.SpiceException)}
     *            method.
     */
    public < T > void getFromCache( Class< T > clazz, String requestCacheKey, long cacheDuration, RequestListener< T > requestListener ) {
        SpiceRequest< T > request = new SpiceRequest< T >( clazz ) {

            @Override
            public T loadDataFromNetwork() throws Exception {
                return null;
            }

            @Override
            public boolean isAggregatable() {
                return false;
            }
        };
        CachedSpiceRequest< T > cachedContentRequest = new CachedSpiceRequest< T >( request, requestCacheKey, cacheDuration );
        execute( cachedContentRequest, requestListener );
    }

    /**
     * Add listener to a pending request if it exists. If no such request exists, this method does nothing. If a request
     * identified by clazz and requestCacheKey, it will receive an additional listener.
     * 
     * @param clazz
     *            the class of the result of the pending request to look for.
     * @param requestCacheKey
     *            the key used to store and retrieve the result of the request in the cache
     * @param cacheDuration
     *            the time in millisecond to keep cache alive (see {@link DurationInMillis})
     * @param requestListener
     *            the listener to notify when the request will finish. If nothing is found in cache, listeners will
     *            receive a null result on their {@link RequestListener#onRequestSuccess(Object)} method. If something
     *            is found in cache, they will receive it in this method. If an error occurs, they will be notified via
     *            their {@link RequestListener#onRequestFailure(com.octo.android.robospice.exception.SpiceException)}
     *            method.
     */
    public < T > void addListenerIfPending( Class< T > clazz, String requestCacheKey, long cacheDuration, RequestListener< T > requestListener ) {
        SpiceRequest< T > request = new SpiceRequest< T >( clazz ) {

            @Override
            public T loadDataFromNetwork() throws Exception {
                return null;
            }
        };
        CachedSpiceRequest< T > cachedContentRequest = new CachedSpiceRequest< T >( request, requestCacheKey, cacheDuration );
        cachedContentRequest.setProcessable( false );
        execute( cachedContentRequest, requestListener );
    }

    /**
     * Execute a request, without using cache.
     * 
     * @param request
     *            the request to execute.
     * @param requestListener
     *            the listener to notify when the request will finish.
     */
    public < T > void execute( SpiceRequest< T > request, RequestListener< T > requestListener ) {
        CachedSpiceRequest< T > cachedContentRequest = new CachedSpiceRequest< T >( request, null, DurationInMillis.ALWAYS );
        execute( cachedContentRequest, requestListener );
    }

    /**
     * Execute a request, put the result in cache with key <i>requestCacheKey</i> during <i>cacheDuration</i>
     * millisecond and register listeners to notify when request is finished.
     * 
     * @param request
     *            the request to execute
     * @param requestCacheKey
     *            the key used to store and retrieve the result of the request in the cache
     * @param cacheDuration
     *            the time in millisecond to keep cache alive (see {@link DurationInMillis})
     * @param requestListener
     *            the listener to notify when the request will finish
     */
    public < T > void execute( SpiceRequest< T > request, String requestCacheKey, long cacheDuration, RequestListener< T > requestListener ) {
        CachedSpiceRequest< T > cachedContentRequest = new CachedSpiceRequest< T >( request, requestCacheKey, cacheDuration );
        execute( cachedContentRequest, requestListener );
    }

    /**
     * Execute a request, put the result in cache and register listeners to notify when request is finished.
     * 
     * @param request
     *            the request to execute. {@link CachedSpiceRequest} is a wrapper of {@link SpiceRequest} that contains
     *            cache key and cache duration
     * @param requestListener
     *            the listener to notify when the request will finish
     */
    public < T > void execute( CachedSpiceRequest< T > cachedContentRequest, RequestListener< T > requestListener ) {
        addRequestListenerToListOfRequestListeners( cachedContentRequest, requestListener );
        this.requestQueue.add( cachedContentRequest );
    }

    /**
     * Add listener to a pending request if it exists. If no such request exists, this method does nothing. If a request
     * identified by clazz and requestCacheKey, it will receive an additional listener.
     * 
     * @param clazz
     *            the class of the result of the pending request to look for.
     * @param requestCacheKey
     *            the key used to store and retrieve the result of the request in the cache
     * @param cacheDuration
     *            the time in millisecond to keep cache alive (see {@link DurationInMillis})
     * @param requestListener
     *            the listener to notify when the request will finish. If nothing is found in cache, listeners will
     *            receive a null result on their {@link RequestListener#onRequestSuccess(Object)} method. If something
     *            is found in cache, they will receive it in this method. If an error occurs, they will be notified via
     *            their {@link RequestListener#onRequestFailure(com.octo.android.robospice.exception.SpiceException)}
     *            method.
     */
    public < T > void cancel( Class< T > clazz, String requestCacheKey ) {
        SpiceRequest< T > request = new SpiceRequest< T >( clazz ) {

            @Override
            public T loadDataFromNetwork() throws Exception {
                return null;
            }
        };
        CachedSpiceRequest< T > cachedContentRequest = new CachedSpiceRequest< T >( request, requestCacheKey, DurationInMillis.NEVER );
        cachedContentRequest.setProcessable( false );
        cachedContentRequest.cancel();
        execute( cachedContentRequest, null );
    }

    // ============================================================================================
    // PUBLIC EXPOSED METHODS : unregister listeners
    // ============================================================================================

    /**
     * Disable request listeners notifications for a specific request.<br/>
     * None of the listeners associated to this request will be called when request will finish.<br/>
     * 
     * This method will ask (asynchronously) to the {@link SpiceService} to remove listeners if requests have already
     * been sent to the {@link SpiceService} if the request has already been sent to the service. Otherwise, it will
     * just remove listeners before passing the request to the {@link SpiceService}.
     * 
     * Calling this method doesn't prevent request from beeing executed (and put in cache) but will remove request's
     * listeners notification.
     * 
     * @param request
     *            Request for which listeners are to unregistered.
     */
    public void dontNotifyRequestListenersForRequest( final SpiceRequest< ? > request ) {
        executorService.execute( new Runnable() {
            public void run() {
                dontNotifyRequestListenersForRequestInternal( request );
            }
        } );
    }

    /**
     * Internal method to remove requests. If request has not been passed to the {@link SpiceService} yet, all listeners
     * are unregistered locally before beeing passed to the service. Otherwise, it will asynchronously ask to the
     * {@link SpiceService} to remove the listeners of the request beeing processed.
     * 
     * @param request
     *            Request for which listeners are to unregistered.
     */
    protected void dontNotifyRequestListenersForRequestInternal( final SpiceRequest< ? > request ) {
        try {
            lockSendRequestsToService.lock();

            boolean requestNotPassedToServiceYet = removeListenersOfCachedRequestToLaunch( request );
            Ln.v( "Removed from requests to launch list : " + requestNotPassedToServiceYet );

            // if the request was already passed to service, bind to service and
            // unregister listeners.
            if ( !requestNotPassedToServiceYet ) {
                removeListenersOfPendingCachedRequest( request );
                Ln.v( "Removed from pending requests list" );
            }

        } catch ( InterruptedException e ) {
            e.printStackTrace();
        } finally {
            lockSendRequestsToService.unlock();
        }
    }

    /**
     * Remove all listeners of a request that has not yet been passed to the {@link SpiceService}.
     * 
     * @param request
     *            the request for which listeners must be unregistered.
     * @return a boolean indicating if the request could be found inside the list of requests to be launched. If false,
     *         the request was already passed to the service.
     */
    private boolean removeListenersOfCachedRequestToLaunch( final SpiceRequest< ? > request ) {
        synchronized ( mapRequestToLaunchToRequestListener ) {
            for ( CachedSpiceRequest< ? > cachedContentRequest : mapRequestToLaunchToRequestListener.keySet() ) {
                if ( match( cachedContentRequest, request ) ) {
                    final Set< RequestListener< ? >> setRequestListeners = mapRequestToLaunchToRequestListener.get( cachedContentRequest );
                    setRequestListeners.clear();
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Remove all listeners of a request that may have already been passed to the {@link SpiceService}. If the request
     * has already been passed to the {@link SpiceService}, the method will bind to the service and ask it to remove
     * listeners.
     * 
     * @param request
     *            the request for which listeners must be unregistered.
     */
    private void removeListenersOfPendingCachedRequest( final SpiceRequest< ? > request ) throws InterruptedException {
        synchronized ( mapPendingRequestToRequestListener ) {
            for ( CachedSpiceRequest< ? > cachedContentRequest : mapPendingRequestToRequestListener.keySet() ) {
                if ( match( cachedContentRequest, request ) ) {
                    waitForServiceToBeBound();
                    if ( spiceService == null ) {
                        return;
                    }
                    final Set< RequestListener< ? >> setRequestListeners = mapPendingRequestToRequestListener.get( cachedContentRequest );
                    spiceService.dontNotifyRequestListenersForRequest( cachedContentRequest, setRequestListeners );
                    mapPendingRequestToRequestListener.remove( cachedContentRequest );
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
        executorService.execute( new Runnable() {
            public void run() {
                dontNotifyAnyRequestListenersInternal();
            }
        } );
    }

    /**
     * Remove all listeners of requests.
     * 
     * All requests that have not been yet passed to the service will see their of listeners cleaned. For all requests
     * that have been passed to the service, we ask the service to remove their listeners.
     */
    protected void dontNotifyAnyRequestListenersInternal() {
        try {
            lockSendRequestsToService.lock();

            mapRequestToLaunchToRequestListener.clear();
            Ln.v( "Cleared listeners of all requests to launch" );

            removeListenersOfAllPendingCachedRequests();
        } catch ( InterruptedException e ) {
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
        synchronized ( mapPendingRequestToRequestListener ) {
            if ( !mapPendingRequestToRequestListener.isEmpty() ) {
                if ( spiceService == null ) {
                    return;
                }
                for ( CachedSpiceRequest< ? > cachedContentRequest : mapPendingRequestToRequestListener.keySet() ) {

                    final Set< RequestListener< ? >> setRequestListeners = mapPendingRequestToRequestListener.get( cachedContentRequest );
                    Ln.d( "Removing listeners of request : " + cachedContentRequest.toString() + " : " + setRequestListeners.size() );
                    spiceService.dontNotifyRequestListenersForRequest( cachedContentRequest, setRequestListeners );
                }
                mapPendingRequestToRequestListener.clear();
            }
            Ln.v( "Cleared listeners of all pending requests" );
        }
    }

    /**
     * Wether or not a given {@link CachedSpiceRequest} matches a {@link SpiceRequest}.
     * 
     * @param cachedContentRequest
     *            the request know by the {@link SpiceManager}.
     * @param contentRequest
     *            the request that we wish to remove notification for.
     * @return true if {@link CachedSpiceRequest} matches contentRequest.
     */
    private boolean match( CachedSpiceRequest< ? > cachedContentRequest, SpiceRequest< ? > contentRequest ) {
        if ( contentRequest instanceof CachedSpiceRequest ) {
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
    public void cancel( final SpiceRequest< ? > request ) {
        executorService.execute( new Runnable() {
            public void run() {
                request.cancel();
            }
        } );
    }

    /**
     * Cancel all requests
     */
    public void cancelAllRequests() {
        executorService.execute( new Runnable() {
            public void run() {
                cancelAllRequestsInternal();
            }
        } );
    }

    private void cancelAllRequestsInternal() {
        try {
            lockSendRequestsToService.lock();
            // cancel each request that to be sent to service, and keep
            // listening for
            // cancellation.
            synchronized ( mapRequestToLaunchToRequestListener ) {
                for ( CachedSpiceRequest< ? > cachedContentRequest : mapRequestToLaunchToRequestListener.keySet() ) {
                    cachedContentRequest.cancel();
                }
            }

            // cancel each request that has been sent to service, and keep
            // listening for cancellation.
            // we must duplicate the list as each call to cancel will, by a listener of request processing
            // remove the request from our list.
            List< CachedSpiceRequest< ? >> listDuplicate = new ArrayList< CachedSpiceRequest< ? > >( mapPendingRequestToRequestListener.keySet() );
            for ( CachedSpiceRequest< ? > cachedContentRequest : listDuplicate ) {
                cachedContentRequest.cancel();
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
    public < T > void removeDataFromCache( final Class< T > clazz, final Object cacheKey ) {
        executorService.execute( new Runnable() {

            public void run() {
                try {
                    waitForServiceToBeBound();
                    if ( spiceService == null ) {
                        return;
                    }
                    spiceService.removeDataFromCache( clazz, cacheKey );
                } catch ( InterruptedException e ) {
                    Ln.e( e, "Interrupted while waiting for acquiring service." );
                }
            }
        } );
    }

    public < T > Future< List< Object >> getAllCacheKeys( final Class< T > clazz ) {
        return executorService.submit( new Callable< List< Object > >() {

            public List< Object > call() throws Exception {
                waitForServiceToBeBound();
                if ( spiceService == null ) {
                    return new ArrayList< Object >();
                }
                return spiceService.getAllCacheKeys( clazz );
            }
        } );
    }

    public < T > Future< List< T >> getAllDataFromCache( final Class< T > clazz ) throws CacheLoadingException {
        return executorService.submit( new Callable< List< T > >() {

            public List< T > call() throws Exception {
                waitForServiceToBeBound();
                if ( spiceService == null ) {
                    return new ArrayList< T >();
                }
                return spiceService.loadAllDataFromCache( clazz );
            }
        } );
    }

    public < T > Future< T > getDataFromCache( final Class< T > clazz, final String cacheKey ) throws CacheLoadingException {
        return executorService.submit( new Callable< T >() {

            public T call() throws Exception {
                waitForServiceToBeBound();
                if ( spiceService == null ) {
                    return null;
                }
                return spiceService.getDataFromCache( clazz, cacheKey );
            }
        } );
    }

    /**
     * Remove all data from cache. This will clear all data stored by the {@link CacheManager} of the
     * {@link SpiceService}.
     */
    public void removeAllDataFromCache() {
        executorService.execute( new Runnable() {

            public void run() {
                try {
                    waitForServiceToBeBound();
                    if ( spiceService == null ) {
                        return;
                    }
                    spiceService.removeAllDataFromCache();
                } catch ( InterruptedException e ) {
                    Ln.e( e, "Interrupted while waiting for acquiring service." );
                }
            }
        } );
    }

    /**
     * Configure the behavior in case of error during reading/writing cache. <br/>
     * Specify wether an error on reading/writing cache must fail the process.
     * 
     * @param failOnCacheError
     *            true if an error must fail the process
     */
    public void setFailOnCacheError( final boolean failOnCacheError ) {
        executorService.execute( new Runnable() {

            public void run() {
                try {
                    waitForServiceToBeBound();
                    if ( spiceService == null ) {
                        return;
                    }
                    spiceService.setFailOnCacheError( failOnCacheError );
                } catch ( InterruptedException e ) {
                    Ln.e( e, "Interrupted while waiting for acquiring service." );
                }
            }
        } );
    }

    private < T > void addRequestListenerToListOfRequestListeners( CachedSpiceRequest< T > cachedContentRequest, RequestListener< T > requestListener ) {
        synchronized ( mapRequestToLaunchToRequestListener ) {
            Set< RequestListener< ? >> listeners = mapRequestToLaunchToRequestListener.get( cachedContentRequest );
            if ( listeners == null ) {
                listeners = new HashSet< RequestListener< ? >>();
                this.mapRequestToLaunchToRequestListener.put( cachedContentRequest, listeners );
            }
            if ( !listeners.contains( requestListener ) ) {
                listeners.add( requestListener );
            }
        }

    }

    // -------------------------------
    // -------Listeners notification
    // -------------------------------

    /**
     * Dumps request processor state.
     */
    public void dumpState() {

        executorService.execute( new Runnable() {
            public void run() {
                try {
                    lockSendRequestsToService.lock();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append( "[SpiceManager : " );

                    stringBuilder.append( "Requests to be launched : \n" );
                    dumpMap( stringBuilder, mapRequestToLaunchToRequestListener );

                    stringBuilder.append( "Pending requests : \n" );
                    dumpMap( stringBuilder, mapPendingRequestToRequestListener );

                    stringBuilder.append( ']' );

                    waitForServiceToBeBound();
                    if ( spiceService == null ) {
                        return;
                    }
                    spiceService.dumpState();
                } catch ( InterruptedException e ) {
                    Ln.e( e, "Interrupted while waiting for acquiring service." );
                } finally {
                    lockSendRequestsToService.unlock();
                }
            }
        } );
    }

    // ============================================================================================
    // INNER CLASS
    // ============================================================================================

    /** Reacts to binding/unbinding with {@link SpiceService}. */
    public class SpiceServiceConnection implements ServiceConnection {

        public void onServiceConnected( ComponentName name, IBinder service ) {
            try {
                lockAcquireService.lock();

                spiceService = ( (SpiceServiceBinder) service ).getSpiceService();
                spiceService.addContentServiceListener( new RequestRemoverContentServiceListener() );
                Ln.d( "Bound to service : " + spiceService.getClass().getSimpleName() );
                conditionServiceBound.signalAll();
            } finally {
                lockAcquireService.unlock();
            }
        }

        /** Called only for unexpected unbinding. */
        public void onServiceDisconnected( ComponentName name ) {
            try {
                Ln.d( "Unbound from service start" );
                lockAcquireService.lock();
                Ln.d( "Unbound from service : " + spiceService.getClass().getSimpleName() );
                spiceService = null;
                isUnbinding = false;
                conditionServiceUnbound.signalAll();
            } finally {
                lockAcquireService.unlock();
            }
        }
    }

    /** Called when a request has been processed by the {@link SpiceService}. */
    private class RequestRemoverContentServiceListener implements SpiceServiceServiceListener {
        public void onRequestProcessed( CachedSpiceRequest< ? > contentRequest ) {
            synchronized ( mapPendingRequestToRequestListener ) {
                mapPendingRequestToRequestListener.remove( contentRequest );
            }
        }
    }

    // ============================================================================================
    // PRIVATE METHODS : SpiceService binding management.
    // ============================================================================================

    private void bindToService( Context context ) {
        if ( context == null ) {
            return;
        }
        try {
            lockAcquireService.lock();

            if ( spiceService == null ) {
                Intent intentService = new Intent( context, contentServiceClass );
                Ln.v( "Binding to service." );
                spiceServiceConnection = new SpiceServiceConnection();
                context.getApplicationContext().bindService( intentService, spiceServiceConnection, Context.BIND_AUTO_CREATE );
            }
        } finally {
            lockAcquireService.unlock();
        }
    }

    private void unbindFromService( Context context ) {
        if ( context == null ) {
            return;
        }

        try {
            Ln.v( "Unbinding from service start." );
            lockAcquireService.lock();
            if ( spiceService != null && !isUnbinding ) {
                isUnbinding = true;
                spiceService.removeContentServiceListener( removerContentServiceListener );
                Ln.v( "Unbinding from service." );
                context.getApplicationContext().unbindService( this.spiceServiceConnection );
                Ln.d( "Unbound from service : " + spiceService.getClass().getSimpleName() );
                spiceService = null;
                isUnbinding = false;
            }
        } catch ( Exception e ) {
            Ln.e( e, "Could not unbind from service." );
        } finally {
            lockAcquireService.unlock();
        }
    }

    /**
     * Wait for acquiring binding to {@link SpiceService}.
     * 
     * @throws InterruptedException
     *             in case the binding is interrupted.
     */
    protected void waitForServiceToBeBound() throws InterruptedException {
        Ln.d( "Waiting for service to be bound." );

        lockAcquireService.lock();
        try {
            while ( spiceService == null && !isStopped ) {
                conditionServiceBound.await();
            }
        } finally {
            lockAcquireService.unlock();
        }
    }

    /**
     * Wait for acquiring binding to {@link SpiceService}.
     * 
     * @throws InterruptedException
     *             in case the binding is interrupted.
     */
    protected void waitForServiceToBeUnbound() throws InterruptedException {
        Ln.d( "Waiting for service to be unbound." );

        lockAcquireService.lock();
        try {
            while ( spiceService != null ) {
                conditionServiceUnbound.await();
            }
        } finally {
            lockAcquireService.unlock();
        }
    }

    private void checkServiceIsProperlyDeclaredInAndroidManifest( Context context ) {
        Intent intentCheck = new Intent( context, contentServiceClass );
        if ( context.getPackageManager().queryIntentServices( intentCheck, 0 ).isEmpty() ) {
            shouldStop();
            throw new RuntimeException( "Impossible to start content manager as no service of class : " + contentServiceClass.getName()
                    + " is registered in AndroidManifest.xml file !" );
        }
    }

    private void dumpMap( StringBuilder stringBuilder, Map< CachedSpiceRequest< ? >, Set< RequestListener< ? >>> map ) {
        synchronized ( map ) {
            stringBuilder.append( " request count= " );
            stringBuilder.append( mapRequestToLaunchToRequestListener.keySet().size() );

            stringBuilder.append( ", listeners per requests = [" );
            for ( Map.Entry< CachedSpiceRequest< ? >, Set< RequestListener< ? >>> entry : map.entrySet() ) {
                stringBuilder.append( entry.getKey().getClass().getName() );
                stringBuilder.append( ":" );
                stringBuilder.append( entry.getKey() );
                stringBuilder.append( " --> " );
                if ( entry.getValue() == null ) {
                    stringBuilder.append( entry.getValue() );
                } else {
                    stringBuilder.append( entry.getValue().size() );
                }
                stringBuilder.append( " listeners" );
                stringBuilder.append( '\n' );
            }
            stringBuilder.append( ']' );
            stringBuilder.append( '\n' );
        }
    }
}