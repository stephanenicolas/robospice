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
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.exception.NetworkException;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ICacheManager;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestCancellationListener;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;
import com.octo.android.robospice.request.listener.SpiceServiceServiceListener;

/**
 * Delegate class of the {@link SpiceService}, easier to test than an Android {@link Service}.
 * @author jva
 */
public class RequestProcessor {
    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================
    private final Map<CachedSpiceRequest<?>, Set<RequestListener<?>>> mapRequestToRequestListener = Collections
        .synchronizedMap(new LinkedHashMap<CachedSpiceRequest<?>, Set<RequestListener<?>>>());

    /**
     * Thanks Olivier Croiser from Zenika for his excellent <a href= "http://blog.zenika.com/index.php?post/2012/04/11/Introduction-programmation-concurrente-Java-2sur2. " >blog article</a>.
     */
    private ExecutorService executorService = null;

    private final ICacheManager cacheManager;

    private final Handler handlerResponse;

    private final Context applicationContext;

    private boolean failOnCacheError;

    private final Set<SpiceServiceServiceListener> spiceServiceListenerSet;

    private final RequestProcessorListener requestProcessorListener;

    private final NetworkStateChecker networkStateChecker;

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================

    /**
     * Build a request processor using a custom. This feature has been implemented follwing a feature request from Riccardo Ciovati.
     * @param context
     *            the context on which {@link SpiceRequest} will provide their results.
     * @param cacheManager
     *            the {@link CacheManager} that will be used to retrieve requests' result and store them.
     * @param executorService
     *            a custom {@link ExecutorService} that will be used to execute {@link SpiceRequest}.
     * @param requestProcessorListener
     *            a listener of the {@link RequestProcessor}, it will be notified when no more requests are left, typically allowing the {@link SpiceService} to stop itself.
     */
    public RequestProcessor(final Context context, final ICacheManager cacheManager, final ExecutorService executorService,
        final RequestProcessorListener requestProcessorListener, final NetworkStateChecker networkStateChecker) {
        this.applicationContext = context;
        this.cacheManager = cacheManager;
        this.requestProcessorListener = requestProcessorListener;
        this.networkStateChecker = networkStateChecker;

        handlerResponse = new Handler(Looper.getMainLooper());
        spiceServiceListenerSet = Collections.synchronizedSet(new HashSet<SpiceServiceServiceListener>());
        this.executorService = executorService;

        this.networkStateChecker.checkPermissions(context);
    }

