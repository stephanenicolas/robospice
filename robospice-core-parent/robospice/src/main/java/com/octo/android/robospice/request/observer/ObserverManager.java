package com.octo.android.robospice.request.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;

import roboguice.util.temp.Ln;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestProgress;

/**
 * The Observer Manager manages observers and passes on request events to the interested observers 
 * @author Andrew.Clark
 *
 */
public class ObserverManager {

    private static final int MESSAGE_QUIT = 'X';

    private final Map<CachedSpiceRequest<?>, List<RequestObserver>> mapRequestToObservers = Collections
            .synchronizedMap(new HashMap<CachedSpiceRequest<?>, List<RequestObserver>>());

    private final Set<RequestObserverFactory> observerFactories = Collections
            .synchronizedSet(new HashSet<RequestObserverFactory>());

    private Handler messageQueue;
    private boolean isQuitLooperRequired = false;

    public ObserverManager() {
    }

    protected ObserverManager(Looper looper) {
        createMessageQueue(looper);
    }

    // package private
    int getRequestToObserverMapCount() {
        return mapRequestToObservers.size();
    }

    /**
     * Register an observer with the observer manager so that it'll be informed when a new request starts 
     * @param observerFactory
     */
    public void registerObserver(RequestObserverFactory observerFactory) {
        observerFactories.add(observerFactory);

        if (messageQueue == null) {
            startThread();
        }
    }

    private void startThread() {
        Ln.d("Started Observer Thread");

        HandlerThread thread = new HandlerThread(ObserverManager.class.getName());
        thread.start();

        isQuitLooperRequired = true;

        createMessageQueue(thread.getLooper());
    }

