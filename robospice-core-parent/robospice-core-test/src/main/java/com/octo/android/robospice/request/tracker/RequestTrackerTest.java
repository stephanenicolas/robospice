package com.octo.android.robospice.request.tracker;

import java.util.Map;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestStatus;

import junit.framework.TestCase;

public class RequestTrackerTest extends TestCase {

    private RequestTrackerService requestMonitor;

    private CachedSpiceRequest<String> cachedRequest, cachedRequest2, uncachedRequest, uncachedRequest2;

    protected void setUp() throws Exception {
        requestMonitor = new RequestTrackerService();

        SpiceRequest<String> request = new SpiceRequest<String>(String.class) {
            @Override
            public String loadDataFromNetwork() throws Exception {
                return null;
            }
        };

        cachedRequest = new CachedSpiceRequest<String>(request, "123", 0);
        cachedRequest2 = new CachedSpiceRequest<String>(request, "345", 0);
        uncachedRequest = new CachedSpiceRequest<String>(request, null, 0);
        uncachedRequest2 = new CachedSpiceRequest<String>(request, null, 0);
    }

    public void testOneCachedPendingRequest() {
        assertEquals(0, requestMonitor.getActiveRequests().size());

        // add request with pending status
        requestMonitor.onRequestProgressUpdated(cachedRequest, new RequestProgress(RequestStatus.PENDING));

        assertEquals(1, requestMonitor.getActiveRequests().size());
        assertEquals(RequestStatus.PENDING, requestMonitor.getActiveRequests().values().toArray()[0]);
    }

    public void testOneCachedPendingRequestAndThenMarkAsLoadingFromNetwork() {
        testOneCachedPendingRequest();

        // add request with loading from network status
        requestMonitor.onRequestProgressUpdated(cachedRequest, new RequestProgress(RequestStatus.LOADING_FROM_NETWORK));

        assertEquals(1, requestMonitor.getActiveRequests().size());
        assertEquals(RequestStatus.LOADING_FROM_NETWORK, requestMonitor.getActiveRequests().values().toArray()[0]);
    }

    public void testOneCachedPendingRequestAndThenMarkAsLoadingFromNetworkThenCompleted() {
        testOneCachedPendingRequestAndThenMarkAsLoadingFromNetwork();

        // completed request
        requestMonitor.onRequestCompleted(cachedRequest, "Result!");

        // should be no request active now
        assertEquals(0, requestMonitor.getActiveRequests().size());
    }

    public void testOneCachedPendingRequestAndThenMarkAsLoadingFromNetworkThenFailed() {
        testOneCachedPendingRequestAndThenMarkAsLoadingFromNetwork();

        // fail request
        requestMonitor.onRequestFailed(cachedRequest, new SpiceException("ERROR"));

        // should be no request active now
        assertEquals(0, requestMonitor.getActiveRequests().size());
    }

    public void testOneCachedPendingRequestAndThenMarkAsLoadingFromNetworkThenCancelled() {
        testOneCachedPendingRequestAndThenMarkAsLoadingFromNetwork();

        // cancel request
        requestMonitor.onRequestCancelled(cachedRequest);

        // should be no request active now
        assertEquals(0, requestMonitor.getActiveRequests().size());
    }

    public void testOneCachedPendingRequestAndThenMarkAsLoadingFromNetworkThenAddAnotherRequest() {
        testOneCachedPendingRequestAndThenMarkAsLoadingFromNetwork();

        // add request with loading from network status
        requestMonitor.onRequestProgressUpdated(cachedRequest2, new RequestProgress(RequestStatus.PENDING));

        // should be 2 requests active now
        assertEquals(2, requestMonitor.getActiveRequests().size());

        assertEquals(RequestStatus.LOADING_FROM_NETWORK, requestMonitor.getActiveRequests().get(cachedRequest));
        assertEquals(RequestStatus.PENDING, requestMonitor.getActiveRequests().get(cachedRequest2));
    }

    public void testOneUncachedPendingRequest() {
        assertEquals(0, requestMonitor.getActiveRequests().size());

        // add request with pending status
        requestMonitor.onRequestProgressUpdated(uncachedRequest, new RequestProgress(RequestStatus.PENDING));

        assertEquals(1, requestMonitor.getActiveRequests().size());
        assertEquals(RequestStatus.PENDING, requestMonitor.getActiveRequests().values().toArray()[0]);
    }

    public void testOneUncachedPendingRequestAndThenMarkAsLoadingFromNetwork() {
        testOneUncachedPendingRequest();

        // add request with loading from network status
        requestMonitor.onRequestProgressUpdated(uncachedRequest,
                new RequestProgress(RequestStatus.LOADING_FROM_NETWORK));

        assertEquals(1, requestMonitor.getActiveRequests().size());
        assertEquals(RequestStatus.LOADING_FROM_NETWORK, requestMonitor.getActiveRequests().values().toArray()[0]);
    }

    public void testOneUncachedPendingRequestAndThenMarkAsLoadingFromNetworkThenAddAnotherRequest() {
        testOneUncachedPendingRequestAndThenMarkAsLoadingFromNetwork();

        // add request with loading from network status
        requestMonitor.onRequestProgressUpdated(uncachedRequest2, new RequestProgress(RequestStatus.PENDING));

        // should be 2 requests active now
        Map<CachedSpiceRequest<?>, RequestStatus> activeRequests = requestMonitor.getActiveRequests();

        assertEquals(2, activeRequests.size());
        assertEquals(RequestStatus.LOADING_FROM_NETWORK, activeRequests.get(uncachedRequest));
        assertEquals(RequestStatus.PENDING, activeRequests.get(uncachedRequest2));
    }

    public void testOneUncachedPendingRequestAndThenMarkAsLoadingFromNetworkThenAddAnotherRequestThenCancelFirst() {
        testOneUncachedPendingRequestAndThenMarkAsLoadingFromNetworkThenAddAnotherRequest();

        // add request with loading from network status
        requestMonitor.onRequestCancelled(uncachedRequest);

        // should be 1 requests active now
        Map<CachedSpiceRequest<?>, RequestStatus> activeRequests = requestMonitor.getActiveRequests();

        assertEquals(1, activeRequests.size());
        assertEquals(RequestStatus.PENDING, activeRequests.get(uncachedRequest2));
    }

    public void testActiveRequestMapIsntAffectedByRequestStatusChanges() {
        testOneCachedPendingRequest();

        // store active
        Map<CachedSpiceRequest<?>, RequestStatus> activeRequests = requestMonitor.getActiveRequests();

        // change request to be loading from network status
        requestMonitor.onRequestProgressUpdated(cachedRequest, new RequestProgress(RequestStatus.LOADING_FROM_NETWORK));

        // check our activeRequests isn't affected by change
        assertEquals(1, activeRequests.size());
        assertEquals(RequestStatus.PENDING, activeRequests.values().toArray()[0]);
    }

    public void testActiveRequestMapIsntAffectedByNewRequestAdded() {
        testOneCachedPendingRequest();

        // store active
        Map<CachedSpiceRequest<?>, RequestStatus> activeRequests = requestMonitor.getActiveRequests();

        // add new request with loading from network status
        requestMonitor.onRequestProgressUpdated(cachedRequest2, new RequestProgress(RequestStatus.LOADING_FROM_NETWORK));

        // check our activeRequests isn't affected by change
        assertEquals(1, activeRequests.size());
        assertEquals(RequestStatus.PENDING, activeRequests.values().toArray()[0]);
    }
}
