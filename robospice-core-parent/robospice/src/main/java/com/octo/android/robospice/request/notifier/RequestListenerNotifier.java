package com.octo.android.robospice.request.notifier;

import java.util.Set;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.RequestProcessor;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;

/**
 * Defines the behavior of an entity that reports on progress processing inside
 * a given {@link RequestProcessor}.
 * @author SNI
 */
public interface RequestListenerNotifier {

    /**
     * Notify listeners that no pending request is found (used by
     * {@link SpiceManager#addListenerIfPending(Class, Object, com.octo.android.robospice.request.listener.PendingRequestListener)}
     * .
     * @param request
     *            a given request.
     * @param listeners
     *            a set of {@link RequestListener}. Only instances of
     *            {@link PendingRequestListener} will be notified of the event.
     */
    <T> void notifyListenersOfRequestNotFound(final CachedSpiceRequest<T> request, final Set<RequestListener<?>> listeners);

    /**
     * Notify listeners that a pending request has been found. This callback is
     * invoked when a request is added into request queue, not during aggregation to a pending request.
     * @param request
     *            a given request.
     * @param listeners
     *            a set of {@link RequestListener}.
     */
    <T> void notifyListenersOfRequestAdded(final CachedSpiceRequest<T> request, final Set<RequestListener<?>> listeners);

    /**
     * Notify listeners that a pending request has been found. This callback is
     * invoked when a request is aggregated to a pending request.
     * @param request
     *            a given request.
     * @param listeners
     *            a set of {@link RequestListener}.
     */
    <T> void notifyListenersOfRequestAggregated(final CachedSpiceRequest<T> request, final Set<RequestListener<?>> listeners);

    /**
     * Notify listeners of a request's success.
     * @param request
     *            a given request.
     * @param listeners
     *            a set of {@link RequestListener}.
     */
    <T> void notifyListenersOfRequestSuccess(final CachedSpiceRequest<T> request, final T result, final Set<RequestListener<?>> listeners);

    /**
     * Notify listeners of a request's failure.
     * @param request
     *            a given request.
     * @param listeners
     *            a set of {@link RequestListener}.
     */
    <T> void notifyListenersOfRequestFailure(final CachedSpiceRequest<T> request, final SpiceException e, final Set<RequestListener<?>> listeners);

    /**
     * Notify listeners of a request's cancelation.
     * @param request
     *            a given request.
     * @param listeners
     *            a set of {@link RequestListener}.
     */
    <T> void notifyListenersOfRequestCancellation(CachedSpiceRequest<T> request, final Set<RequestListener<?>> listeners);

    /**
     * Notify listeners of a request's progress.
     * @param request
     *            a given request.
     * @param listeners
     *            a set of {@link RequestListener}. Only
     *            {@link RequestProgressListener} will be notified of the event.
     */
    <T> void notifyListenersOfRequestProgress(final CachedSpiceRequest<T> request, final Set<RequestListener<?>> listeners, final RequestProgress progress);

    /**
     * Clears all pending notifications for a given request.
     * @param request
     *            the request not to notify listeners of.
     * @param listeners
     *            the listeners that won't be notified.
     */
    <T> void clearNotificationsForRequest(final CachedSpiceRequest<T> request, final Set<RequestListener<?>> listeners);
}
