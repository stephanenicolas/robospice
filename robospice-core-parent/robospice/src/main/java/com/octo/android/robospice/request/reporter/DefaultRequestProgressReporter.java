package com.octo.android.robospice.request.reporter;

import java.util.Set;

import roboguice.util.temp.Ln;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;

public class DefaultRequestProgressReporter implements RequestProgressReporter {
    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================
    private final Handler handlerResponse;

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================
    public DefaultRequestProgressReporter() {
        handlerResponse = new Handler(Looper.getMainLooper());
    }

    private void post(final Runnable r, final Object token) {
        handlerResponse.postAtTime(r, token, SystemClock.uptimeMillis());
    }

    @Override
    public <T> void notifyListenersOfRequestNotFound(final CachedSpiceRequest<T> request, 
            final Set<RequestListener<?>> listRequestListener) {
    
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

    @Override
    public <T> void notifyListenersOfRequestAdded(final CachedSpiceRequest<T> request,
            Set<RequestListener<?>> listeners) {
    }

    @Override
    public <T> void notifyListenersOfRequestProgress(final CachedSpiceRequest<T> request,
            final Set<RequestListener<?>> listeners,
            final RequestProgress progress) {

        post(new ProgressRunnable(listeners, progress),
                request.getRequestCacheKey());
    }

    @Override
    public <T> void notifyListenersOfRequestSuccess(final CachedSpiceRequest<T> request,
            final T result, final Set<RequestListener<?>> listeners) {

        post(new ResultRunnable<T>(listeners, result),
                request.getRequestCacheKey());
    }

    @Override
    public <T> void notifyListenersOfRequestFailure(final CachedSpiceRequest<T> request,
            final SpiceException e, final Set<RequestListener<?>> listeners) {

        post(new ResultRunnable<T>(listeners, e), request.getRequestCacheKey());
    }

    @Override
    public <T> void notifyListenersOfRequestCancellation(final CachedSpiceRequest<T> request,
            final Set<RequestListener<?>> listeners) {

        post(new ResultRunnable<T>(listeners, new RequestCancelledException(
                "Request has been cancelled explicitely.")),
                request.getRequestCacheKey());
    }

    @Override
    public <T> void clearNotificationsForRequest(final CachedSpiceRequest<T> request,
            final Set<RequestListener<?>> listeners) {

        handlerResponse.removeCallbacksAndMessages(request.getRequestCacheKey());
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
}
