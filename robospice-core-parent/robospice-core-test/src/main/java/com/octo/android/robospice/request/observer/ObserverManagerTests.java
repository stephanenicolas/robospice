package com.octo.android.robospice.request.observer;

import static com.octo.android.robospice.request.observer.ObserverTestHelper.eqStatus;
import junit.framework.TestCase;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestStatus;
import com.octo.android.robospice.stub.SpiceRequestSucceedingStub;

public class ObserverManagerTests extends TestCase {

    private static final String RETURNED_DATA = "";
    private static final int THREE_OBSERVERS = 3;
    private ObserverTestHelper observerTestHelper;
    private ObserverManager observerManager;
    private static SpiceRequest<String> stringRequest = new SpiceRequestSucceedingStub<String>(String.class, RETURNED_DATA);
    private static CachedSpiceRequest<String> request = new CachedSpiceRequest<String>(stringRequest, null, 0);

    @Override
    protected void setUp() throws Exception {
        observerTestHelper = new ObserverTestHelper();
        observerManager = observerTestHelper.getObserverManager();
    }

    @Override
    protected void tearDown() throws Exception {
        observerTestHelper.shutdownObserverManagerAndWaitAndVerifyObservers();
    }

    public void testNoObserversButStillRunOk() throws InterruptedException {
        observerTestHelper.createObserversForSuccess(0, 0, null, null);
        executeSuccessfulRequestSequence(request, RETURNED_DATA);
    }

    public void testOneWorkingObserver() throws InterruptedException {
        observerTestHelper.createObserversForSuccess(1, 0, request, RETURNED_DATA);
        executeSuccessfulRequestSequence(request, RETURNED_DATA);
    }

    public void testOneWorkingObserverWithFailedRequest()
        throws InterruptedException {

        observerTestHelper.createObserversForFailure(1, 0, request, RETURNED_DATA);
        executeFailedRequestSequence(request, RETURNED_DATA);
    }

    public void testThreeWorkingObservers() throws InterruptedException {
        observerTestHelper.createObserversForSuccess(THREE_OBSERVERS, 0, request, RETURNED_DATA);

        executeSuccessfulRequestSequence(request, RETURNED_DATA);
    }

    public void testThreeWorkingOneInactiveObservers()
        throws InterruptedException {

        observerTestHelper.createObserversForSuccess(THREE_OBSERVERS, 1, request, RETURNED_DATA);
        executeSuccessfulRequestSequence(request, RETURNED_DATA);
    }

    private <RESULT> void executeFailedRequestSequence(CachedSpiceRequest<RESULT> request, RESULT returnedData) {
        executeMainRequestSequence(request);
        observerManager.notifyObserversOfRequestFailure(request, new SpiceException("FAILED"));
    }

    private <RESULT> void executeSuccessfulRequestSequence(CachedSpiceRequest<RESULT> request, RESULT returnedData) {
        executeMainRequestSequence(request);
        observerManager.notifyObserversOfRequestSuccess(request, returnedData);
    }

    <RESULT> void executeMainRequestSequence(CachedSpiceRequest<RESULT> request) {
        observerManager.notifyObserversOfRequestAdded(request);
        observerManager.notifyObserversOfRequestProgress(request, eqStatus(RequestStatus.PENDING));
        observerManager.notifyObserversOfRequestProgress(request, eqStatus(RequestStatus.LOADING_FROM_NETWORK));
        observerManager.notifyObserversOfRequestProgress(request, eqStatus(RequestStatus.COMPLETE));
    }
}
