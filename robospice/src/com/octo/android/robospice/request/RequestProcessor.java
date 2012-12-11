package com.octo.android.robospice.request;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import roboguice.util.temp.Ln;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.SpiceServiceServiceListener;
import com.octo.android.robospice.exception.NetworkException;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.ICacheManager;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestCancellationListener;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;

/**
 * Delegate class of the {@link SpiceService}, easier to test than an Android {@link Service}. TODO make it possible to
 * set the number of threads in the {@link ExecutorService}
 * 
 * @author jva
 * 
 */
public class RequestProcessor {
    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================
    private Map< CachedSpiceRequest< ? >, Set< RequestListener< ? >>> mapRequestToRequestListener = Collections
            .synchronizedMap( new LinkedHashMap< CachedSpiceRequest< ? >, Set< RequestListener< ? >>>() );

    /**
     * Thanks Olivier Croiser from Zenika for his excellent <a href=
     * "http://blog.zenika.com/index.php?post/2012/04/11/Introduction-programmation-concurrente-Java-2sur2. " >blog
     * article</a>.
     */
    private ExecutorService executorService = null;

    private ICacheManager cacheManager;

    private Handler handlerResponse;

    private Context applicationContext;

    private boolean failOnCacheError;

    private Set< SpiceServiceServiceListener > contentServiceListenerSet;

    private RequestProcessorListener requestProcessorListener;

    private NetworkStateChecker networkStateChecker;

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================

    /**
     * Build a request processor using a custom. This feature has been implemented follwing a feature request from
     * Riccardo Ciovati.
     * 
     * @param context
     *            the context on which {@link SpiceRequest} will provide their results.
     * @param cacheManager
     *            the {@link CacheManager} that will be used to retrieve requests' result and store them.
     * @param executorService
     *            a custom {@link ExecutorService} that will be used to execute {@link SpiceRequest}.
     * @param requestProcessorListener
     *            a listener of the {@link RequestProcessor}, it will be notified when no more requests are left,
     *            typically allowing the {@link SpiceService} to stop itself.
     */
    public RequestProcessor( Context context, ICacheManager cacheManager, ExecutorService executorService,//
            RequestProcessorListener requestProcessorListener, NetworkStateChecker networkStateChecker ) {
        this.applicationContext = context;
        this.cacheManager = cacheManager;
        this.requestProcessorListener = requestProcessorListener;
        this.networkStateChecker = networkStateChecker;

        handlerResponse = new Handler( Looper.getMainLooper() );
        contentServiceListenerSet = Collections.synchronizedSet( new HashSet< SpiceServiceServiceListener >() );
        this.executorService = executorService;

        if ( !hasNetworkPermission( context ) ) {
            throw new SecurityException( "Application doesn\'t declare <uses-permission android:name=\"android.permission.INTERNET\" />" );
        }

        if ( !hasNetworkStatePermission( context ) ) {
            throw new SecurityException( "Application doesn\'t declare <uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />" );
        }
    }

    // ============================================================================================
    // PUBLIC
    // ============================================================================================
    public void addRequest( final CachedSpiceRequest< ? > request, final Set< RequestListener< ? >> listRequestListener ) {
        Ln.d( "Adding request to queue " + hashCode() + ": " + request + " size is " + mapRequestToRequestListener.size() );

        if ( request.isCancelled() ) {
            for ( CachedSpiceRequest< ? > cachedContentRequest : mapRequestToRequestListener.keySet() ) {
                if ( cachedContentRequest.equals( request ) ) {
                    cachedContentRequest.cancel();
                    return;
                }
            }
        }

        boolean aggregated = false;
        if ( listRequestListener != null ) {
            Set< RequestListener< ? >> listRequestListenerForThisRequest = mapRequestToRequestListener.get( request );

            if ( listRequestListenerForThisRequest == null ) {
                listRequestListenerForThisRequest = new HashSet< RequestListener< ? >>();
                this.mapRequestToRequestListener.put( request, listRequestListenerForThisRequest );
            } else {
                Ln.d( String.format( "Request for type %s and cacheKey %s already exists.", request.getResultType(), request.getRequestCacheKey() ) );
                aggregated = true;
            }

            listRequestListenerForThisRequest.addAll( listRequestListener );
            if ( request.isProcessable() ) {
                notifyListenersOfRequestProgress( request, listRequestListener, request.getProgress() );
            }
        }

        if ( aggregated ) {
            return;
        }

        RequestCancellationListener requestCancellationListener = new RequestCancellationListener() {

            public void onRequestCancelled() {
                mapRequestToRequestListener.remove( request );
                notifyListenersOfRequestCancellation( request, listRequestListener );
            }
        };
        request.setRequestCancellationListener( requestCancellationListener );

        Future< ? > future = executorService.submit( new Runnable() {
            public void run() {
                try {
                    processRequest( request );
                } catch ( Throwable t ) {
                    Ln.d( t, "An unexpected error occured when processsing request %s", request.toString() );

                }
            }
        } );
        request.setFuture( future );
    }