    // ============================================================================================
    // PUBLIC
    // ============================================================================================
    public void addRequest(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listRequestListener) {
        Ln.d("Adding request to queue " + hashCode() + ": " + request + " size is " + mapRequestToRequestListener.size());

        if (request.isCancelled()) {
            synchronized (mapRequestToRequestListener) {
                for (final CachedSpiceRequest<?> cachedSpiceRequest : mapRequestToRequestListener.keySet()) {
                    if (cachedSpiceRequest.equals(request)) {
                        cachedSpiceRequest.cancel();
                        return;
                    }
                }
            }
        }

        boolean aggregated = false;
        if (listRequestListener != null) {
            Set<RequestListener<?>> listRequestListenerForThisRequest = mapRequestToRequestListener.get(request);

            if (listRequestListenerForThisRequest == null) {
                listRequestListenerForThisRequest = new HashSet<RequestListener<?>>();
                this.mapRequestToRequestListener.put(request, listRequestListenerForThisRequest);
            } else {
                Ln.d(String.format("Request for type %s and cacheKey %s already exists.", request.getResultType(), request.getRequestCacheKey()));
                aggregated = true;
            }

            listRequestListenerForThisRequest.addAll(listRequestListener);
            if (request.isProcessable()) {
                notifyListenersOfRequestProgress(request, listRequestListener, request.getProgress());
            }
        }

        if (aggregated) {
            return;
        }

        final RequestCancellationListener requestCancellationListener = new RequestCancellationListener() {

            @Override
            public void onRequestCancelled() {
                mapRequestToRequestListener.remove(request);
                notifyListenersOfRequestCancellation(request, listRequestListener);
            }
        };
        request.setRequestCancellationListener(requestCancellationListener);

        if (request.isCancelled()) {
            mapRequestToRequestListener.remove(request);
            notifyListenersOfRequestCancellation(request, listRequestListener);
            return;
        } else {

            final Future<?> future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        processRequest(request);
                    } catch (final Throwable t) {
                        Ln.d(t, "An unexpected error occured when processsing request %s", request.toString());

                    }
                }
            });
            request.setFuture(future);
        }
    }

    protected <T> void processRequest(final CachedSpiceRequest<T> request) {

        Ln.d("Processing request : " + request);

        T result = null;
        if (!request.isProcessable()) {
            notifyOfRequestProcessed(request);
            return;
        }

        // add a progress listener to the request to be notified of
        // progress during load data from network
        final RequestProgressListener requestProgressListener = new RequestProgressListener() {
            @Override
            public void onRequestProgressUpdate(final RequestProgress progress) {
                final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
                notifyListenersOfRequestProgress(request, listeners, progress);
            }
        };
        request.setRequestProgressListener(requestProgressListener);

        if (request.getRequestCacheKey() != null && request.getCacheDuration() != DurationInMillis.ALWAYS_EXPIRED) {
            // First, search data in cache
            try {
                Ln.d("Loading request from cache : " + request);
                request.setStatus(RequestStatus.READING_FROM_CACHE);
                result = loadDataFromCache(request.getResultType(), request.getRequestCacheKey(), request.getCacheDuration());
                // if something is found in cache, fire result and finish request
                if (result != null) {
                    Ln.d("Request loaded from cache : " + request + " result=" + result);
                    notifyListenersOfRequestSuccess(request, result);
                    return;
                } else if (request.isAcceptingDirtyCache()) {
                    // as a fallback, some request may accept whatever is in the cache but still want an update from network.
                    result = loadDataFromCache(request.getResultType(), request.getRequestCacheKey(), DurationInMillis.ALWAYS_RETURNED);
                    if (result != null) {
                        notifyListenersOfRequestSuccessButDontCompleteRequest(request, result);
                    }
                }
            } catch (final CacheLoadingException e) {
                Ln.d(e, "Cache file could not be read.");
                if (failOnCacheError) {
                    notifyListenersOfRequestFailure(request, e);
                    return;
                }
                cacheManager.removeDataFromCache(request.getResultType(), request.getRequestCacheKey());
                Ln.d(e, "Cache file deleted.");
            }
        }

        // if result is not in cache, load data from network
        Ln.d("Cache content not available or expired or disabled");
        if (!isNetworkAvailable(applicationContext)) {
            Ln.e("Network is down.");
            notifyListenersOfRequestFailure(request, new NoNetworkException());
            return;
        }

        // network is ok, load data from network
        try {
            if (request.isCancelled()) {
                return;
            }
            Ln.d("Calling netwok request.");
            request.setStatus(RequestStatus.LOADING_FROM_NETWORK);
            result = request.loadDataFromNetwork();
            Ln.d("Network request call ended.");
        } catch (final Exception e) {
            if (!request.isCancelled()) {
                Ln.e(e, "An exception occured during request network execution :" + e.getMessage());
                notifyListenersOfRequestFailure(request, new NetworkException("Exception occured during invocation of web service.", e));
            } else {
                Ln.e("An exception occured during request network execution but request was cancelled, so listeners are not called.");
            }
            return;
        }

        if (result != null && request.getRequestCacheKey() != null) {
            // request worked and result is not null, save
            // it to cache
            try {
                if (request.isCancelled()) {
                    return;
                }
                Ln.d("Start caching content...");
                request.setStatus(RequestStatus.WRITING_TO_CACHE);
                result = saveDataToCacheAndReturnData(result, request.getRequestCacheKey());
                if (request.isCancelled()) {
                    return;
                }
                notifyListenersOfRequestSuccess(request, result);
                return;
            } catch (final CacheSavingException e) {
                Ln.d("An exception occured during service execution :" + e.getMessage(), e);
                if (failOnCacheError) {
                    notifyListenersOfRequestFailure(request, e);
                    return;
                } else {
                    if (request.isCancelled()) {
                        return;
                    }
                    // result can't be saved to
                    // cache but we reached that
                    // point after a success of load
                    // data from
                    // network
                    notifyListenersOfRequestSuccess(request, result);
                }
                cacheManager.removeDataFromCache(request.getResultType(), request.getRequestCacheKey());
                Ln.d(e, "Cache file deleted.");
            }
        } else {
            // result can't be saved to cache but we reached
            // that point after a success of load data from
            // network
            notifyListenersOfRequestSuccess(request, result);
            return;
        }
    }

    private void post(final Runnable r, final Object token) {
        handlerResponse.postAtTime(r, token, SystemClock.uptimeMillis());
    }

    private <T> void notifyListenersOfRequestProgress(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listeners,
        final RequestStatus status) {
        notifyListenersOfRequestProgress(request, listeners, new RequestProgress(status));
    }

    private <T> void notifyListenersOfRequestProgress(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listeners,
        final RequestProgress progress) {
        Ln.d("Sending progress %s", progress.getStatus());
        post(new ProgressRunnable(listeners, progress), request.getRequestCacheKey());
        checkAllRequestComplete();
    }

    private void checkAllRequestComplete() {
        if (mapRequestToRequestListener.isEmpty()) {
            requestProcessorListener.allRequestComplete();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T> void notifyListenersOfRequestSuccessButDontCompleteRequest(final CachedSpiceRequest<T> request, final T result) {
        final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
        post(new ResultRunnable(listeners, result), request.getRequestCacheKey());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T> void notifyListenersOfRequestSuccess(final CachedSpiceRequest<T> request, final T result) {
        final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
        notifyListenersOfRequestProgress(request, listeners, RequestStatus.COMPLETE);
        post(new ResultRunnable(listeners, result), request.getRequestCacheKey());
        notifyOfRequestProcessed(request);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T> void notifyListenersOfRequestFailure(final CachedSpiceRequest<T> request, final SpiceException e) {
        final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
        notifyListenersOfRequestProgress(request, listeners, RequestStatus.COMPLETE);
        post(new ResultRunnable(listeners, e), request.getRequestCacheKey());
        notifyOfRequestProcessed(request);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void notifyListenersOfRequestCancellation(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listeners) {
        Ln.d("Not calling network request : " + request + " as it is cancelled. ");
        notifyListenersOfRequestProgress(request, listeners, RequestStatus.COMPLETE);
        post(new ResultRunnable(listeners, new RequestCancelledException("Request has been cancelled explicitely.")), request.getRequestCacheKey());
        notifyOfRequestProcessed(request);
    }

    /**
     * Disable request listeners notifications for a specific request.<br/>
     * All listeners associated to this request won't be called when request will finish.<br/>
     * @param request
     *            Request on which you want to disable listeners
     * @param listRequestListener
     *            the collection of listeners associated to request not to be notified
     */
    public void dontNotifyRequestListenersForRequest(final CachedSpiceRequest<?> request, final Collection<RequestListener<?>> listRequestListener) {
        handlerResponse.removeCallbacksAndMessages(request.getRequestCacheKey());
        final Set<RequestListener<?>> setRequestListener = mapRequestToRequestListener.get(request);
        if (setRequestListener != null && listRequestListener != null) {
            Ln.d("Removing listeners of request : " + request.toString() + " : " + setRequestListener.size());
            setRequestListener.removeAll(listRequestListener);
        }
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

    public boolean removeDataFromCache(final Class<?> clazz, final Object cacheKey) {
        return cacheManager.removeDataFromCache(clazz, cacheKey);
    }

    public void removeAllDataFromCache(final Class<?> clazz) {
        cacheManager.removeAllDataFromCache(clazz);
    }

    public void removeAllDataFromCache() {
        cacheManager.removeAllDataFromCache();
    }

    public boolean isFailOnCacheError() {
        return failOnCacheError;
    }

    public void setFailOnCacheError(final boolean failOnCacheError) {
        this.failOnCacheError = failOnCacheError;
    }

    // ============================================================================================
    // PRIVATE
    // ============================================================================================

    private <T> T loadDataFromCache(final Class<T> clazz, final Object cacheKey, final long maxTimeInCacheBeforeExpiry) throws CacheLoadingException {
        return cacheManager.loadDataFromCache(clazz, cacheKey, maxTimeInCacheBeforeExpiry);
    }

    private <T> T saveDataToCacheAndReturnData(final T data, final Object cacheKey) throws CacheSavingException {
        return cacheManager.saveDataToCacheAndReturnData(data, cacheKey);
    }

    private static class ProgressRunnable implements Runnable {
        private final RequestProgress progress;
        private final Set<RequestListener<?>> listeners;

        public ProgressRunnable(final Set<RequestListener<?>> listeners, final RequestProgress progress) {
            this.progress = progress;
            this.listeners = listeners;
        }

        @Override
        public void run() {

            if (listeners == null) {
                return;
            }

            Ln.v("Notifying " + listeners.size() + " listeners of progress " + progress);
            for (final RequestListener<?> listener : listeners) {
                if (listener != null && listener instanceof RequestProgressListener) {
                    Ln.v("Notifying %s", listener.getClass().getSimpleName());
                    ((RequestProgressListener) listener).onRequestProgressUpdate(progress);
                }
            }
        }
    }

    private static class ResultRunnable<T> implements Runnable {

        private SpiceException spiceException;
        private T result;
        private final Set<RequestListener<?>> listeners;

        public ResultRunnable(final Set<RequestListener<?>> listeners, final T result) {
            this.result = result;
            this.listeners = listeners;
        }

        public ResultRunnable(final Set<RequestListener<?>> listeners, final SpiceException spiceException) {
            this.spiceException = spiceException;
            this.listeners = listeners;
        }

        @Override
        public void run() {
            if (listeners == null) {
                return;
            }

            final String resultMsg = spiceException == null ? "success" : "failure";
            Ln.v("Notifying " + listeners.size() + " listeners of request " + resultMsg);
            for (final RequestListener<?> listener : listeners) {
                if (listener != null) {
                    @SuppressWarnings("unchecked")
                    final RequestListener<T> listenerOfT = (RequestListener<T>) listener;
                    Ln.v("Notifying %s", listener.getClass().getSimpleName());
                    if (spiceException == null) {
                        listenerOfT.onRequestSuccess(result);
                    } else {
                        listener.onRequestFailure(spiceException);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        stringBuilder.append(getClass().getName());
        stringBuilder.append(" : ");

        stringBuilder.append(" request count= ");
        stringBuilder.append(mapRequestToRequestListener.keySet().size());

        stringBuilder.append(", listeners per requests = [");
        for (final Map.Entry<CachedSpiceRequest<?>, Set<RequestListener<?>>> entry : mapRequestToRequestListener.entrySet()) {
            stringBuilder.append(entry.getKey().getClass().getName());
            stringBuilder.append(":");
            stringBuilder.append(entry.getKey());
            stringBuilder.append(" --> ");
            if (entry.getValue() == null) {
                stringBuilder.append(entry.getValue());
            } else {
                stringBuilder.append(entry.getValue().size());
            }
        }
        stringBuilder.append(']');

        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    public void addSpiceServiceListener(final SpiceServiceServiceListener spiceServiceServiceListener) {
        this.spiceServiceListenerSet.add(spiceServiceServiceListener);
    }

    public void removeSpiceServiceListener(final SpiceServiceServiceListener spiceServiceServiceListener) {
        this.spiceServiceListenerSet.add(spiceServiceServiceListener);
    }

    protected void notifyOfRequestProcessed(final CachedSpiceRequest<?> request) {
        Ln.v("Removing %s  size is %d", request, mapRequestToRequestListener.size());
        mapRequestToRequestListener.remove(request);

        checkAllRequestComplete();
        synchronized (spiceServiceListenerSet) {
            for (final SpiceServiceServiceListener spiceServiceServiceListener : spiceServiceListenerSet) {
                spiceServiceServiceListener.onRequestProcessed(request);
            }
        }
    }

    public int getPendingRequestCount() {
        return mapRequestToRequestListener.keySet().size();
    }
}
