package com.octo.android.robospice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import android.app.Application;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.RequestProcessor;
import com.octo.android.robospice.request.RequestProcessorListener;
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

    private final static String LOG_CAT = "SpiceService";

    private static final int DEFAULT_THREAD_COUNT = 1;
    private static final boolean DEFAULT_FAIL_ON_CACHE_ERROR = false;

    private static boolean isStarted;

    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================
    public ContentServiceBinder mContentServiceBinder;

    /** Responsible for persisting data. */

    private RequestProcessor requestProcessor;

    private int currentPendingRequestCount = 0;

    private boolean isBound;

    private Notification notification;

    private CacheManager cacheManager;

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================
    /**
     * Basic constructor
     * 
     * @param name
     */
    public SpiceService() {
        mContentServiceBinder = new ContentServiceBinder( this );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;

        cacheManager = createCacheManager( getApplication() );
        requestProcessor = new RequestProcessor( getApplicationContext(), cacheManager, getThreadCount(), new RequestProcessorListener() {

            public void allRequestComplete() {
                currentPendingRequestCount = 0;
                stopIfNotBoundAndHasNoPendingRequests();
            }
        } );
        requestProcessor.setFailOnCacheError( DEFAULT_FAIL_ON_CACHE_ERROR );

        notification = createDefaultNotification();
        startForeground( notification );

        Log.d( LOG_CAT, "Content Service instance created." );
    }

    public static Notification createDefaultNotification() {
        Notification note = new Notification( 0, null, System.currentTimeMillis() );
        note.flags |= Notification.FLAG_NO_CLEAR;
        return note;
    }

    @Override
    public void onDestroy() {
        Log.d( LOG_CAT, "Content Service instance destroyed." );
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
        return mContentServiceBinder;
    }

    @Override
    public boolean onUnbind( Intent intent ) {
        boolean result = super.onUnbind( intent );
        isBound = false;
        stopIfNotBoundAndHasNoPendingRequests();
        return result;
    }

    public static class ContentServiceBinder extends Binder {
        private SpiceService spiceService;

        public ContentServiceBinder( SpiceService spiceService ) {
            this.spiceService = spiceService;
        }

        public SpiceService getContentService() {
            return spiceService;
        }
    }

    public void dumpState() {
        Log.v( LOG_CAT, requestProcessor.toString() );
    }

    public void addContentServiceListener( SpiceServiceServiceListener spiceServiceServiceListener ) {
        requestProcessor.addContentServiceListener( spiceServiceServiceListener );
    }

    public void removeContentServiceListener( SpiceServiceServiceListener spiceServiceServiceListener ) {
        requestProcessor.removeContentServiceListener( spiceServiceServiceListener );
    }

    private void stopIfNotBoundAndHasNoPendingRequests() {
        Log.v( LOG_CAT, "Pending requests : " + currentPendingRequestCount );
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
            Log.e( LOG_CAT, "Unable to start a service in foreground", e );
        } catch ( NoSuchMethodException e ) {
            Log.e( LOG_CAT, "Unable to start a service in foreground", e );
        } catch ( IllegalArgumentException e ) {
            Log.e( LOG_CAT, "Unable to start a service in foreground", e );
        } catch ( IllegalAccessException e ) {
            Log.e( LOG_CAT, "Unable to start a service in foreground", e );
        } catch ( InvocationTargetException e ) {
            Log.e( LOG_CAT, "Unable to start a service in foreground", e );
        }
    }

}