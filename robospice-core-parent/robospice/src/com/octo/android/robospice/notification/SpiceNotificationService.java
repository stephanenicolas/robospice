package com.octo.android.robospice.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;

public abstract class SpiceNotificationService extends Service {

    public final static String BUNDLE_KEY_NOTIFICATION_ID = "BUNDLE_KEY_NOTIFICATION_ID";
    public final static String BUNDLE_KEY_REQUEST_CACHE_KEY = "BUNDLE_KEY_REQUEST_CACHE_KEY";
    public final static String BUNDLE_KEY_REQUEST_CLASS = "BUNDLE_KEY_REQUEST_CLASS";
    public final static String BUNDLE_KEY_SERVICE_CLASS = "BUNDLE_KEY_SERVICE_CLASS";
    public final static String BUNDLE_KEY_FOREGROUND = "BUNDLE_KEY_FOREGROUND";

    private int notificationId = 70;
    private Class< ? > requestClass;
    private String requestCacheKey;
    private boolean foreground;
    private Class< ? extends SpiceService > spiceServiceClass;

    private NotificationManager notificationManager;
    private SpiceManager spiceManager;

    public static Intent createIntent( Context context, Class< ? extends SpiceNotificationService > clazz, Class< ? extends SpiceService > spiceServiceClass,
            int notificationId, Class< ? > requestResultType, String cacheKey, boolean foreground ) {
        Intent intent = new Intent( context, clazz );
        intent.putExtra( BUNDLE_KEY_NOTIFICATION_ID, notificationId );
        intent.putExtra( BUNDLE_KEY_SERVICE_CLASS, spiceServiceClass );
        intent.putExtra( BUNDLE_KEY_REQUEST_CLASS, requestResultType );
        intent.putExtra( BUNDLE_KEY_REQUEST_CACHE_KEY, cacheKey );
        intent.putExtra( BUNDLE_KEY_FOREGROUND, foreground );
        return intent;
    }

    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public final void onStart( Intent intent, int startId ) {
        super.onStart( intent, startId );
        notificationId = intent.getIntExtra( BUNDLE_KEY_NOTIFICATION_ID, 70 );
        requestClass = (Class< ? >) intent.getSerializableExtra( BUNDLE_KEY_REQUEST_CLASS );
        requestCacheKey = intent.getStringExtra( BUNDLE_KEY_REQUEST_CACHE_KEY );
        spiceServiceClass = (Class< ? extends SpiceService >) intent.getSerializableExtra( BUNDLE_KEY_SERVICE_CLASS );
        foreground = intent.getBooleanExtra( BUNDLE_KEY_FOREGROUND, true );

        spiceManager = new SpiceManager( spiceServiceClass );
        notificationManager = (NotificationManager) getSystemService( NOTIFICATION_SERVICE );
        spiceManager.start( this );
        spiceManager.addListenerIfPending( requestClass, requestCacheKey, new NotificationRequestListener() );

        if ( foreground ) {
            startForeground( startId, onCreateForegroundNotification() );
        }
    }

    @Override
    public final void onDestroy() {
        spiceManager.shouldStop();
        super.onDestroy();
    }

    public Notification onCreateForegroundNotification() {
        throw new RuntimeException( "If you use foreground = true, then you must override onCreateForegroundNotification()." );
    }

    public abstract Notification onCreateNotificationForRequestFailure( SpiceException ex );

    public abstract Notification onCreateNotificationForRequestSuccess();

    public abstract Notification onCreateNotificationForRequestProgress( RequestProgress requestProgress );

    private class NotificationRequestListener< T > implements RequestListener< T >, RequestProgressListener {

        @Override
        public void onRequestFailure( SpiceException arg0 ) {
            Notification notification = onCreateNotificationForRequestFailure( arg0 );
            notificationManager.notify( notificationId, notification );
            stopSelf();
        }

        @Override
        public void onRequestSuccess( T result ) {
            Notification notification = onCreateNotificationForRequestSuccess();
            notificationManager.notify( notificationId, notification );
            stopSelf();
        }

        @Override
        public void onRequestProgressUpdate( RequestProgress progress ) {
            Notification notification = onCreateNotificationForRequestProgress( progress );
            notificationManager.notify( notificationId, notification );

            if ( progress.getStatus() == RequestStatus.COMPLETE ) {
                stopSelf();
            }
        }

    }

}
