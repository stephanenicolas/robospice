package com.octo.android.robospice.request;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import roboguice.util.temp.Ln;
import android.content.Context;
import android.content.pm.PackageManager;

import com.octo.android.robospice.exception.NetworkException;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ICacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.priority.PriorityRunnable;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;

public class RequestRunner {
    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================

    /**
     * Thanks Olivier Croiser from Zenika for his excellent <a href=
     * "http://blog.zenika.com/index.php?post/2012/04/11/Introduction-programmation-concurrente-Java-2sur2. "
     * >blog article</a>.
     */
    private ExecutorService executorService = null;
    private final ICacheManager cacheManager;
    private final Context applicationContext;
    private boolean failOnCacheError;
    private final NetworkStateChecker networkStateChecker;
    private final RequestProgressManager progressMonitor;

    // ============================================================================================
    // CONSTRUCTOR
    // ====================================================================================

    public RequestRunner(final Context context, final ICacheManager cacheManager,
            final ExecutorService executorService, 
            final RequestProgressManager requestProgressBroadcaster,
            final NetworkStateChecker networkStateChecker) {
        this.applicationContext = context;
        this.cacheManager = cacheManager;
        this.networkStateChecker = networkStateChecker;

        this.executorService = executorService;
        this.progressMonitor = requestProgressBroadcaster;

        this.networkStateChecker.checkPermissions(context);
    }

    public void executeRequest(CachedSpiceRequest<?> request) {
        planRequestExecution(request);
    }

    private static String getTimeString(long millis) {
        return String.format("%02d ms", millis);
    }

    private void printRequestProcessingDuration(long startTime, CachedSpiceRequest<?> request) {
        Ln.d("It tooks %s to process request %s.", getTimeString(System.currentTimeMillis() - startTime), request.toString());
    }

    protected <T> void processRequest(final CachedSpiceRequest<T> request) {
        final long startTime = System.currentTimeMillis();
        Ln.d("Processing request : " + request);

        T result = null;

        // add a progress listener to the request to be notified of
        // progress during load data from network

        final RequestProgressListener requestProgressListener = progressMonitor
                .createProgressListener(request);

        request.setRequestProgressListener(requestProgressListener);

        if (request.getRequestCacheKey() != null && request.getCacheDuration() != DurationInMillis.ALWAYS_EXPIRED) {
            // First, search data in cache
            try {
                Ln.d("Loading request from cache : " + request);
                request.setStatus(RequestStatus.READING_FROM_CACHE);
                result = loadDataFromCache(request.getResultType(), request.getRequestCacheKey(), request.getCacheDuration());
                // if something is found in cache, fire result and finish
                // request
                if (result != null) {
                    Ln.d("Request loaded from cache : " + request + " result=" + result);
                    progressMonitor.notifyListenersOfRequestSuccess(request, result);
                    printRequestProcessingDuration(startTime, request);
                    return;
                } else if (request.isAcceptingDirtyCache()) {
                    // as a fallback, some request may accept whatever is in the
                    // cache but still
                    // want an update from network.
                    result = loadDataFromCache(request.getResultType(), request.getRequestCacheKey(), DurationInMillis.ALWAYS_RETURNED);
                    if (result != null) {
                        progressMonitor.notifyListenersOfRequestSuccessButDontCompleteRequest(request, result);
                    }
                }
            } catch (final SpiceException e) {
                Ln.d(e, "Cache file could not be read.");
                if (failOnCacheError) {
                    handleRetry(request, e);
                    printRequestProcessingDuration(startTime, request);
                    return;
                }
                cacheManager.removeDataFromCache(request.getResultType(), request.getRequestCacheKey());
                Ln.d(e, "Cache file deleted.");
            }
        }

        // if result is not in cache, load data from network
        Ln.d("Cache content not available or expired or disabled");
        if (!isNetworkAvailable(applicationContext) && !request.isOffline()) {
            Ln.e("Network is down.");
            handleRetry(request, new NoNetworkException());
            printRequestProcessingDuration(startTime, request);
            return;
        }

        // network is ok, load data from network
        try {
            if (request.isCancelled()) {
                printRequestProcessingDuration(startTime, request);
                return;
            }
            Ln.d("Calling netwok request.");
            request.setStatus(RequestStatus.LOADING_FROM_NETWORK);
            result = request.loadDataFromNetwork();
            Ln.d("Network request call ended.");
        } catch (final Exception e) {
            if (!request.isCancelled()) {
                Ln.e(e, "An exception occurred during request network execution :" + e.getMessage());
                handleRetry(request, new NetworkException("Exception occurred during invocation of web service.", e));
            } else {
                Ln.e("An exception occurred during request network execution but request was cancelled, so listeners are not called.");
            }
            printRequestProcessingDuration(startTime, request);
            return;
        }

        if (result != null && request.getRequestCacheKey() != null) {
            // request worked and result is not null, save
            // it to cache
            try {
                if (request.isCancelled()) {
                    printRequestProcessingDuration(startTime, request);
                    return;
                }
                Ln.d("Start caching content...");
                request.setStatus(RequestStatus.WRITING_TO_CACHE);
                result = saveDataToCacheAndReturnData(result, request.getRequestCacheKey());
                if (request.isCancelled()) {
                    printRequestProcessingDuration(startTime, request);
                    return;
                }
                progressMonitor.notifyListenersOfRequestSuccess(request, result);
                printRequestProcessingDuration(startTime, request);
                return;
            } catch (final SpiceException e) {
                Ln.d(e, "An exception occurred during service execution :" + e.getMessage());
                if (failOnCacheError) {
                    handleRetry(request, e);
                    printRequestProcessingDuration(startTime, request);
                    return;
                } else {
                    if (request.isCancelled()) {
                        printRequestProcessingDuration(startTime, request);
                        return;
                    }
                    // result can't be saved to
                    // cache but we reached that
                    // point after a success of load
                    // data from
                    // network
                    progressMonitor.notifyListenersOfRequestSuccess(request, result);
                }
                cacheManager.removeDataFromCache(request.getResultType(), request.getRequestCacheKey());
                Ln.d(e, "Cache file deleted.");
            }
        } else {
            // result can't be saved to cache but we reached
            // that point after a success of load data from
            // network
            progressMonitor.notifyListenersOfRequestSuccess(request, result);
            printRequestProcessingDuration(startTime, request);
            return;
        }
    }

