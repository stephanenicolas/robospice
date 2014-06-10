package com.octo.android.robospice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import roboguice.util.temp.Ln;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.octo.android.robospice.networkstate.DefaultNetworkStateChecker;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.priority.PriorityThreadPoolExecutor;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.DefaultRequestRunner;
import com.octo.android.robospice.request.RequestProcessor;
import com.octo.android.robospice.request.RequestProcessorListener;
import com.octo.android.robospice.request.RequestProgressManager;
import com.octo.android.robospice.request.RequestRunner;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.SpiceServiceListener;
import com.octo.android.robospice.request.notifier.DefaultRequestListenerNotifier;
import com.octo.android.robospice.request.notifier.RequestListenerNotifier;
import com.octo.android.robospice.request.notifier.SpiceServiceListenerNotifier;

/**
 * This is an abstract class used to manage the cache and provide web service
 * result to an activity. <br/>
 * Extends this class to provide a service able to load content from web service
 * or cache (if available and enabled). You will have to implement
 * {@link #createCacheManager(Application)} to configure the
 * {@link CacheManager} used by all requests to persist their results in the
 * cache (and load them from cache if possible).
 * @author jva
 * @author mwa
 * @author sni
 */
public abstract class SpiceService extends Service {

    // http://stackoverflow.com/a/13359680/693752
    /** JUNIT - this is for testing purposes only */
    private static boolean isJUnit = false;

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------
    protected static final int DEFAULT_NOTIFICATION_ID = 42;

    protected static final int DEFAULT_THREAD_COUNT = 1;
    protected static final int DEFAULT_THREAD_PRIORITY = Thread.MIN_PRIORITY;
    /** Default in TimeUnit.NANOSECONDS implies core threads are not disposed when idle.*/
    protected static final int DEFAULT_THREAD_KEEP_ALIVE_TIME = 0;

    private static final boolean DEFAULT_FAIL_ON_CACHE_ERROR = false;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private SpiceServiceBinder mSpiceServiceBinder;

    /** Responsible for processing requests. */
    private RequestProcessor requestProcessor;

    private int currentPendingRequestCount = 0;

    private boolean isBound;

    private Notification notification;

    /** Responsible for persisting data. */
    private CacheManager cacheManager;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    /**
     * Default constructor.
     */
    public SpiceService() {
        mSpiceServiceBinder = new SpiceServiceBinder(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            cacheManager = createCacheManager(getApplication());
        } catch (CacheCreationException e) {
            Ln.e(e);
            stopSelf();
            return;
        }
        if (cacheManager == null) {
            Ln.e(new CacheCreationException("createCacheManager() can't create a null cacheManager"));
            stopSelf();
            return;
        }

        final RequestListenerNotifier progressReporter = createRequestRequestListenerNotifier();
        final SpiceServiceListenerNotifier spiceServiceListenerNotifier = createSpiceServiceListenerNotifier();
        final RequestProcessorListener requestProcessorListener = createRequestProcessorListener();
        final ExecutorService executorService = getExecutorService();
        final NetworkStateChecker networkStateChecker = getNetworkStateChecker();
        final RequestProgressManager requestProgressManager = createRequestProgressManager(requestProcessorListener, progressReporter, spiceServiceListenerNotifier);
        final RequestRunner requestRunner = createRequestRunner(executorService, networkStateChecker, requestProgressManager);

        requestProcessor = createRequestProcessor(cacheManager, requestProgressManager, requestRunner);
        requestProcessor.setFailOnCacheError(DEFAULT_FAIL_ON_CACHE_ERROR);

        notification = createDefaultNotification();

        Ln.d("SpiceService instance created.");
    }

    private RequestRunner createRequestRunner(final ExecutorService executorService, final NetworkStateChecker networkStateChecker, RequestProgressManager requestProgressManager) {
        return new DefaultRequestRunner(getApplicationContext(), cacheManager, executorService, requestProgressManager, networkStateChecker);
    }

