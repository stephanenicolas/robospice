package com.octo.android.robospice.request;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import roboguice.util.temp.Ln;
import android.content.Context;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.ICacheManager;
import com.octo.android.robospice.request.listener.RequestCancellationListener;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.SpiceServiceServiceListener;
import com.octo.android.robospice.request.reporter.RequestProgressReporter;

/**
 * Delegate class of the {@link SpiceService}, easier to test than an Android
 * {@link Service}.
 * @author jva
 */
public class RequestProcessor {
    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================

    private final Map<CachedSpiceRequest<?>, Set<RequestListener<?>>> mapRequestToRequestListener = Collections.synchronizedMap(new LinkedHashMap<CachedSpiceRequest<?>, Set<RequestListener<?>>>());
    private final RequestProgressManager progressMonitor;
    private final RequestRunner requestRunner;
    private final ICacheManager cacheManager;


    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================

    /**
     * Build a request processor using a custom. This feature has been
     * implemented follwing a feature request from Riccardo Ciovati.
     * @param context
     *            the context on which {@link SpiceRequest} will provide their
     *            results.
     * @param cacheManager
     *            the {@link CacheManager} that will be used to retrieve
     *            requests' result and store them.
     * @param executorService
     *            a custom {@link ExecutorService} that will be used to execute
     *            {@link SpiceRequest} .
     * @param requestProcessorListener
     *            a listener of the {@link RequestProcessor}, it will be
     *            notified when no more requests are left, typically allowing
     *            the {@link SpiceService} to stop itself.
     */
    public RequestProcessor(final Context context, final ICacheManager cacheManager,
            final ExecutorService executorService,
            final RequestProcessorListener requestProcessorListener,
            final NetworkStateChecker networkStateChecker,
            final RequestProgressReporter requestProgressReporter) {

        this.cacheManager = cacheManager;
        this.progressMonitor = new RequestProgressManager(requestProcessorListener, mapRequestToRequestListener, requestProgressReporter);
        this.requestRunner = new RequestRunner(context, cacheManager, executorService, progressMonitor, networkStateChecker);
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
                if (request.isProcessable()) {
                    Ln.d("Adding entry for type %s and cacheKey %s.", request.getResultType(), request.getRequestCacheKey());
                    listRequestListenerForThisRequest = Collections.synchronizedSet(new HashSet<RequestListener<?>>());
                    this.mapRequestToRequestListener.put(request, listRequestListenerForThisRequest);
                }
            } else {
                Ln.d("Request for type %s and cacheKey %s already exists.", request.getResultType(), request.getRequestCacheKey());
                aggregated = true;
            }

            if (listRequestListenerForThisRequest != null) {
                listRequestListenerForThisRequest.addAll(listRequestListener);
            }

            if (request.isProcessable()) {
                progressMonitor.notifyListenersOfRequestAdded(request, listRequestListener);
            } else if (listRequestListenerForThisRequest == null) {
                progressMonitor.notifyListenersOfRequestNotFound(request, listRequestListener);
            }
        }

        if (aggregated) {
            return;
        }

        final RequestCancellationListener requestCancellationListener = new RequestCancellationListener() {

            @Override
            public void onRequestCancelled() {
                mapRequestToRequestListener.remove(request);
                progressMonitor.notifyListenersOfRequestCancellation(request, listRequestListener);
            }
        };
        request.setRequestCancellationListener(requestCancellationListener);

        if (request.isCancelled()) {
            mapRequestToRequestListener.remove(request);
            progressMonitor.notifyListenersOfRequestCancellation(request, listRequestListener);
            return;
        } else if (!request.isProcessable()) {
            progressMonitor.notifyOfRequestProcessed(request);
            return;
        } else {
            requestRunner.executeRequest(request);
        }
    }

    /**
     * Disable request listeners notifications for a specific request.<br/>
     * All listeners associated to this request won't be called when request
     * will finish.<br/>
     * @param request
     *            Request on which you want to disable listeners
     * @param listRequestListener
     *            the collection of listeners associated to request not to be
     *            notified
     */
    public void dontNotifyRequestListenersForRequest(final CachedSpiceRequest<?> request, final Collection<RequestListener<?>> listRequestListener) {
        progressMonitor.dontNotifyRequestListenersForRequest(request, listRequestListener);
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
        return requestRunner.isFailOnCacheError();
    }

    public void setFailOnCacheError(final boolean failOnCacheError) {
        requestRunner.setFailOnCacheError(failOnCacheError);
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
        progressMonitor.addSpiceServiceListener(spiceServiceServiceListener);
    }

    public void removeSpiceServiceListener(final SpiceServiceServiceListener spiceServiceServiceListener) {
        progressMonitor.removeSpiceServiceListener(spiceServiceServiceListener);
    }
}
