package com.octo.android.robospice.request;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import roboguice.util.temp.Ln;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;
import com.octo.android.robospice.request.listener.SpiceServiceServiceListener;
import com.octo.android.robospice.request.observer.IObserverManager;

public class RequestProgressBroadcaster {
    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================
    private final Map<CachedSpiceRequest<?>, Set<RequestListener<?>>> mapRequestToRequestListener;

    private final Handler handlerResponse;
    private final Set<SpiceServiceServiceListener> spiceServiceListenerSet;

    private final RequestProcessorListener requestProcessorListener;
    private final IObserverManager observerManager;

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================

    public RequestProgressBroadcaster(IObserverManager observerManager,
            final RequestProcessorListener requestProcessorListener, Map<CachedSpiceRequest<?>, Set<RequestListener<?>>> mapRequestToRequestListener) {

        this.requestProcessorListener = requestProcessorListener;
        this.mapRequestToRequestListener = mapRequestToRequestListener;
        handlerResponse = new Handler(Looper.getMainLooper());
        spiceServiceListenerSet = Collections.synchronizedSet(new HashSet<SpiceServiceServiceListener>());
        this.observerManager = observerManager;
    }

    private void post(final Runnable r, final Object token) {
        handlerResponse.postAtTime(r, token, SystemClock.uptimeMillis());
    }

    protected void notifyListenersOfRequestNofFound(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listRequestListener) {
        Ln.d("Request was not found when adding request listeners to existing requests. Now try and call onRequestNotFound");
    
        for (final RequestListener<?> listener: listRequestListener) {
            if (listener instanceof PendingRequestListener) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        ((PendingRequestListener<?>) listener).onRequestNotFound();
                    }
                }, request.getRequestCacheKey());
            }
        }
    }

    protected <T> void notifyListenersOfRequestProgress(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listeners, final RequestStatus status) {
        notifyListenersOfRequestProgress(request, listeners, new RequestProgress(status));
    }

    protected <T> void notifyListenersOfRequestProgress(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listeners, final RequestProgress progress) {
        Ln.d("Sending progress %s", progress.getStatus());
        observerManager.notifyObserversOfRequestProgress(request, listeners, progress);

        post(new ProgressRunnable(listeners, progress), request.getRequestCacheKey());
        checkAllRequestComplete();
    }

    private void checkAllRequestComplete() {
        if (mapRequestToRequestListener.isEmpty()) {
            requestProcessorListener.allRequestComplete();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected <T> void notifyListenersOfRequestSuccessButDontCompleteRequest(final CachedSpiceRequest<T> request, final T result) {
        final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
        post(new ResultRunnable(listeners, result), request.getRequestCacheKey());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected <T> void notifyListenersOfRequestSuccess(final CachedSpiceRequest<T> request, final T result) {

        observerManager.notifyObserversOfRequestSuccess(request, result);

        final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
        notifyListenersOfRequestProgress(request, listeners, RequestStatus.COMPLETE);
        post(new ResultRunnable(listeners, result), request.getRequestCacheKey());
        notifyOfRequestProcessed(request);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected <T> void notifyListenersOfRequestFailure(final CachedSpiceRequest<T> request, final SpiceException e) {
        observerManager.notifyObserversOfRequestFailure(request, e);

        final Set<RequestListener<?>> listeners = mapRequestToRequestListener.get(request);
        notifyListenersOfRequestProgress(request, listeners, RequestStatus.COMPLETE);
        post(new ResultRunnable(listeners, e), request.getRequestCacheKey());
        notifyOfRequestProcessed(request);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void notifyListenersOfRequestCancellation(final CachedSpiceRequest<?> request, final Set<RequestListener<?>> listeners) {
        observerManager.notifyObserversOfRequestCancellation(request, listeners);

        Ln.d("Not calling network request : " + request + " as it is cancelled. ");
        notifyListenersOfRequestProgress(request, listeners, RequestStatus.COMPLETE);
        post(new ResultRunnable(listeners, new RequestCancelledException("Request has been cancelled explicitely.")), request.getRequestCacheKey());
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
        handlerResponse.removeCallbacksAndMessages(request.getRequestCacheKey());
        final Set<RequestListener<?>> setRequestListener = mapRequestToRequestListener.get(request);
        if (setRequestListener != null && listRequestListener != null) {
            Ln.d("Removing listeners of request : " + request.toString() + " : " + setRequestListener.size());
            setRequestListener.removeAll(listRequestListener);
        }
    }
    // ============================================================================================
    // PRIVATE
    // ============================================================================================

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
            synchronized (listeners) {
                for (final RequestListener<?> listener : listeners) {
                    if (listener != null && listener instanceof RequestProgressListener) {
                        Ln.v("Notifying %s", listener.getClass().getSimpleName());
                        ((RequestProgressListener) listener).onRequestProgressUpdate(progress);
                    }
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
            synchronized (listeners) {
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
    }




    public void addSpiceServiceListener(final SpiceServiceServiceListener spiceServiceServiceListener) {
        this.spiceServiceListenerSet.add(spiceServiceServiceListener);
    }

    public void removeSpiceServiceListener(final SpiceServiceServiceListener spiceServiceServiceListener) {
        this.spiceServiceListenerSet.remove(spiceServiceServiceListener);
    }

    protected void prepareObserversForRequest(final CachedSpiceRequest<?> request) {
        observerManager.prepareObserversForRequest(request);
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

    protected <T> RequestProgressListener createProgressListener(final CachedSpiceRequest<T> request) {
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
