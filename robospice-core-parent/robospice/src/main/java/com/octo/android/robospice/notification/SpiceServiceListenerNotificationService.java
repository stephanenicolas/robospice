package com.octo.android.robospice.notification;

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

/**
 * Will display updates in the status of a given spice service itself by
 * displaying notifications when the service processes a request, updates its
 * progress, etc. This service will need to be started via an intent and will be
 * stopped automatically when the target spice service stops.
 * @author SNI
 */
public abstract class SpiceServiceListenerNotificationService extends Service {

    private static final int DEFAULT_ROBOSPICE_NOTIFICATION_ID = 700;
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

    @SuppressWarnings({ "unchecked", "deprecation" })
    @Override
    public final void onStart(final Intent intent, final int startId) {
        super.onStart(intent, startId);
        notificationId = intent.getIntExtra(BUNDLE_KEY_NOTIFICATION_ID, DEFAULT_ROBOSPICE_NOTIFICATION_ID);
        spiceServiceClass = (Class<? extends SpiceService>) intent.getSerializableExtra(BUNDLE_KEY_SERVICE_CLASS);
        foreground = intent.getBooleanExtra(BUNDLE_KEY_FOREGROUND, true);

        spiceManager = new SpiceManager(spiceServiceClass);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        spiceManager.start(this);
        spiceManager.addSpiceServiceListener(new NotificationSpiceServiceListener());

        if (foreground) {
            startForeground(notificationId, onCreateForegroundNotification());
        }
    }

    @Override
    public final void onDestroy() {
        spiceManager.shouldStop();
        super.onDestroy();
    }

    public SpiceNotification onCreateForegroundNotification() {
        throw new RuntimeException("If you use foreground = true, then you must override onCreateForegroundNotification().");
    }

    public abstract SpiceNotification onCreateNotificationForServiceStopped();

    public abstract SpiceNotification onCreateNotificationForRequestSucceeded(CachedSpiceRequest<?> request, Thread thread);

    public abstract SpiceNotification onCreateNotificationForRequestCancelled(CachedSpiceRequest<?> request, Thread thread);

    public abstract SpiceNotification onCreateNotificationForRequestFailed(CachedSpiceRequest<?> request, Thread thread);

    public abstract SpiceNotification onCreateNotificationForRequestProgressUpdate(CachedSpiceRequest<?> request, Thread thread);

    public abstract SpiceNotification onCreateNotificationForRequestAdded(CachedSpiceRequest<?> request, Thread thread);

    public abstract SpiceNotification onCreateNotificationForRequestNotFound(CachedSpiceRequest<?> request, Thread thread);

    public abstract SpiceNotification onCreateNotificationForRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest);

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------

    public class SpiceNotification extends Notification {
        private int id;

        public SpiceNotification(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public class NotificationSpiceServiceListener implements SpiceServiceListener {

        @Override
        public void onRequestSucceeded(CachedSpiceRequest<?> request, Thread thread) {
            final SpiceNotification notification = onCreateNotificationForRequestSucceeded(request, thread);
            notificationManager.notify(notification.getId(), notification);
        }

        @Override
        public void onRequestFailed(CachedSpiceRequest<?> request, Thread thread) {
            final SpiceNotification notification = onCreateNotificationForRequestFailed(request, thread);
            notificationManager.notify(notification.getId(), notification);
        }

        @Override
        public void onRequestCancelled(CachedSpiceRequest<?> request, Thread thread) {
            final SpiceNotification notification = onCreateNotificationForRequestCancelled(request, thread);
            notificationManager.notify(notification.getId(), notification);
        }

        @Override
        public void onRequestProgressUpdated(CachedSpiceRequest<?> request, Thread thread) {
            final SpiceNotification notification = onCreateNotificationForRequestProgressUpdate(request, thread);
            notificationManager.notify(notification.getId(), notification);
        }

        @Override
        public void onRequestAdded(CachedSpiceRequest<?> request, Thread thread) {
            final SpiceNotification notification = onCreateNotificationForRequestAdded(request, thread);
            notificationManager.notify(notification.getId(), notification);
        }

        @Override
        public void onRequestNotFound(CachedSpiceRequest<?> request, Thread thread) {
            final SpiceNotification notification = onCreateNotificationForRequestNotFound(request, thread);
            notificationManager.notify(notification.getId(), notification);
        }

        @Override
        public void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest) {
            final SpiceNotification notification = onCreateNotificationForRequestProcessed(cachedSpiceRequest);
            notificationManager.notify(notification.getId(), notification);
        }

        @Override
        public void onServiceStopped() {
            final SpiceNotification notification = onCreateNotificationForServiceStopped();
            notificationManager.notify(notification.getId(), notification);
            stopSelf();
        }

    }

}
