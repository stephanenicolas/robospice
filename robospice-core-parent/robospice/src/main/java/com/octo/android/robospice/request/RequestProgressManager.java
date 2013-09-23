package com.octo.android.robospice.request;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import roboguice.util.temp.Ln;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;
import com.octo.android.robospice.request.listener.SpiceServiceListener;
import com.octo.android.robospice.request.notifier.RequestListenerNotifier;
import com.octo.android.robospice.request.notifier.SpiceServiceListenerNotifier;

/**
 * Entity responsible for managing request's progress and notifying associated
 * listeners of any progress change.
 * @author Andrew Clark
 */
public class RequestProgressManager {
    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================
    private final Map<CachedSpiceRequest<?>, Set<RequestListener<?>>> mapRequestToRequestListener;

    private final RequestProcessorListener requestProcessorListener;
    private final RequestListenerNotifier requestListenerNotifier;
    private final SpiceServiceListenerNotifier spiceServiceListenerNotifier;

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================

    public RequestProgressManager(final RequestProcessorListener requestProcessorListener, Map<CachedSpiceRequest<?>, Set<RequestListener<?>>> mapRequestToRequestListener,
        final RequestListenerNotifier requestListenerNotifier, final SpiceServiceListenerNotifier spiceServiceListenerNotifier) {

        this.requestProcessorListener = requestProcessorListener;
        this.mapRequestToRequestListener = mapRequestToRequestListener;
        this.requestListenerNotifier = requestListenerNotifier;
        this.spiceServiceListenerNotifier = spiceServiceListenerNotifier;
    }

    public void notifyListenersOfRequestNotFound(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listeners) {
        Ln.d("Request was *NOT* found when adding request listeners to existing requests.");

        spiceServiceListenerNotifier.notifyObserversOfRequestNotFound(request);
        requestListenerNotifier.notifyListenersOfRequestNotFound(request, listeners);
    }

    public <T> void notifyListenersOfRequestAdded(CachedSpiceRequest<T> request, Set<RequestListener<?>> listeners) {
        Ln.d("Request was added to queue.");

        spiceServiceListenerNotifier.notifyObserversOfRequestAdded(request, listeners);
        requestListenerNotifier.notifyListenersOfRequestAdded(request, listeners);
        notifyListenersOfRequestProgress(request, listeners, request.getProgress());
    }

    public <T> void notifyListenersOfRequestAggregated(CachedSpiceRequest<T> request, Set<RequestListener<?>> listeners) {
        Ln.d("Request was aggregated in queue.");

        spiceServiceListenerNotifier.notifyObserversOfRequestAggregated(request, listeners);
        requestListenerNotifier.notifyListenersOfRequestAggregated(request, listeners);
        notifyListenersOfRequestProgress(request, listeners, request.getProgress());
    }

    protected <T> void notifyListenersOfRequestProgress(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listeners, final RequestStatus status) {
        notifyListenersOfRequestProgress(request, listeners, new RequestProgress(status));
        checkAllRequestComplete();
    }

    public <T> void notifyListenersOfRequestProgress(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listeners, final RequestProgress progress) {
        Ln.d("Sending progress %s", progress.getStatus());

        spiceServiceListenerNotifier.notifyObserversOfRequestProgress(request, progress);
        requestListenerNotifier.notifyListenersOfRequestProgress(request, listeners, progress);
        checkAllRequestComplete();
    }

    protected void checkAllRequestComplete() {
        if (mapRequestToRequestListener.isEmpty()) {
            Ln.d("Sending all request complete.");
            requestProcessorListener.allRequestComplete();
        }
    }

    public <T> void notifyListenersOfRequestSuccessButDontCompleteRequest(final CachedSpiceRequest<T> request, final T result) {
        final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);

        spiceServiceListenerNotifier.notifyObserversOfRequestSuccess(request);
        requestListenerNotifier.notifyListenersOfRequestSuccess(request, result, listeners);
    }

    public <T> void notifyListenersOfRequestSuccess(final CachedSpiceRequest<T> request, final T result) {
        final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
        notifyListenersOfRequestProgress(request, listeners, RequestStatus.COMPLETE);

        spiceServiceListenerNotifier.notifyObserversOfRequestSuccess(request);
        requestListenerNotifier.notifyListenersOfRequestSuccess(request, result, listeners);
        notifyOfRequestProcessed(request, listeners);
    }

    public <T> void notifyListenersOfRequestFailure(final CachedSpiceRequest<T> request, final SpiceException e) {
        final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
        notifyListenersOfRequestProgress(request, listeners, RequestStatus.COMPLETE);

        spiceServiceListenerNotifier.notifyObserversOfRequestFailure(request);
        requestListenerNotifier.notifyListenersOfRequestFailure(request, e, listeners);
        notifyOfRequestProcessed(request, listeners);
    }

    public void notifyListenersOfRequestCancellation(final CachedSpiceRequest<?> request) {
        Ln.d("Not calling network request : " + request + " as it is cancelled. ");
        final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
        notifyListenersOfRequestProgress(request, listeners, RequestStatus.COMPLETE);

        spiceServiceListenerNotifier.notifyObserversOfRequestCancellation(request);
        requestListenerNotifier.notifyListenersOfRequestCancellation(request, listeners);
        notifyOfRequestProcessed(request, listeners);
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

        requestListenerNotifier.clearNotificationsForRequest(request, setRequestListener);

        if (setRequestListener != null && listRequestListener != null) {
            Ln.d("Removing listeners of request : " + request.toString() + " : " + setRequestListener.size());
            setRequestListener.removeAll(listRequestListener);
        }
    }

    public void addSpiceServiceListener(final SpiceServiceListener spiceServiceListener) {
        this.spiceServiceListenerNotifier.addSpiceServiceListener(spiceServiceListener);
    }

    public void removeSpiceServiceListener(final SpiceServiceListener spiceServiceListener) {
        this.spiceServiceListenerNotifier.removeSpiceServiceListener(spiceServiceListener);
    }

    public void notifyOfRequestProcessed(final CachedSpiceRequest<?> request, Set<RequestListener<?>> listeners) {
        Ln.v("Removing %s  size is %d", request, mapRequestToRequestListener.size());
        mapRequestToRequestListener.remove(request);

        checkAllRequestComplete();
        spiceServiceListenerNotifier.notifyObserversOfRequestProcessed(request, listeners);
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
