package com.octo.android.robospice.request.tracker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestStatus;
import com.octo.android.robospice.request.observer.RequestObserver;
import com.octo.android.robospice.request.observer.RequestObserverFactory;

/**
 * The Request Tracker Service is a Request Observer that monitors the progress of all requests
 * and supports obtaining the list of currently active requests and their status.
 * 
 * @author Andrew.Clark
 *
 */
public class RequestTrackerService implements RequestObserver, RequestTracker {

    private static RequestTrackerService instance = new RequestTrackerService();

    private static RequestObserverFactory factoryInstance = new RequestObserverFactory() {
        @Override
        public RequestObserver create(CachedSpiceRequest<?> request) {
            return instance;
        }
    };

    private Map<CachedSpiceRequest<?>, RequestStatus> activeRequestStatus = Collections
            .synchronizedMap(new HashMap<CachedSpiceRequest<?>, RequestStatus>());

    // package private for tests
    RequestTrackerService() {
    }

    public static RequestTracker getRequestTracker() {
        return instance;
    }

    public static RequestObserverFactory getRequestTrackerFactory() {
        return factoryInstance;
    }

    @Override
    public Map<CachedSpiceRequest<?>, RequestStatus> getActiveRequests() {
        Map<CachedSpiceRequest<?>, RequestStatus> activeRequestsSnapshot = new HashMap<CachedSpiceRequest<?>, RequestStatus>();

        synchronized (activeRequestStatus) {
            for (Entry<CachedSpiceRequest<?>, RequestStatus> mapEntry : activeRequestStatus.entrySet())
            {
                activeRequestsSnapshot.put(mapEntry.getKey(), mapEntry.getValue());
            }
        }

        return activeRequestsSnapshot;
    }

    @Override
    public <RESULT> void onRequestCompleted(CachedSpiceRequest<RESULT> request,
            RESULT result) {

        activeRequestStatus.remove(request);
    }

    @Override
    public void onRequestFailed(CachedSpiceRequest<?> request, SpiceException e) {
        activeRequestStatus.remove(request);
    }

    @Override
    public void onRequestCancelled(CachedSpiceRequest<?> request) {
        activeRequestStatus.remove(request);
    }

    @Override
    public void onRequestProgressUpdated(CachedSpiceRequest<?> request,
            RequestProgress progress) {

        activeRequestStatus.put(request, progress.getStatus());
    }

    @Override
    public void onRequestAdded(CachedSpiceRequest<?> request) {
        // we get request pending immediately anyway so ignore
    }
}
