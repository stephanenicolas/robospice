package com.octo.android.robospice.request.notifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import roboguice.util.temp.Ln;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.SpiceServiceListener;
import com.octo.android.robospice.request.listener.SpiceServiceListener.RequestProcessingContext;

/**
 * The Observer Manager manages observers and passes on request events to the
 * interested observers.
 * @author Andrew.Clark
 */
public class SpiceServiceListenerNotifier {

    private final List<SpiceServiceListener> spiceServiceListenerList = Collections.synchronizedList(new ArrayList<SpiceServiceListener>());

    private Handler messageQueue;

    public void addSpiceServiceListener(SpiceServiceListener spiceServiceListener) {
        spiceServiceListenerList.add(spiceServiceListener);
        if (messageQueue == null) {
            Ln.d("Message Queue starting");
            messageQueue = new Handler(Looper.getMainLooper());
        }
    }

    public void removeSpiceServiceListener(SpiceServiceListener spiceServiceListener) {
        spiceServiceListenerList.remove(spiceServiceListener);
    }

    // package private
    int getRequestToObserverMapCount() {
        return spiceServiceListenerList.size();
    }

    protected void createMessageQueue() {
        messageQueue = new Handler(Looper.getMainLooper());
    }

    /**
     * Inform the observers of a request. The observers can optionally observe
     * the new request if required
     * @param request
     */
    public void notifyObserversOfRequestNotFound(CachedSpiceRequest<?> request) {
        RequestProcessingContext requestProcessingContext = new RequestProcessingContext();
        requestProcessingContext.setExecutionThread(Thread.currentThread());
        post(new RequestNotFoundNotifier(request, spiceServiceListenerList, requestProcessingContext));
    }

    /**
     * Inform the observers of a request. The observers can optionally observe
     * the new request if required
     * @param request
     */
    public void notifyObserversOfRequestAdded(CachedSpiceRequest<?> request) {
        RequestProcessingContext requestProcessingContext = new RequestProcessingContext();
        requestProcessingContext.setExecutionThread(Thread.currentThread());
        post(new RequestAddedNotifier(request, spiceServiceListenerList, requestProcessingContext));
    }

    /**
     * Notify interested observers that the request failed
     * @param request
     * @param Exception
     */
    public void notifyObserversOfRequestFailure(CachedSpiceRequest<?> request) {
        RequestProcessingContext requestProcessingContext = new RequestProcessingContext();
        requestProcessingContext.setExecutionThread(Thread.currentThread());
        post(new RequestFailedNotifier(request, spiceServiceListenerList, requestProcessingContext));
    }

    /**
     * Notify interested observers that the request succeeded
     * @param request
     * @param result
     *            data
     */
    public <T> void notifyObserversOfRequestSuccess(CachedSpiceRequest<T> request) {
        RequestProcessingContext requestProcessingContext = new RequestProcessingContext();
        requestProcessingContext.setExecutionThread(Thread.currentThread());
        post(new RequestSucceededNotifier<T>(request, spiceServiceListenerList, requestProcessingContext));
    }

    /**
     * Notify interested observers that the request was cancelled
     * @param request
     */
    public void notifyObserversOfRequestCancellation(CachedSpiceRequest<?> request) {
        RequestProcessingContext requestProcessingContext = new RequestProcessingContext();
        requestProcessingContext.setExecutionThread(Thread.currentThread());
        post(new RequestCancelledNotifier(request, spiceServiceListenerList, requestProcessingContext));
    }

    /**
     * Notify interested observers of request progress
     * @param request
     * @param progress
     */
    public void notifyObserversOfRequestProgress(CachedSpiceRequest<?> request, RequestProgress requestProgress) {
        RequestProcessingContext requestProcessingContext = new RequestProcessingContext();
        requestProcessingContext.setExecutionThread(Thread.currentThread());
        requestProcessingContext.setRequestProgress(requestProgress);
        post(new RequestProgressNotifier(request, spiceServiceListenerList, requestProcessingContext));
    }

    /**
     * Add the request update to the observer message queue
     * @param runnable
     */
    protected void post(Runnable runnable) {
        Ln.d("Message queue is " + messageQueue);

        if (messageQueue == null) {
            return;
        }
        messageQueue.postAtTime(runnable, SystemClock.uptimeMillis());
    }

    protected void onStopped() {
    }

    /**
     * Runnable to inform interested observers of request added
     * @author Andrew.Clark
     */
    private static class RequestAddedNotifier implements Runnable {
        private List<SpiceServiceListener> spiceServiceListenerList;
        private CachedSpiceRequest<?> request;
        private RequestProcessingContext requestProcessingContext;