    protected < T > void processRequest( final CachedSpiceRequest< T > request ) {

        Ln.d( "Processing request : " + request );

        T result = null;
        final Set< RequestListener< ? >> listeners = mapRequestToRequestListener.get( request );

        if ( !request.isProcessable() ) {
            notifyOfRequestProcessed( request );
            return;
        }

        // add a progress listener to the request to be notified of progress during load data from network
        RequestProgressListener requestProgressListener = new RequestProgressListener() {
            public void onRequestProgressUpdate( RequestProgress progress ) {
                notifyListenersOfRequestProgress( request, listeners, progress );
            }
        };
        request.setRequestProgressListener( requestProgressListener );

        // TODO remove this
        if ( request.isCancelled() ) {
            notifyListenersOfRequestCancellation( request, listeners );
            return;
        }

        if ( request.getRequestCacheKey() != null ) {
            // First, search data in cache
            try {
                Ln.d( "Loading request from cache : " + request );
                request.setStatus( RequestStatus.READING_FROM_CACHE );
                result = loadDataFromCache( request.getResultType(), request.getRequestCacheKey(), request.getCacheDuration() );
                if ( result != null ) {
                    notifyListenersOfRequestSuccess( request, result );
                    return;
                }
            } catch ( CacheLoadingException e ) {
                Ln.d( e, "Cache file could not be read." );
                if ( failOnCacheError ) {
                    notifyListenersOfRequestFailure( request, e );
                    return;
                }
                cacheManager.removeDataFromCache( request.getResultType(), request.getRequestCacheKey() );
                Ln.d( e, "Cache file deleted." );
            }
        }

        /*
         * if ( request.isCancelled() ) { notifyListenersOfRequestCancellation( request, listeners ); return; }
         */

        if ( result == null ) {
            // if result is not in cache, load data from network
            Ln.d( "Cache content not available or expired or disabled" );
            if ( !isNetworkAvailable( applicationContext ) ) {
                Ln.e( "Network is down." );
                notifyListenersOfRequestFailure( request, new NoNetworkException() );
                return;
            }

            /*
             * if ( request.isCancelled() ) { notifyListenersOfRequestCancellation( request, listeners ); return; }
             */

            // network is ok, load data from network
            try {
                Ln.d( "Calling netwok request." );
                request.setStatus( RequestStatus.LOADING_FROM_NETWORK );
                result = request.loadDataFromNetwork();
                Ln.d( "Network request call ended." );
                /*
                 * if ( result == null ) { Ln.d( "Unable to get web service result : " + request.getResultType() );
                 * fireCacheContentRequestProcessed( request ); handlerResponse.post( new ResultRunnable(
                 * requestListeners, (T) null ) ); return; }
                 */
            } catch ( Exception e ) {
                /*
                 * if ( request.isCancelled() ) { notifyListenersOfRequestCancellation( request, listeners ); return; }
                 */
                Ln.e( e, "An exception occured during request network execution :" + e.getMessage() );
                notifyListenersOfRequestFailure( request, new NetworkException( "Exception occured during invocation of web service.", e ) );
                return;
            }

            /*
             * if ( request.isCancelled() ) { notifyListenersOfRequestCancellation( request, listeners ); return; }
             */

            if ( result != null && request.getRequestCacheKey() != null ) {
                // request worked and result is not null, save it to cache
                try {
                    Ln.d( "Start caching content..." );
                    request.setStatus( RequestStatus.WRITING_TO_CACHE );
                    result = saveDataToCacheAndReturnData( result, request.getRequestCacheKey() );
                    notifyListenersOfRequestSuccess( request, result );
                    return;
                } catch ( CacheSavingException e ) {
                    Ln.d( "An exception occured during service execution :" + e.getMessage(), e );
                    if ( failOnCacheError ) {
                        notifyListenersOfRequestFailure( request, e );
                        return;
                    } else {
                        // result can't be saved to cache but we reached that point after a success of load data from
                        // network
                        notifyListenersOfRequestSuccess( request, result );
                    }
                    cacheManager.removeDataFromCache( request.getResultType(), request.getRequestCacheKey() );
                    Ln.d( e, "Cache file deleted." );
                }
            } else {
                // result can't be saved to cache but we reached that point after a success of load data from network
                notifyListenersOfRequestSuccess( request, result );
                return;
            }
        }
    }

