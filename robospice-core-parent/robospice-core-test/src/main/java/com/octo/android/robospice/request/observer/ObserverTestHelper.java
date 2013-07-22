package com.octo.android.robospice.request.observer;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.concurrent.Semaphore;

import junit.framework.Assert;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestStatus;

public class ObserverTestHelper {

    private static final RequestStatus[] SUCCESSFUL_STATUSES_WITH_CACHE_KEY = new RequestStatus[] {
        RequestStatus.PENDING, RequestStatus.READING_FROM_CACHE, RequestStatus.LOADING_FROM_NETWORK,
        RequestStatus.COMPLETE };

    private static final RequestStatus[] SUCCESSFUL_STATUSES_WITHOUT_CACHE_KEY = new RequestStatus[] {
        RequestStatus.PENDING, RequestStatus.LOADING_FROM_NETWORK, RequestStatus.COMPLETE };

    public static enum ObserverResult {
        SUCCESS, FAILED, CANCELLED
    }

    public static RequestProgress eqStatus(RequestStatus status) {
        return new EqRequestProgressStatus(status);
    }

    public static class EqRequestProgressStatus extends RequestProgress {
        public EqRequestProgressStatus(RequestStatus status) {
            super(status);
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof RequestProgress)) {
                return false;
            }

            RequestProgress other = (RequestProgress) obj;

            return getStatus().equals(other.getStatus());
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    private ObserverManager observerManager;
    private Semaphore observerManagerThreadSemaphore;
    private RequestObserver[] observers;

    public ObserverTestHelper() throws InterruptedException {
        observers = null;

        observerManagerThreadSemaphore = new Semaphore(1);
        observerManagerThreadSemaphore.acquire();

        observerManager = new ObserverManager() {
            @Override
            protected void onStopped() {
                observerManagerThreadSemaphore.release();
            }
        };
    }

    public void shutdownObserverManagerAndWaitAndVerifyObservers() throws InterruptedException {
        observerManager.stop();
        observerManagerThreadSemaphore.acquire();

        if (observers != null) {
            for (RequestObserver observer : observers) {
                verify(observer);
            }
        }

        Assert.assertEquals("There should be no requests still being stored", 0,
                observerManager.getRequestToObserverMapCount());
    }

    public ObserverManager getObserverManager() {
        return observerManager;
    }

    public <RESULT> void createObserversForSuccess(int activeObserverCount, int inactiveObserverCount,
        CachedSpiceRequest<RESULT> activeRequest, RESULT resultData) {

        RequestStatus[] expectedProgressStatuses = null;

        if (activeRequest != null) {
            expectedProgressStatuses = (activeRequest.getRequestCacheKey() != null ? SUCCESSFUL_STATUSES_WITH_CACHE_KEY
                : SUCCESSFUL_STATUSES_WITHOUT_CACHE_KEY);
        }

        createObservers(activeObserverCount, inactiveObserverCount, activeRequest, resultData,
                expectedProgressStatuses, ObserverResult.SUCCESS);
    }

    public <RESULT> void createObserversForFailure(int activeObserverCount, int inactiveObserverCount,
        CachedSpiceRequest<RESULT> activeRequest, RESULT resultData) {

        RequestStatus[] expectedProgressStatuses = null;

        if (activeRequest != null) {
            expectedProgressStatuses = (activeRequest.getRequestCacheKey() != null ? SUCCESSFUL_STATUSES_WITH_CACHE_KEY
                : SUCCESSFUL_STATUSES_WITHOUT_CACHE_KEY);
        }

        createObservers(activeObserverCount, inactiveObserverCount, activeRequest, resultData,
                expectedProgressStatuses, ObserverResult.FAILED);
    }

    public <RESULT> void createObserversForCancellation(int activeObserverCount, int inactiveObserverCount,
        CachedSpiceRequest<RESULT> activeRequest, RESULT resultData) {

        RequestStatus[] expectedProgressStatuses = new RequestStatus[] {RequestStatus.PENDING, RequestStatus.COMPLETE};

        createObservers(activeObserverCount, inactiveObserverCount, activeRequest, resultData,
                expectedProgressStatuses, ObserverResult.CANCELLED);
    }

    private <RESULT> void createObservers(int activeObserverCount, int inactiveObserverCount,
        CachedSpiceRequest<RESULT> activeRequest, RESULT resultData, RequestStatus[] expectedStatuses,
        ObserverResult result) {

        observers = new RequestObserver[activeObserverCount];

        for (int i = 0; i < activeObserverCount; i++) {
            observerManager.registerObserver(new MyObserverFactory<RESULT>(observers, i, activeRequest, resultData,
                    expectedStatuses, result));
        }

        for (int i = activeObserverCount; i < activeObserverCount + inactiveObserverCount; i++) {
            observerManager.registerObserver(new MyInactiveObserverFactory());
        }
    }

    public static class AnySpiceException extends SpiceException {
        public AnySpiceException() {
            super(new Exception());
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof SpiceException);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    public static class MyObserverFactory<RESULT> implements RequestObserverFactory {

        private RequestObserver[] observers;
        private int observerIndex;
        private ObserverResult result;
        private CachedSpiceRequest<RESULT> activeRequest;
        private RESULT resultData;
        private RequestStatus[] expectedStatuses;

        public MyObserverFactory(RequestObserver[] observers, int observerIndex,
            CachedSpiceRequest<RESULT> activeRequest, RESULT resultData, RequestStatus[] expectedStatuses,
            ObserverResult result) {

            this.observers = observers;
            this.observerIndex = observerIndex;
            this.activeRequest = activeRequest;
            this.resultData = resultData;
            this.expectedStatuses = expectedStatuses;
            this.result = result;
        }

        @Override
        public RequestObserver create(CachedSpiceRequest<?> request) {
            RequestObserver observer = createStrictMock(RequestObserver.class);
            observer.onRequestAdded(activeRequest);

            for (RequestStatus expectedStatus : expectedStatuses) {
                observer.onRequestProgressUpdated(activeRequest, eqStatus(expectedStatus));
            }

            switch (result) {
                case SUCCESS:
                    observer.onRequestCompleted(activeRequest, resultData);
                    break;

                case FAILED:
                    observer.onRequestFailed(activeRequest, new AnySpiceException());
                    break;

                case CANCELLED:
                    observer.onRequestCancelled(activeRequest);
                    break;

                default:
                    break;
            }

            replay(observer);

            observers[observerIndex] = observer;
            return observer;
        }
    }

    public static class MyInactiveObserverFactory implements RequestObserverFactory {
        @Override
        public RequestObserver create(CachedSpiceRequest<?> request) {
            return null;
        }
    }
}
