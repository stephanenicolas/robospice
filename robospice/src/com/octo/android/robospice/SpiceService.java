package com.octo.android.robospice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import roboguice.util.temp.Ln;
import android.app.Application;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.octo.android.robospice.networkstate.DefaultNetworkStateChecker;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.RequestProcessor;
import com.octo.android.robospice.request.RequestProcessorListener;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * This is an abstract class used to manage the cache and provide web service result to an activity. <br/>
 * 
 * Extends this class to provide a service able to load content from web service or cache (if available and enabled).
 * You will have to implement {@link #createCacheManager(Application)} to configure the {@link CacheManager} used by all
 * requests to persist their results in the cache (and load them from cache if possible).
 * 
 * @author jva
 * @author mwa
 * @author sni
 */
public abstract class SpiceService extends Service {

    private static final int NOTIFICATION_ID = 42;

    private static final int DEFAULT_THREAD_COUNT = 1;
    private static final boolean DEFAULT_FAIL_ON_CACHE_ERROR = false;

    private static boolean isStarted;

    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================
    public SpiceServiceBinder mSpiceServiceBinder;

    /** Responsible for persisting data. */

    private RequestProcessor requestProcessor;

    private int currentPendingRequestCount = 0;

    private boolean isBound;

    private Notification notification;

    private CacheManager cacheManager;

    private SelfStopperRequestProcessorListener requestProcessorListener = new SelfStopperRequestProcessorListener();

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================
    /**
     * Basic constructor
     * 
     * @param name
     */
    public SpiceService() {
        mSpiceServiceBinder = new SpiceServiceBinder( this );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;

        cacheManager = createCacheManager( getApplication() );
        ExecutorService executorService = getExecutorService();
        NetworkStateChecker networkStateChecker = getNetworkStateChecker();

        requestProcessor = new RequestProcessor( getApplicationContext(), cacheManager, executorService, requestProcessorListener, networkStateChecker );
        requestProcessor.setFailOnCacheError( DEFAULT_FAIL_ON_CACHE_ERROR );

        notification = createDefaultNotification();
        startForeground( notification );

        Ln.d( "Content Service instance created." );
    }

    /**
     * Factory method to create an entity responsible to check for network state. The default implementation of this
     * method will return a {@link DefaultNetworkStateChecker}. Override this method if you want to inject a custom
     * network state for testing or to adapt to connectivity changes on the Android.
     * 
     * This method is also useful to create non-network related requests. In that case create a
     * {@link NetworkStateChecker} that always return true. This feature has been implemented following a request from
     * Pierre Durand.
     * 
     * @return a {@link NetworkStateChecker} that will be used to determine if network state allows requests executions.
     */
    protected NetworkStateChecker getNetworkStateChecker() {
        return new DefaultNetworkStateChecker();
    }

    /**
     * Factory method to create an {@link ExecutorService} that will be used to execute {@link SpiceRequest} instances.
     * The default implementation of this method will create a single threaded or multi-threaded {@link ExecutorService}
     * depending on the number of threads returned by {@link #getThreadCount()}. If you override this method in your
     * service, you can supply a custom {@link ExecutorService}. This feature has been implemented following a request
     * from Riccardo Ciovati.
     * 
     * @return the {@link ExecutorService} to be used to execute {@link SpiceRequest} instances.
     */
    protected ExecutorService getExecutorService() {
        ExecutorService executorService;
        int threadCount = getThreadCount();
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
        return executorService;
    }

    public static Notification createDefaultNotification() {
        Notification note = new Notification( 0, null, System.currentTimeMillis() );
        note.flags |= Notification.FLAG_NO_CLEAR;
        return note;
    }

    @Override
    public void onDestroy() {
        Ln.d( "Content Service instance destroyed." );
        super.onDestroy();
        isStarted = false;
    }

    public static boolean isStarted() {
        return isStarted;
    }

    // ============================================================================================
    // DELEGATE METHODS (delegation is used to ease tests)
    // ============================================================================================

    public abstract CacheManager createCacheManager( Application application );

    public int getThreadCount() {
        return DEFAULT_THREAD_COUNT;
    }

    public void addRequest( final CachedSpiceRequest< ? > request, Set< RequestListener< ? >> listRequestListener ) {
        currentPendingRequestCount++;
        requestProcessor.addRequest( request, listRequestListener );
    }

    public boolean removeDataFromCache( Class< ? > clazz, Object cacheKey ) {
        return requestProcessor.removeDataFromCache( clazz, cacheKey );
    }

    public void removeAllDataFromCache( Class< ? > clazz ) {
        requestProcessor.removeAllDataFromCache( clazz );
    }

    public < T > List< Object > getAllCacheKeys( Class< T > clazz ) {
        return cacheManager.getAllCacheKeys( clazz );
    }

    public < T > List< T > loadAllDataFromCache( Class< T > clazz ) throws CacheLoadingException {
        return cacheManager.loadAllDataFromCache( clazz );
    }

    public < T > T getDataFromCache( Class< T > clazz, String cacheKey ) throws CacheLoadingException {
        return cacheManager.loadDataFromCache( clazz, cacheKey, DurationInMillis.ALWAYS );
    }

    public void removeAllDataFromCache() {
        requestProcessor.removeAllDataFromCache();
    }

    public boolean isFailOnCacheError() {
        return requestProcessor.isFailOnCacheError();
    }

    public void setFailOnCacheError( boolean failOnCacheError ) {
        requestProcessor.setFailOnCacheError( failOnCacheError );
    }

    public void dontNotifyRequestListenersForRequest( CachedSpiceRequest< ? > request, Collection< RequestListener< ? >> listRequestListener ) {
        requestProcessor.dontNotifyRequestListenersForRequest( request, listRequestListener );
    }

    // ============================================================================================
    // SERVICE METHODS
    // ============================================================================================

    @Override
    public IBinder onBind( Intent intent ) {
        isBound = true;
        return mSpiceServiceBinder;
    }

    @Override
    public boolean onUnbind( Intent intent ) {
        boolean result = super.onUnbind( intent );
        isBound = false;
        stopIfNotBoundAndHasNoPendingRequests();
        return result;
    }

    private final class SelfStopperRequestProcessorListener implements RequestProcessorListener {
        public void allRequestComplete() {
            currentPendingRequestCount = 0;
            stopIfNotBoundAndHasNoPendingRequests();
        }
    }

    public static class SpiceServiceBinder extends Binder {
        private SpiceService spiceService;

        public SpiceServiceBinder( SpiceService spiceService ) {
            this.spiceService = spiceService;
        }

        public SpiceService getSpiceService() {
            return spiceService;
        }
    }

    public void dumpState() {
        Ln.v( requestProcessor.toString() );
    }

    public void addContentServiceListener( SpiceServiceServiceListener spiceServiceServiceListener ) {
        requestProcessor.addContentServiceListener( spiceServiceServiceListener );
    }

    public void removeContentServiceListener( SpiceServiceServiceListener spiceServiceServiceListener ) {
        requestProcessor.removeContentServiceListener( spiceServiceServiceListener );
    }

    private void stopIfNotBoundAndHasNoPendingRequests() {
        Ln.v( "Pending requests : " + currentPendingRequestCount );
        if ( currentPendingRequestCount == 0 && !isBound ) {
            stopSelf();
        }
    }

    // There is a bug in ServiceTestCase : a call to setForeground will fail
    // http://code.google.com/p/android/issues/detail?id=12122
    private void startForeground( Notification notification ) {
        try {
            Method setForegroundMethod = Service.class.getMethod( "startForeground", int.class, Notification.class );
            setForegroundMethod.invoke( this, NOTIFICATION_ID, notification );
        } catch ( SecurityException e ) {
            Ln.e( e, "Unable to start a service in foreground" );
        } catch ( NoSuchMethodException e ) {
            Ln.e( e, "Unable to start a service in foreground" );
        } catch ( IllegalArgumentException e ) {
            Ln.e( e, "Unable to start a service in foreground" );
        } catch ( IllegalAccessException e ) {
            Ln.e( e, "Unable to start a service in foreground" );
        } catch ( InvocationTargetException e ) {
            Ln.e( e, "Unable to start a service in foreground" );
        }
    }

}