    private RequestProgressManager createRequestProgressManager(final RequestProcessorListener requestProcessorListener, final RequestListenerNotifier progressReporter,
        final SpiceServiceListenerNotifier spiceServiceListenerNotifier) {
        return new RequestProgressManager(requestProcessorListener, progressReporter, spiceServiceListenerNotifier);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    /**
     * Factory method to create an entity responsible for processing requests
     * send to the SpiceService. The default implementation of this method will
     * return a {@link RequestProcessor}. Override this method if you want to
     * inject a custom request processor. This feature has been implemented
     * following a request from Christopher Jenkins.
     * @param cacheManager
     *            the cache manager used by this service.
     * @param requestProgressManager
     *            will notify of requests progress.
     * @param requestRunner
     *            executes requests.
     * @return a {@link RequestProcessor} that will be used to process requests.
     */
    protected RequestProcessor createRequestProcessor(CacheManager cacheManager, RequestProgressManager requestProgressManager, RequestRunner requestRunner) {
        return new RequestProcessor(cacheManager, requestProgressManager, requestRunner);
    }

    /**
     * Creates a {@link RequestProcessorListener} for the
     * {@link RequestProcessor} used by this service. See a typical
     * implementation : @see {@link SelfStopperRequestProcessorListener}.
     * @return a new instance {@link RequestProcessorListener} for the
     *         {@link RequestProcessor} used by this service.
     */
    protected RequestProcessorListener createRequestProcessorListener() {
        return new SelfStopperRequestProcessorListener();
    }

    /**
     * For testing purposes only.
     * @return the request processor of this spice service.
     */
    protected RequestProcessor getRequestProcessor() {
        return requestProcessor;
    }

    /**
     * Method to create a Request Progress Reporter object which is responsible
     * for informing the listeners of the current state of each request. You can
     * use this method to modify the existing behavior
     * @return {@link RequestListenerNotifier}
     */
    protected RequestListenerNotifier createRequestRequestListenerNotifier() {
        return new DefaultRequestListenerNotifier();
    }

    /**
     * Factory method to create an entity responsible to check for network
     * state. The default implementation of this method will return a
     * {@link DefaultNetworkStateChecker}. Override this method if you want to
     * inject a custom network state for testing or to adapt to connectivity
     * changes on the Android. This method is also useful to create non-network
     * related requests. In that case create a {@link NetworkStateChecker} that
     * always return true. This feature has been implemented following a request
     * from Pierre Durand.
     * @return a {@link NetworkStateChecker} that will be used to determine if
     *         network state allows requests executions.
     */
    protected NetworkStateChecker getNetworkStateChecker() {
        return new DefaultNetworkStateChecker();
    }

    /**
     * Factory method to create an {@link ExecutorService} that will be used to
     * execute requests. The default implementation of this method will create a
     * single threaded or multi-threaded {@link ExecutorService} depending on
     * the number of threads returned by {@link #getThreadCount()} and will
     * assign them the priority returned by {@link #getThreadPriority()}.
     * <p/>
     * If you override this method in your service, you can supply a custom
     * {@link ExecutorService}.
     * <p/>
     * This feature has been implemented following a request from Riccardo
     * Ciovati.
     * @return the {@link ExecutorService} to be used to execute requests .
     */
    protected ExecutorService getExecutorService() {
        final int coreThreadCount = getCoreThreadCount();
        final int maxThreadCount = getMaximumThreadCount();
        final int threadPriority = getThreadPriority();
        if (coreThreadCount <= 0 || maxThreadCount <= 0) {
            throw new IllegalArgumentException("Thread count must be >= 1");
        } else {
            PriorityThreadPoolExecutor executor = PriorityThreadPoolExecutor
                .getPriorityExecutor(coreThreadCount, maxThreadCount,
                    threadPriority);
            executor.setKeepAliveTime(getKeepAliveTime(), TimeUnit.NANOSECONDS);
            executor.allowCoreThreadTimeOut(isCoreThreadDisposable());
            return executor;
        }
    }

    /**
     * Creates the SpiceServiceListenerNotifier.
     * @return {@link SpiceServiceListenerNotifier}
     */
    protected SpiceServiceListenerNotifier createSpiceServiceListenerNotifier() {
        return new SpiceServiceListenerNotifier();
    }

    /**
     * This method can be overriden in order to create a foreground
     * SpiceService. By default, it will create a notification that can be used
     * to set a spiceService to foreground (depending on the versions of
     * Android, the behavior is different : before ICS, no notification is
     * shown, on ICS+, a notification is shown with app icon). On Jelly Bean+,
     * the notifiation only appears when notification bar is expanded / pulled
     * down.
     * @return a notification used to tell user that the SpiceService is still
     *         running and processing requests.
     */
    @SuppressWarnings("deprecation")
    public Notification createDefaultNotification() {

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = new Notification.Builder(this).setSmallIcon(getApplicationInfo().icon).build();
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            notification = new Notification.Builder(this).setSmallIcon(getApplicationInfo().icon).getNotification();
        } else {
            notification = new Notification();
            notification.icon = getApplicationInfo().icon;
            // temporary fix https://github.com/octo-online/robospice/issues/200
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
            notification.setLatestEventInfo(this, "", "", pendingIntent);
            notification.tickerText = null;
            notification.when = System.currentTimeMillis();
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.priority = Notification.PRIORITY_MIN;
        }

        return notification;
    }