    protected void createMessageQueue(Looper looper) {
        messageQueue = new Handler(looper) {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == MESSAGE_QUIT) {
                    Ln.d("Observer Message Thread stopped");

                    if (isQuitLooperRequired) {
                        getLooper().quit();
                    }
                    onStopped();
                }
            }
        };
    }

    /**
     * Stop the observer manager and kill the thread when all messages have been processed
     */
    public void stop() {
        if (messageQueue != null) {
            messageQueue.sendEmptyMessageAtTime(MESSAGE_QUIT, SystemClock.uptimeMillis());
        } else {
            onStopped();
        }
    }

    /**
     * Inform the observers of a request. The observers can optionally observe the new request if required
     *
     * @param request
     */
    public void notifyObserversOfRequestAdded(CachedSpiceRequest<?> request) {
        List<RequestObserver> observersForRequest = createObserversForRequest(request);

        if (observersForRequest.size() > 0) {
            mapRequestToObservers.put(request, observersForRequest);

            post(new RequestAddedNotifier(request, observersForRequest));
        }
    }

    /**
     * Ask all the observers if they wish to observer a request and generate a set of interested observers
     * @param request
     *
     * @return List of interested observers or empty List if none interested
     *
     */
    private List<RequestObserver> createObserversForRequest(CachedSpiceRequest<?> request) {
        Ln.d("Creating observers for request");

        List<RequestObserver> observersForRequest = new ArrayList<RequestObserver>();
        RequestObserver newObserver;

        synchronized (observerFactories) {
            for (RequestObserverFactory observerFactory : observerFactories) {
                newObserver = observerFactory.create(request);

                if (newObserver != null) {
                    observersForRequest.add(newObserver);
                }
            }
        }

        Ln.d("Created %d observers", observersForRequest.size());
        return observersForRequest;
    }

    /**
     * Notify interested observers that the request failed
     * @param request
     * @param Exception
     */
    public void notifyObserversOfRequestFailure(CachedSpiceRequest<?> request, SpiceException e) {

        List<RequestObserver> observersForRequest = mapRequestToObservers.get(request);

        if (observersForRequest != null) {
            post(new RequestFailedNotifier(request, e, observersForRequest));

            // remove the request entry
            mapRequestToObservers.remove(request);
        }
    }

    /**
     * Notify interested observers that the request succeeded
     * @param request
     * @param result data
     */
    public <T> void notifyObserversOfRequestSuccess(CachedSpiceRequest<T> request, T result) {

        List<RequestObserver> observersForRequest = mapRequestToObservers.get(request);

        if (observersForRequest != null) {
            post(new RequestCompletedNotifier<T>(request, result, observersForRequest));

            // remove the request entry
            mapRequestToObservers.remove(request);
        }
    }

    /**
     * Notify interested observers that the request was cancelled
     * @param request
     */
    public void notifyObserversOfRequestCancellation(CachedSpiceRequest<?> request) {

        List<RequestObserver> observersForRequest = mapRequestToObservers.get(request);

        if (observersForRequest != null) {
            post(new RequestCancelledNotifier(request, observersForRequest));

            // remove the request entry
            mapRequestToObservers.remove(request);
        }
    }

    /**
     * Notify interested observers of request progress
     * @param request
     * @param progress
     */
    public void notifyObserversOfRequestProgress(CachedSpiceRequest<?> request, RequestProgress progress) {

        List<RequestObserver> observersForRequest = mapRequestToObservers.get(request);

        if (observersForRequest != null) {
            post(new RequestProgressNotifier(request, progress, observersForRequest));
        }
    }

    /**
     * Add the request update to the observer message queue
     * @param runnable
     */
    protected void post(Runnable runnable) {
        messageQueue.postAtTime(runnable, SystemClock.uptimeMillis());
    }

    protected void onStopped() {
    }

    /**
     * Runnable to inform interested observers of request added
     * @author Andrew.Clark
     *
     */
    private static class RequestAddedNotifier implements Runnable {
        private List<RequestObserver> observersForRequest;
        private CachedSpiceRequest<?> request;

        public RequestAddedNotifier(CachedSpiceRequest<?> request, List<RequestObserver> observersForRequest) {

            this.observersForRequest = observersForRequest;
            this.request = request;
        }

        public void run() {
            Ln.d("Processing request added: %s", request);

            for (RequestObserver observer : observersForRequest) {
                observer.onRequestAdded(request);
            }
        }
    }

    /**
     * Runnable to inform interested observers of request failed
     * @author Andrew.Clark
     *
     */
    private static class RequestFailedNotifier implements Runnable {
        private List<RequestObserver> observersForRequest;
        private CachedSpiceRequest<?> request;
        private SpiceException e;

        public RequestFailedNotifier(CachedSpiceRequest<?> request, SpiceException e,
                List<RequestObserver> observersForRequest) {

            this.observersForRequest = observersForRequest;
            this.request = request;
            this.e = e;
        }

        public void run() {
            Ln.d("Processing request failed: %s due to %s", request, ExceptionUtils.getRootCause(e));

            for (RequestObserver observer : observersForRequest) {
                observer.onRequestFailed(request, e);
            }
        }
    }

    /**
     * Runnable to inform interested observers of request completed
     * @author Andrew.Clark
     *
     * @param <T>
     */
    private static class RequestCompletedNotifier<T> implements Runnable {
        private List<RequestObserver> observersForRequest;
        private CachedSpiceRequest<T> request;
        private T result;

        public RequestCompletedNotifier(CachedSpiceRequest<T> request, T result,
                List<RequestObserver> observersForRequest) {

            this.observersForRequest = observersForRequest;
            this.request = request;
            this.result = result;
        }

        public void run() {
            Ln.d("Processing request completed: %s with result %s", request, result);

            for (RequestObserver observer : observersForRequest) {
                observer.onRequestCompleted(request, result);
            }
        }
    }

    /**
     * Runnable to inform interested observers of request cancelled
     * @author Andrew.Clark
     *
     */
    private static class RequestCancelledNotifier implements Runnable {
        private List<RequestObserver> observersForRequest;
        private CachedSpiceRequest<?> request;

        public RequestCancelledNotifier(CachedSpiceRequest<?> request, List<RequestObserver> observersForRequest) {
            this.observersForRequest = observersForRequest;
            this.request = request;
        }

        public void run() {
            Ln.d("Processing request cancelled: %s", request);

            for (RequestObserver observer : observersForRequest) {
                observer.onRequestCancelled(request);
            }
        }
    }

    /**
     * Runnable to inform interested observers of request progress
     * @author Andrew.Clark
     *
     */
    private static class RequestProgressNotifier implements Runnable {
        private List<RequestObserver> observersForRequest;
        private CachedSpiceRequest<?> request;
        private RequestProgress progress;

        public RequestProgressNotifier(CachedSpiceRequest<?> request, RequestProgress progress,
                List<RequestObserver> observersForRequest) {

            this.observersForRequest = observersForRequest;
            this.request = request;
            this.progress = progress;
        }

        public void run() {
            Ln.d("Processing request progress: %s with %f", request, progress.getProgress());

            for (RequestObserver observer : observersForRequest) {
                observer.onRequestProgressUpdated(request, progress);
            }
        }
    }
}
