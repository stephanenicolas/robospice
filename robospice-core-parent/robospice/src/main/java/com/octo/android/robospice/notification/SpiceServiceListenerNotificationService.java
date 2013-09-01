package com.octo.android.robospice.notification;

import roboguice.util.temp.Ln;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.SpiceServiceListener;
import com.octo.android.robospice.request.listener.SpiceServiceListener.RequestProcessingContext;

/**
 * Will display updates in the status of a given spice service itself by
 * displaying notifications when the service processes a request, updates its
 * progress, etc. This service will need to be started via an intent and will be
 * stopped automatically when the target spice service stops.
 * @author SNI
 */
public abstract class SpiceServiceListenerNotificationService extends Service {

    public static final int DEFAULT_ROBOSPICE_NOTIFICATION_ID = 700;
    public static final String BUNDLE_KEY_NOTIFICATION_ID = "BUNDLE_KEY_NOTIFICATION_ID";
    public static final String BUNDLE_KEY_REQUEST_CACHE_KEY = "BUNDLE_KEY_REQUEST_CACHE_KEY";
    public static final String BUNDLE_KEY_REQUEST_CLASS = "BUNDLE_KEY_REQUEST_CLASS";
    public static final String BUNDLE_KEY_SERVICE_CLASS = "BUNDLE_KEY_SERVICE_CLASS";
    public static final String BUNDLE_KEY_FOREGROUND = "BUNDLE_KEY_FOREGROUND";

    private int notificationId = DEFAULT_ROBOSPICE_NOTIFICATION_ID;
    private boolean foreground;
    private Class<? extends SpiceService> spiceServiceClass;

    private NotificationManager notificationManager;
    private SpiceManager spiceManager;

    public static Intent createIntent(final Context context, final Class<? extends SpiceServiceListenerNotificationService> clazz, final Class<? extends SpiceService> spiceServiceClass,
        final int notificationId, final boolean foreground) {
        final Intent intent = new Intent(context, clazz);
        intent.putExtra(BUNDLE_KEY_NOTIFICATION_ID, notificationId);
        intent.putExtra(BUNDLE_KEY_SERVICE_CLASS, spiceServiceClass);
        intent.putExtra(BUNDLE_KEY_FOREGROUND, foreground);
        return intent;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    public Class<? extends SpiceService> getSpiceServiceClass() {
        return spiceServiceClass;
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    @Override
    public final void onStart(final Intent intent, final int startId) {
        super.onStart(intent, startId);
        if (intent == null) {
            return;
        }
        notificationId = intent.getIntExtra(BUNDLE_KEY_NOTIFICATION_ID, DEFAULT_ROBOSPICE_NOTIFICATION_ID);
        spiceServiceClass = (Class<? extends SpiceService>) intent.getSerializableExtra(BUNDLE_KEY_SERVICE_CLASS);

        if (spiceServiceClass == null) {
            throw new RuntimeException("Please specify a service class to monitor. Use #createIntent as helper.");
        }
        foreground = intent.getBooleanExtra(BUNDLE_KEY_FOREGROUND, true);

        spiceManager = new SpiceManager(spiceServiceClass);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        spiceManager.start(this);
        spiceManager.addSpiceServiceListener(new NotificationSpiceServiceListener());

        if (foreground) {
            startForeground(notificationId, onCreateForegroundNotification());
        }
        Ln.d(getClass().getSimpleName() + " started.");
    }

    @Override
    public final void onDestroy() {
        spiceManager.shouldStop();
        super.onDestroy();
    }

    public Notification onCreateForegroundNotification() {
        throw new RuntimeException("If you use foreground = true, then you must override onCreateForegroundNotification().");
    }

    public abstract SpiceNotification onCreateNotificationForServiceStopped();

    public abstract SpiceNotification onCreateNotificationForRequestSucceeded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

    public abstract SpiceNotification onCreateNotificationForRequestCancelled(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

    public abstract SpiceNotification onCreateNotificationForRequestFailed(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

    public abstract SpiceNotification onCreateNotificationForRequestProgressUpdate(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

    public abstract SpiceNotification onCreateNotificationForRequestAdded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

    public abstract SpiceNotification onCreateNotificationForRequestAggregated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

    public abstract SpiceNotification onCreateNotificationForRequestNotFound(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext);

    public abstract SpiceNotification onCreateNotificationForRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext);

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------

    public static class SpiceNotification {
        private int id;
        private Notification notification;

        public SpiceNotification(int id, Notification notification) {
            this.id = id;
            this.notification = notification;
        }

        public int getId() {
            return id;
        }

        public Notification getNotification() {
            return notification;
        }
    }

    public class NotificationSpiceServiceListener implements SpiceServiceListener {

        @Override
        public void onRequestSucceeded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            final SpiceNotification notification = onCreateNotificationForRequestSucceeded(request, requestProcessingContext);
            notificationManager.notify(notification.getId(), notification.getNotification());
        }

        @Override
        public void onRequestFailed(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            final SpiceNotification notification = onCreateNotificationForRequestFailed(request, requestProcessingContext);
            notificationManager.notify(notification.getId(), notification.getNotification());
        }

        @Override
        public void onRequestCancelled(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            final SpiceNotification notification = onCreateNotificationForRequestCancelled(request, requestProcessingContext);
            notificationManager.notify(notification.getId(), notification.getNotification());
        }

        @Override
        public void onRequestProgressUpdated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            final SpiceNotification notification = onCreateNotificationForRequestProgressUpdate(request, requestProcessingContext);
            notificationManager.notify(notification.getId(), notification.getNotification());
        }

        @Override
        public void onRequestAdded(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            final SpiceNotification notification = onCreateNotificationForRequestAdded(request, requestProcessingContext);
            notificationManager.notify(notification.getId(), notification.getNotification());
        }
        
        @Override
        public void onRequestAggregated(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            final SpiceNotification notification = onCreateNotificationForRequestAdded(request, requestProcessingContext);
            notificationManager.notify(notification.getId(), notification.getNotification());
        }

        @Override
        public void onRequestNotFound(CachedSpiceRequest<?> request, RequestProcessingContext requestProcessingContext) {
            final SpiceNotification notification = onCreateNotificationForRequestNotFound(request, requestProcessingContext);
            notificationManager.notify(notification.getId(), notification.getNotification());
        }

        @Override
        public void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest, RequestProcessingContext requestProcessingContext) {
            final SpiceNotification notification = onCreateNotificationForRequestProcessed(cachedSpiceRequest, requestProcessingContext);
            notificationManager.notify(notification.getId(), notification.getNotification());
        }

        @Override
        public void onServiceStopped() {
            final SpiceNotification notification = onCreateNotificationForServiceStopped();
            notificationManager.notify(notification.getId(), notification.getNotification());
            stopSelf();
        }

    }

}