    protected int getNotificationId() {
        return DEFAULT_NOTIFICATION_ID;
    }

    @Override
    public void onDestroy() {
        requestProcessor.shouldStop();
        Ln.d("SpiceService instance destroyed.");
        super.onDestroy();
    }

    // ----------------------------------
    // DELEGATE METHODS (delegation is used to ease tests)
    // ----------------------------------

    public abstract CacheManager createCacheManager(Application application)
        throws CacheCreationException;

    /**
     * Override this method to increase the number of threads used to process
     * requests. This method will have no effect if you override
     * {@link #getExecutorService()}.
     * @return the number of threads used to process requests. Defaults to
     *         {@link #DEFAULT_THREAD_COUNT}.
     */
    public int getThreadCount() {
        return DEFAULT_THREAD_COUNT;
    }

    /**
     * Override this method to increase the number of core threads used to
     * process requests. This method will have no effect if you override
     * {@link #getExecutorService()}.
     * @return the number of threads used to process requests. Defaults to
     *         {@link #DEFAULT_THREAD_COUNT}.
     */
    public int getCoreThreadCount() {
        return getThreadCount();
    }

    /**
     * Override this method to increase the number of maximum threads used to
     * process requests. This method will have no effect if you override
     * {@link #getExecutorService()}.
     * @return the number of threads used to process requests. Defaults to
     *         {@link #DEFAULT_THREAD_COUNT}.
     */
    public int getMaximumThreadCount() {
        return getThreadCount();
    }

    /**
     * Override this method to set the keep alive time for core threads
     * {@link #getExecutorService()}.
     * @return the time to keep alive idle threads on {@see
     *         TimeUnit.NANOSECONDS}. Defaults to
     *         {@link #DEFAULT_THREAD_KEEP_ALIVE_TIME}.
     */
    public int getKeepAliveTime() {
        return DEFAULT_THREAD_KEEP_ALIVE_TIME;
    }
    
    /**
     * Override this method to disable timeout on core threads.
     * {@link #getExecutorService()}.
     * @return whether core threads are disposable or not (DEFAULT=true).
     */
    public boolean isCoreThreadDisposable() {
        return true;
    }

    /**
     * Override this method to change the priority of threads requests. This
     * method will have no effect if you override {@link #getExecutorService()}.
     * @return the number of threads used to process requests.Defaults to
     *         {@link #DEFAULT_THREAD_PRIORITY}.
     */
    public int getThreadPriority() {
        return DEFAULT_THREAD_PRIORITY;
    }

    public void addRequest(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listRequestListener) {
        currentPendingRequestCount++;
        requestProcessor.addRequest(request, listRequestListener);
        showNotificationIfNotBoundAndHasPendingRequestsOtherwiseHideNotification();
    }

    public boolean removeDataFromCache(final Class<?> clazz, final Object cacheKey) {
        return requestProcessor.removeDataFromCache(clazz, cacheKey);
    }

    public void removeAllDataFromCache(final Class<?> clazz) {
        requestProcessor.removeAllDataFromCache(clazz);
    }

    public <T> List<Object> getAllCacheKeys(final Class<T> clazz) {
        return cacheManager.getAllCacheKeys(clazz);
    }

    public <T> List<T> loadAllDataFromCache(final Class<T> clazz) throws CacheLoadingException, CacheCreationException {
        return cacheManager.loadAllDataFromCache(clazz);
    }

    public <T> T getDataFromCache(final Class<T> clazz, final Object cacheKey) throws CacheLoadingException, CacheCreationException {
        return cacheManager.loadDataFromCache(clazz, cacheKey, DurationInMillis.ALWAYS_RETURNED);
    }

    public <T> T putDataInCache(final Object cacheKey, T data) throws CacheSavingException, CacheCreationException {
        return cacheManager.saveDataToCacheAndReturnData(data, cacheKey);
    }

