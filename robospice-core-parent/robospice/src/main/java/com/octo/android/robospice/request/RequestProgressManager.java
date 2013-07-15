package com.octo.android.robospice.request;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import roboguice.util.temp.Ln;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;
import com.octo.android.robospice.request.listener.SpiceServiceServiceListener;
import com.octo.android.robospice.request.reporter.RequestProgressReporter;

public class RequestProgressManager {
    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================
    private final Map<CachedSpiceRequest<?>, Set<RequestListener<?>>> mapRequestToRequestListener;

    private final Set<SpiceServiceServiceListener> spiceServiceListenerSet;
    private final RequestProcessorListener requestProcessorListener;
    private final RequestProgressReporter requestProgressReporter;

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================

    public RequestProgressManager(final RequestProcessorListener requestProcessorListener, 
        Map<CachedSpiceRequest<?>, Set<RequestListener<?>>> mapRequestToRequestListener, final RequestProgressReporter requestProgressReporter) {

        this.requestProcessorListener = requestProcessorListener;
        this.mapRequestToRequestListener = mapRequestToRequestListener;
        this.requestProgressReporter = requestProgressReporter;

        spiceServiceListenerSet = Collections.synchronizedSet(new HashSet<SpiceServiceServiceListener>());
    }

    public void notifyListenersOfRequestNotFound(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listeners) {
        Ln.d("Request was not found when adding request listeners to existing requests. Now try and call onRequestNotFound");
        requestProgressReporter.notifyListenersOfRequestNotFound(request, listeners);
    }

    public <T> void notifyListenersOfRequestAdded(CachedSpiceRequest<T> request,
            Set<RequestListener<?>> listeners) {

        requestProgressReporter.notifyListenersOfRequestAdded(request, listeners);
        notifyListenersOfRequestProgress(request, listeners, request.getProgress());
    }

    protected <T> void notifyListenersOfRequestProgress(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listeners, final RequestStatus status) {
        notifyListenersOfRequestProgress(request, listeners, new RequestProgress(status));
    }

    public <T> void notifyListenersOfRequestProgress(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listeners, final RequestProgress progress) {
        Ln.d("Sending progress %s", progress.getStatus());
        requestProgressReporter.notifyListenersOfRequestProgress(request, listeners, progress);
        checkAllRequestComplete();
    }

    protected void checkAllRequestComplete() {
        if (mapRequestToRequestListener.isEmpty()) {
            requestProcessorListener.allRequestComplete();
        }
    }

    public <T> void notifyListenersOfRequestSuccessButDontCompleteRequest(final CachedSpiceRequest<T> request, final T result) {
        final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
        requestProgressReporter.notifyListenersOfRequestSuccess(request, result, listeners);
    }

    public <T> void notifyListenersOfRequestSuccess(final CachedSpiceRequest<T> request, final T result) {

        final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
        notifyListenersOfRequestProgress(request, listeners, RequestStatus.COMPLETE);
        requestProgressReporter.notifyListenersOfRequestSuccess(request, result, listeners);
        notifyOfRequestProcessed(request);
    }

    public <T> void notifyListenersOfRequestFailure(final CachedSpiceRequest<T> request, final SpiceException e) {
        final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
        notifyListenersOfRequestProgress(request, listeners, RequestStatus.COMPLETE);

        requestProgressReporter.notifyListenersOfRequestFailure(request, e, listeners);
        notifyOfRequestProcessed(request);
    }

    public void notifyListenersOfRequestCancellation(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listeners) {
        Ln.d("Not calling network request : " + request + " as it is cancelled. ");
        notifyListenersOfRequestProgress(request, listeners, RequestStatus.COMPLETE);

        requestProgressReporter.notifyListenersOfRequestCancellation(request, listeners);
        notifyOfRequestProcessed(request);
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
        final Set<RequestListener<?>> setRequestListener = mapRequestToRequestListener.get(request);

        requestProgressReporter.clearNotificationsForRequest(request, setRequestListener);

        if (setRequestListener != null && listRequestListener != null) {
            Ln.d("Removing listeners of request : " + request.toString() + " : " + setRequestListener.size());
            setRequestListener.removeAll(listRequestListener);
        }
    }
    public void addSpiceServiceListener(final SpiceServiceServiceListener spiceServiceServiceListener) {
        this.spiceServiceListenerSet.add(spiceServiceServiceListener);
    }

    public void removeSpiceServiceListener(final SpiceServiceServiceListener spiceServiceServiceListener) {
        this.spiceServiceListenerSet.remove(spiceServiceServiceListener);
    }

    public void notifyOfRequestProcessed(final CachedSpiceRequest<?> request) {
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

    public <T> RequestProgressListener createProgressListener(final CachedSpiceRequest<T> request) {
        // add a progress listener to the request to be notified of
        // progress during load data from network
        final RequestProgressListener requestProgressListener = new RequestProgressListener() {
            @Override
            public void onRequestProgressUpdate(final RequestProgress progress) {
                final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
                notifyListenersOfRequestProgress(request, listeners, progress);
            }
        };
        return requestProgressListener;
    }
}
