package com.octo.android.robospice.request;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import roboguice.util.temp.Ln;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.SpiceServiceServiceListener;
import com.octo.android.robospice.exception.NetworkException;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.ICacheManager;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.exception.SpiceException;
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

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================

    /**
     * Build a request processor using a default {@link ExecutorService}.
     * 
     * @param context
     *            the context on which {@link SpiceRequest} will provide their results.
     * @param cacheManager
     *            the {@link CacheManager} that will be used to retrieve requests' result and store them.
     * @param threadCount
     *            the number of thread that will be used to execute {@link SpiceRequest}.
     * @param requestProcessorListener
     *            a listener of the {@link RequestProcessor}, it will be notified when no more requests are left,
     *            typically allowing the {@link SpiceService} to stop itself.
     */
    public RequestProcessor( Context context, ICacheManager cacheManager, int threadCount, RequestProcessorListener requestProcessorListener ) {
        this( context, cacheManager, null, requestProcessorListener );
        initiateExecutorService( threadCount );
    }

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
    public RequestProcessor( Context context, ICacheManager cacheManager, ExecutorService executorService, RequestProcessorListener requestProcessorListener ) {
        this.applicationContext = context;
        this.cacheManager = cacheManager;
        this.requestProcessorListener = requestProcessorListener;

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

    protected void initiateExecutorService( int threadCount ) {
        if ( threadCount <= 0 ) {
            throw new IllegalArgumentException( "Thread count must be >= 1" );
        } else if ( threadCount == 1 ) {
            executorService = Executors.newSingleThreadExecutor();
        } else {
            executorService = Executors.newFixedThreadPool( threadCount, new ThreadFactory() {

                public Thread newThread( Runnable r ) {
                    return new Thread( r );
                }
            } );
        }
    }

    // ============================================================================================
    // PUBLIC
    // ============================================================================================
    public void addRequest( final CachedSpiceRequest< ? > request, Set< RequestListener< ? >> listRequestListener ) {
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
            notifyListenersOfRequestProgress( listRequestListener, request.getProgress() );
        }

        if ( aggregated ) {
            return;
        }

        Future< ? > future = executorService.submit( new Runnable() {
            public void run() {
                processRequest( request );
            }
        } );
        request.setFuture( future );
    }

    protected < T > void processRequest( CachedSpiceRequest< T > request ) {

        if ( !request.isProcessable() ) {
            notifyOfRequestProcessed( request );
            return;
        }

        Ln.d( "Processing request : " + request );

        T result = null;
        final Set< RequestListener< ? >> requestListeners = mapRequestToRequestListener.get( request );

        // add a progress listener to the request to be notified of progress during load data from network
        RequestProgressListener requestProgressListener = new RequestProgressListener() {
            public void onRequestProgressUpdate( RequestProgress progress ) {
                notifyListenersOfRequestProgress( requestListeners, progress );
            }
        };
        request.setRequestProgressListener( requestProgressListener );

        if ( request.isCancelled() ) {
            notifyListenersOfRequestCancellation( request, requestListeners );
            return;
        }

        if ( request.getRequestCacheKey() != null ) {
            // First, search data in cache
            try {
                Ln.d( "Loading request from cache : " + request );
                request.setStatus( RequestStatus.READING_FROM_CACHE );
                result = loadDataFromCache( request.getResultType(), request.getRequestCacheKey(), request.getCacheDuration() );
                if ( result != null ) {
                    notifyListenersOfRequestSuccess( request, result, requestListeners );
                    return;
                }
            } catch ( CacheLoadingException e ) {
                Ln.d( e, "Cache file could not be read." );
                if ( failOnCacheError ) {
                    notifyListenersOfRequestFailure( request, requestListeners, e );
                    return;
                }
            }
        }

        if ( request.isCancelled() ) {
            notifyListenersOfRequestCancellation( request, requestListeners );
            return;
        }

        if ( result == null && !request.isCancelled() ) {
            // if result is not in cache, load data from network
            Ln.d( "Cache content not available or expired or disabled" );
            if ( !isNetworkAvailable( applicationContext ) ) {
                Ln.e( "Network is down." );
                notifyListenersOfRequestFailure( request, requestListeners, new NoNetworkException() );
                return;
            }

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
                if ( request.isCancelled() ) {
                    notifyListenersOfRequestCancellation( request, requestListeners );
                    return;
                }
                Ln.e( e, "An exception occured during request network execution :" + e.getMessage() );
                notifyListenersOfRequestFailure( request, requestListeners, new NetworkException( "Exception occured during invocation of web service.", e ) );
                return;
            }

            if ( request.isCancelled() ) {
                notifyListenersOfRequestCancellation( request, requestListeners );
                return;
            }

            if ( result != null && request.getRequestCacheKey() != null ) {
                // request worked and result is not null, save it to cache
                try {
                    Ln.d( "Start caching content..." );
                    request.setStatus( RequestStatus.WRITING_TO_CACHE );
                    result = saveDataToCacheAndReturnData( result, request.getRequestCacheKey() );
                    notifyListenersOfRequestSuccess( request, result, requestListeners );
                    return;
                } catch ( CacheSavingException e ) {
                    Ln.d( "An exception occured during service execution :" + e.getMessage(), e );
                    if ( failOnCacheError ) {
                        notifyListenersOfRequestFailure( request, requestListeners, e );
                        return;
                    } else {
                        // result can't be saved to cache but we reached that point after a success of load data from
                        // network
                        notifyListenersOfRequestSuccess( request, result, requestListeners );
                    }
                }
            } else {
                // result can't be saved to cache but we reached that point after a success of load data from network
                notifyListenersOfRequestSuccess( request, result, requestListeners );
                return;
            }
        }
    }

    private < T > void notifyListenersOfRequestProgress( final Set< RequestListener< ? >> requestListeners, RequestStatus status ) {
        notifyListenersOfRequestProgress( requestListeners, new RequestProgress( status ) );
    }

    private < T > void notifyListenersOfRequestProgress( final Set< RequestListener< ? >> requestListeners, RequestProgress progress ) {
        handlerResponse.post( new ProgressRunnable( requestListeners, progress ) );
        checkAllRequestComplete();
    }

    private void checkAllRequestComplete() {
        if ( mapRequestToRequestListener.isEmpty() ) {
            requestProcessorListener.allRequestComplete();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private < T > void notifyListenersOfRequestSuccess( CachedSpiceRequest< T > request, T result, final Set< RequestListener< ? >> requestListeners ) {
        notifyOfRequestProcessed( request );
        handlerResponse.post( new ResultRunnable( requestListeners, result ) );
        notifyListenersOfRequestProgress( requestListeners, RequestStatus.COMPLETE );
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private < T > void notifyListenersOfRequestFailure( CachedSpiceRequest< T > request, final Set< RequestListener< ? >> requestListeners, SpiceException e ) {
        notifyOfRequestProcessed( request );
        handlerResponse.post( new ResultRunnable( requestListeners, e ) );
        notifyListenersOfRequestProgress( requestListeners, RequestStatus.COMPLETE );
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void notifyListenersOfRequestCancellation( CachedSpiceRequest< ? > request, final Set< RequestListener< ? >> requestListeners ) {
        Ln.d( "Not calling network request : " + request + " as it is cancelled. " );
        notifyOfRequestProcessed( request );
        notifyListenersOfRequestProgress( requestListeners, RequestStatus.COMPLETE );
        handlerResponse.post( new ResultRunnable( requestListeners, new RequestCancelledException( "Request has been cancelled explicitely." ) ) );
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
        Set< RequestListener< ? >> setRequestListener = mapRequestToRequestListener.get( request );
        if ( setRequestListener != null && listRequestListener != null ) {
            setRequestListener.removeAll( listRequestListener );
        }
    }

    /**
     * @return true if network is available (at least one way to connect to network is connected or connecting).
     */
    public static boolean isNetworkAvailable( Context context ) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo[] allNetworkInfos = connectivityManager.getAllNetworkInfo();
        for ( NetworkInfo networkInfo : allNetworkInfos ) {
            if ( networkInfo.getState() == NetworkInfo.State.CONNECTED || networkInfo.getState() == NetworkInfo.State.CONNECTING ) {
                return true;
            }
        }
        return false;
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
        private Set< RequestListener< ? >> listeners;
        private RequestProgress progress;

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
        private Set< RequestListener< T >> listeners;

        public ResultRunnable( Set< RequestListener< T >> listeners, T result ) {
            this.result = result;
            this.listeners = listeners;
        }

        public ResultRunnable( Set< RequestListener< T >> listeners, SpiceException spiceException ) {
            this.listeners = listeners;
            this.spiceException = spiceException;
        }

        public void run() {
            if ( listeners == null ) {
                return;
            }

            String resultMsg = spiceException == null ? "success" : "failure";
            Ln.v( "Notifying " + listeners.size() + " listeners of request " + resultMsg );
            for ( RequestListener< T > listener : listeners ) {
                Ln.v( "Notifying %s", listener.getClass().getSimpleName() );
                if ( spiceException == null ) {
                    listener.onRequestSuccess( result );
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
        mapRequestToRequestListener.remove( request );
        checkAllRequestComplete();
        for ( SpiceServiceServiceListener spiceServiceServiceListener : contentServiceListenerSet ) {
            spiceServiceServiceListener.onRequestProcessed( request );
        }
    }
}