    public boolean isDataInCache(Class<?> clazz, Object cacheKey, long cacheExpiryDuration) throws CacheCreationException {
        return cacheManager.isDataInCache(clazz, cacheKey, cacheExpiryDuration);
    }

    public Date getDateOfDataInCache(Class<?> clazz, Object cacheKey) throws CacheLoadingException, CacheCreationException {
        return cacheManager.getDateOfDataInCache(clazz, cacheKey);
    }

    public void removeAllDataFromCache() {
        requestProcessor.removeAllDataFromCache();
    }

    public boolean isFailOnCacheError() {
        return requestProcessor.isFailOnCacheError();
    }

    public void setFailOnCacheError(final boolean failOnCacheError) {
        requestProcessor.setFailOnCacheError(failOnCacheError);
    }

    public void dontNotifyRequestListenersForRequest(final CachedSpiceRequest<?> request, final Collection<RequestListener<?>> listRequestListener) {
        requestProcessor.dontNotifyRequestListenersForRequest(request, listRequestListener);
    }

    // ----------------------------------
    // SERVICE METHODS
    // ----------------------------------

    @Override
    public IBinder onBind(final Intent intent) {
        isBound = true;
        showNotificationIfNotBoundAndHasPendingRequestsOtherwiseHideNotification();
        return mSpiceServiceBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        isBound = true;
        showNotificationIfNotBoundAndHasPendingRequestsOtherwiseHideNotification();
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        isBound = false;
        showNotificationIfNotBoundAndHasPendingRequestsOtherwiseHideNotification();
        stopIfNotBoundAndHasNoPendingRequests();
        return true;
    }

    protected final class SelfStopperRequestProcessorListener implements RequestProcessorListener {
        @Override
        public void requestsInProgress() {
        }

        @Override
        public void allRequestComplete() {
            currentPendingRequestCount = 0;
            stopIfNotBoundAndHasNoPendingRequests();
        }
    }

    public static class SpiceServiceBinder extends Binder {
        private final SpiceService spiceService;

        public SpiceServiceBinder(final SpiceService spiceService) {
            this.spiceService = spiceService;
        }

        public SpiceService getSpiceService() {
            return spiceService;
        }
    }

    public void dumpState() {
        Ln.v(requestProcessor.toString());
    }

    public void addSpiceServiceListener(final SpiceServiceListener spiceServiceListener) {
        requestProcessor.addSpiceServiceListener(spiceServiceListener);
    }

    public void removeSpiceServiceListener(final SpiceServiceListener spiceServiceListener) {
        requestProcessor.removeSpiceServiceListener(spiceServiceListener);
    }

    private void stopIfNotBoundAndHasNoPendingRequests() {
        Ln.v("Pending requests : " + currentPendingRequestCount);
        if (currentPendingRequestCount == 0 && !isBound) {
            stopSelf();
        }
    }

    private void showNotificationIfNotBoundAndHasPendingRequestsOtherwiseHideNotification() {
        // http://stackoverflow.com/a/13359680/693752
        if (notification == null || isJUnit) {
            return;
        }
        Ln.v("Pending requests : " + currentPendingRequestCount);
        if (isBound || currentPendingRequestCount == 0) {
            Ln.v("Stop foreground");
            stopForeground(true);
        } else {
            Ln.v("Start foreground");
            startForeground(notification);
        }
    }

    private void startForeground(final Notification notification) {
        try {
            final Method setForegroundMethod = Service.class.getMethod("startForeground", int.class, Notification.class);
            setForegroundMethod.invoke(this, getNotificationId(), notification);
        } catch (final SecurityException e) {
            Ln.e(e, "Unable to start a service in foreground");
        } catch (final NoSuchMethodException e) {
            Ln.e(e, "Unable to start a service in foreground");
        } catch (final IllegalArgumentException e) {
            Ln.e(e, "Unable to start a service in foreground");
        } catch (final IllegalAccessException e) {
            Ln.e(e, "Unable to start a service in foreground");
        } catch (final InvocationTargetException e) {
            Ln.e(e, "Unable to start a service in foreground");
        }
    }

    // http://code.google.com/p/android/issues/detail?id=12122
    // There is a bug in ServiceTestCase : a call to setForeground will fail
    public static final void setIsJunit(boolean b) {
        isJUnit = b;
    }
}