    private void post( Runnable r, Object token ) {
        handlerResponse.postAtTime( r, token, 0 );
    }

    private < T > void notifyListenersOfRequestProgress( CachedSpiceRequest< ? > request, Set< RequestListener< ? >> listeners, RequestStatus status ) {
        notifyListenersOfRequestProgress( request, listeners, new RequestProgress( status ) );
    }

    private < T > void notifyListenersOfRequestProgress( CachedSpiceRequest< ? > request, Set< RequestListener< ? >> listeners, RequestProgress progress ) {
        post( new ProgressRunnable( listeners, progress ), request.getRequestCacheKey() );
        checkAllRequestComplete();
    }

    private void checkAllRequestComplete() {
        if ( mapRequestToRequestListener.isEmpty() ) {
            requestProcessorListener.allRequestComplete();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private < T > void notifyListenersOfRequestSuccess( CachedSpiceRequest< T > request, T result ) {
        final Set< RequestListener< ? >> listeners = mapRequestToRequestListener.get( request );
        notifyListenersOfRequestProgress( request, listeners, RequestStatus.COMPLETE );
        post( new ResultRunnable( listeners, result ), request.getRequestCacheKey() );
        notifyOfRequestProcessed( request );
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private < T > void notifyListenersOfRequestFailure( CachedSpiceRequest< T > request, SpiceException e ) {
        final Set< RequestListener< ? >> listeners = mapRequestToRequestListener.get( request );
        notifyListenersOfRequestProgress( request, listeners, RequestStatus.COMPLETE );
        post( new ResultRunnable( listeners, e ), request.getRequestCacheKey() );
        notifyOfRequestProcessed( request );
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void notifyListenersOfRequestCancellation( CachedSpiceRequest< ? > request, Set< RequestListener< ? >> listeners ) {
        Ln.d( "Not calling network request : " + request + " as it is cancelled. " );
        notifyListenersOfRequestProgress( request, listeners, RequestStatus.COMPLETE );
        post( new ResultRunnable( listeners, new RequestCancelledException( "Request has been cancelled explicitely." ) ), request.getRequestCacheKey() );
        notifyOfRequestProcessed( request );
    }

    /**
     * Disable request listeners notifications for a specific request.<br/>
     * All listeners associated to this request won't be called when request will finish.<br/>
     * Should be called in {@link Activity#onPause}
     * 
     * @param request
     *            Request on which you want to disable listeners
     * @param listRequestListener
     *            the collection of listeners associated to request not to be notified
     */
    public void dontNotifyRequestListenersForRequest( CachedSpiceRequest< ? > request, Collection< RequestListener< ? >> listRequestListener ) {
        handlerResponse.removeCallbacksAndMessages( request.getRequestCacheKey() );
        // Ouh that hurts, Release 1.3.0 fails, it doesn't have any request in map any more
        // TODO
        Set< RequestListener< ? >> setRequestListener = mapRequestToRequestListener.get( request );
        if ( setRequestListener != null && listRequestListener != null ) {
            Ln.d( "Removing listeners of request : " + request.toString() + " : " + setRequestListener.size() );
            setRequestListener.removeAll( listRequestListener );
        }
    }

    /**
     * @return true if network is available.
     */
    public boolean isNetworkAvailable( Context context ) {
        return networkStateChecker.isNetworkAvailable( context );
    }

    public static boolean hasNetworkStatePermission( Context context ) {
        return context.getPackageManager().checkPermission( "android.permission.ACCESS_NETWORK_STATE", context.getPackageName() ) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasNetworkPermission( Context context ) {
        return context.getPackageManager().checkPermission( "android.permission.INTERNET", context.getPackageName() ) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean removeDataFromCache( Class< ? > clazz, Object cacheKey ) {
        return cacheManager.removeDataFromCache( clazz, cacheKey );
    }

    public void removeAllDataFromCache( Class< ? > clazz ) {
        cacheManager.removeAllDataFromCache( clazz );
    }

    public void removeAllDataFromCache() {
        cacheManager.removeAllDataFromCache();
    }

    public boolean isFailOnCacheError() {
        return failOnCacheError;
    }

    public void setFailOnCacheError( boolean failOnCacheError ) {
        this.failOnCacheError = failOnCacheError;
    }

    // ============================================================================================
    // PRIVATE
    // ============================================================================================

    private < T > T loadDataFromCache( Class< T > clazz, Object cacheKey, long maxTimeInCacheBeforeExpiry ) throws CacheLoadingException {
        return cacheManager.loadDataFromCache( clazz, cacheKey, maxTimeInCacheBeforeExpiry );
    }

    private < T > T saveDataToCacheAndReturnData( T data, Object cacheKey ) throws CacheSavingException {
        return cacheManager.saveDataToCacheAndReturnData( data, cacheKey );
    }

    private class ProgressRunnable implements Runnable {
        private RequestProgress progress;
        private Set< RequestListener< ? >> listeners;

        public ProgressRunnable( Set< RequestListener< ? >> listeners, RequestProgress progress ) {
            this.progress = progress;
            this.listeners = listeners;
        }

        public void run() {

            if ( listeners == null ) {
                return;
            }

            Ln.v( "Notifying " + listeners.size() + " listeners of progress " + progress );
            for ( RequestListener< ? > listener : listeners ) {
                if ( listener instanceof RequestProgressListener ) {
                    Ln.v( "Notifying %s", listener.getClass().getSimpleName() );
                    ( (RequestProgressListener) listener ).onRequestProgressUpdate( progress );
                }
            }
        }
    }

    private class ResultRunnable< T > implements Runnable {

        private SpiceException spiceException;
        private T result;
        private Set< RequestListener< ? >> listeners;

        public ResultRunnable( Set< RequestListener< ? >> listeners, T result ) {
            this.result = result;
            this.listeners = listeners;
        }

        public ResultRunnable( Set< RequestListener< ? >> listeners, SpiceException spiceException ) {
            this.spiceException = spiceException;
            this.listeners = listeners;
        }

        public void run() {
            if ( listeners == null ) {
                return;
            }

            String resultMsg = spiceException == null ? "success" : "failure";
            Ln.v( "Notifying " + listeners.size() + " listeners of request " + resultMsg );
            for ( RequestListener< ? > listener : listeners ) {
                @SuppressWarnings("unchecked")
                RequestListener< T > listener2 = (RequestListener< T >) listener;
                Ln.v( "Notifying %s", listener.getClass().getSimpleName() );
                if ( spiceException == null ) {
                    listener2.onRequestSuccess( result );
                } else {
                    listener.onRequestFailure( spiceException );
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append( '[' );
        stringBuilder.append( getClass().getName() );
        stringBuilder.append( " : " );

        stringBuilder.append( " request count= " );
        stringBuilder.append( mapRequestToRequestListener.keySet().size() );

        stringBuilder.append( ", listeners per requests = [" );
        for ( Map.Entry< CachedSpiceRequest< ? >, Set< RequestListener< ? >>> entry : mapRequestToRequestListener.entrySet() ) {
            stringBuilder.append( entry.getKey().getClass().getName() );
            stringBuilder.append( ":" );
            stringBuilder.append( entry.getKey() );
            stringBuilder.append( " --> " );
            if ( entry.getValue() == null ) {
                stringBuilder.append( entry.getValue() );
            } else {
                stringBuilder.append( entry.getValue().size() );
            }
        }
        stringBuilder.append( ']' );

        stringBuilder.append( ']' );
        return stringBuilder.toString();
    }

    public void addContentServiceListener( SpiceServiceServiceListener spiceServiceServiceListener ) {
        this.contentServiceListenerSet.add( spiceServiceServiceListener );
    }

    public void removeContentServiceListener( SpiceServiceServiceListener spiceServiceServiceListener ) {
        this.contentServiceListenerSet.add( spiceServiceServiceListener );
    }

    protected void notifyOfRequestProcessed( CachedSpiceRequest< ? > request ) {
        Ln.v( "Removing %s  size is %d", request, mapRequestToRequestListener.size() );
        mapRequestToRequestListener.remove( request );

        checkAllRequestComplete();
        synchronized ( contentServiceListenerSet ) {
            for ( SpiceServiceServiceListener spiceServiceServiceListener : contentServiceListenerSet ) {
                spiceServiceServiceListener.onRequestProcessed( request );
            }
        }
    }
}