    protected void planRequestExecution(final CachedSpiceRequest<?> request) {
        Future<?> future = executorService.submit(new PriorityRunnable() {
            @Override
            public void run() {
                try {
                    processRequest(request);
                } catch (final Throwable t) {
                    Ln.d(t, "An unexpected error occurred when processsing request %s", request.toString());
                } finally {
                    request.setRequestCancellationListener(null);
                }
            }

            @Override
            public int getPriority() {
                return request.getPriority();
            }
        });
        request.setFuture(future);
    }

    private void handleRetry(final CachedSpiceRequest<?> request, final SpiceException e) {
        if (request.getRetryPolicy() != null) {
            request.getRetryPolicy().retry(e);
            if (request.getRetryPolicy().getRetryCount() > 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(request.getRetryPolicy().getDelayBeforeRetry());
                            planRequestExecution(request);
                        } catch (InterruptedException e) {
                            Ln.e(e, "Retry attempt failed for request " + request);
                        }
                    }
                }).start();
                return;
            }
        }
        progressMonitor.notifyListenersOfRequestFailure(request, e);
    }

    /**
     * @return true if network is available.
     */
    public boolean isNetworkAvailable(final Context context) {
        return networkStateChecker.isNetworkAvailable(context);
    }

    public void checkPermissions(final Context context) {
        networkStateChecker.checkPermissions(context);
    }

    public static boolean hasNetworkPermission(final Context context) {
        return context.getPackageManager().checkPermission("android.permission.INTERNET", context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
    }


    public boolean isFailOnCacheError() {
        return failOnCacheError;
    }

    public void setFailOnCacheError(boolean failOnCacheError) {
        this.failOnCacheError = failOnCacheError;
    }

    // ============================================================================================
    // PRIVATE
    // ============================================================================================

    private <T> T loadDataFromCache(final Class<T> clazz, final Object cacheKey, final long maxTimeInCacheBeforeExpiry) throws CacheLoadingException, CacheCreationException {
        return cacheManager.loadDataFromCache(clazz, cacheKey, maxTimeInCacheBeforeExpiry);
    }

    private <T> T saveDataToCacheAndReturnData(final T data, final Object cacheKey) throws CacheSavingException, CacheCreationException {
        return cacheManager.saveDataToCacheAndReturnData(data, cacheKey);
    }
}