        public RequestAddedNotifier(CachedSpiceRequest<?> request, List<SpiceServiceListener> spiceServiceListenerList, RequestProcessingContext requestProcessingContext) {

            this.spiceServiceListenerList = spiceServiceListenerList;
            this.request = request;
            this.requestProcessingContext = requestProcessingContext;
        }

        @Override
        public void run() {
            Ln.d("Processing request added: %s", request);

            for (SpiceServiceListener listener : spiceServiceListenerList) {
                listener.onRequestAdded(request, requestProcessingContext);
            }
        }
    }

    /**
     * Runnable to inform interested observers of request not found
     * @author Andrew.Clark
     */
    private static class RequestNotFoundNotifier implements Runnable {
        private List<SpiceServiceListener> spiceServiceListenerList;
        private CachedSpiceRequest<?> request;
        private RequestProcessingContext requestProcessingContext;

        public RequestNotFoundNotifier(CachedSpiceRequest<?> request, List<SpiceServiceListener> spiceServiceListenerList, RequestProcessingContext requestProcessingContext) {

            this.spiceServiceListenerList = spiceServiceListenerList;
            this.request = request;
            this.requestProcessingContext = requestProcessingContext;
        }

        @Override
        public void run() {
            Ln.d("Processing request not found: %s", request);

            for (SpiceServiceListener listener : spiceServiceListenerList) {
                listener.onRequestNotFound(request, requestProcessingContext);
            }
        }
    }

    /**
     * Runnable to inform interested observers of request failed
     * @author Andrew.Clark
     */
    private static class RequestFailedNotifier implements Runnable {
        private List<SpiceServiceListener> spiceServiceListenerList;
        private CachedSpiceRequest<?> request;
        private RequestProcessingContext requestProcessingContext;

        public RequestFailedNotifier(CachedSpiceRequest<?> request, List<SpiceServiceListener> spiceServiceListenerList, RequestProcessingContext requestProcessingContext) {

            this.spiceServiceListenerList = spiceServiceListenerList;
            this.request = request;
            this.requestProcessingContext = requestProcessingContext;
        }

        @Override
        public void run() {
            for (SpiceServiceListener listener : spiceServiceListenerList) {
                listener.onRequestFailed(request, requestProcessingContext);
            }
        }
    }

    /**
     * Runnable to inform interested observers of request completed
     * @author Andrew.Clark
     * @param <T>
     */
    private static class RequestSucceededNotifier<T> implements Runnable {
        private List<SpiceServiceListener> spiceServiceListenerList;
        private CachedSpiceRequest<T> request;
        private RequestProcessingContext requestProcessingContext;

        public RequestSucceededNotifier(CachedSpiceRequest<T> request, List<SpiceServiceListener> spiceServiceListenerList, RequestProcessingContext requestProcessingContext) {

            this.spiceServiceListenerList = spiceServiceListenerList;
            this.request = request;
            this.requestProcessingContext = requestProcessingContext;
        }

        @Override
        public void run() {
            for (SpiceServiceListener listener : spiceServiceListenerList) {
                listener.onRequestSucceeded(request, requestProcessingContext);
            }
        }
    }

    /**
     * Runnable to inform interested observers of request cancelled
     * @author Andrew.Clark
     */
    private static class RequestCancelledNotifier implements Runnable {
        private List<SpiceServiceListener> spiceServiceListenerList;
        private CachedSpiceRequest<?> request;
        private RequestProcessingContext requestProcessingContext;

        public RequestCancelledNotifier(CachedSpiceRequest<?> request, List<SpiceServiceListener> spiceServiceListenerList, RequestProcessingContext requestProcessingContext) {
            this.spiceServiceListenerList = spiceServiceListenerList;
            this.request = request;
            this.requestProcessingContext = requestProcessingContext;
        }

        @Override
        public void run() {
            Ln.d("Processing request cancelled: %s", request);

            for (SpiceServiceListener listener : spiceServiceListenerList) {
                listener.onRequestCancelled(request, requestProcessingContext);
            }
        }
    }

    /**
     * Runnable to inform interested observers of request progress
     * @author Andrew.Clark
     */
    private static class RequestProgressNotifier implements Runnable {
        private List<SpiceServiceListener> spiceServiceListenerList;
        private CachedSpiceRequest<?> request;
        private RequestProcessingContext requestProcessingContext;

        public RequestProgressNotifier(CachedSpiceRequest<?> request, List<SpiceServiceListener> spiceServiceListenerList, RequestProcessingContext requestProcessingContext) {

            this.spiceServiceListenerList = spiceServiceListenerList;
            this.request = request;
            this.requestProcessingContext = requestProcessingContext;
        }

        @Override
        public void run() {
            for (SpiceServiceListener listener : spiceServiceListenerList) {
                listener.onRequestProgressUpdated(request, requestProcessingContext);
            }
        }
    }
}
