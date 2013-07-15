package com.octo.android.robospice.request.reporter;

import java.util.Set;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;

public interface RequestProgressReporter {

    <T> void notifyListenersOfRequestNotFound(final CachedSpiceRequest<T> request,
            final Set<RequestListener<?>> listeners);

    <T> void notifyListenersOfRequestAdded(final CachedSpiceRequest<T> request,
            final Set<RequestListener<?>> listeners);

    <T> void notifyListenersOfRequestSuccess(final CachedSpiceRequest<T> request,
            final T result, final Set<RequestListener<?>> listeners);

    <T> void notifyListenersOfRequestFailure(final CachedSpiceRequest<T> request,
            final SpiceException e, final Set<RequestListener<?>> listeners);

    <T> void notifyListenersOfRequestCancellation(CachedSpiceRequest<T> request,
            final Set<RequestListener<?>> listeners);

    <T> void notifyListenersOfRequestProgress(final CachedSpiceRequest<T> request,
            final Set<RequestListener<?>> listeners,
            final RequestProgress progress);

    <T> void clearNotificationsForRequest(final CachedSpiceRequest<T> request, 
            final Set<RequestListener<?>> listeners);
}
