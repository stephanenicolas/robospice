package com.octo.android.robospice;

import roboguice.util.temp.Ln;
import android.content.Intent;
import android.test.InstrumentationTestCase;

import com.octo.android.robospice.core.test.SpiceTestService;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestStatus;
import com.octo.android.robospice.stub.RequestListenerStub;
import com.octo.android.robospice.stub.RequestListenerWithProgressHistoryStub;
import com.octo.android.robospice.stub.RequestListenerWithProgressStub;
import com.octo.android.robospice.stub.SpiceRequestFailingStub;
import com.octo.android.robospice.stub.SpiceRequestStub;
import com.octo.android.robospice.stub.SpiceRequestSucceedingStub;

public class SpiceManagerTest extends InstrumentationTestCase {

    private static final int SERVICE_TIME_OUT_WHEN_THROW_EXCEPTION = 1000;
    private static final Class<String> TEST_CLASS = String.class;
    private static final Class<Integer> TEST_CLASS2 = Integer.class;
    private static final String TEST_CACHE_KEY = "12345";
    private static final String TEST_CACHE_KEY2 = "123456";
    private static final long TEST_DURATION = DurationInMillis.ALWAYS_EXPIRED;
    private static final String TEST_RETURNED_DATA = "coucou";
    private static final long WAIT_BEFORE_EXECUTING_REQUEST_LARGE = 500;
    private static final long WAIT_BEFORE_EXECUTING_REQUEST_SHORT = 200;
    private static final long REQUEST_COMPLETION_TIME_OUT = 4000;
    private static final long SPICE_MANAGER_WAIT_TIMEOUT = 500;

    private SpiceManagerUnderTest spiceManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        spiceManager = new SpiceManagerUnderTest(SpiceTestService.class);
    }

    @Override
    protected void tearDown() throws Exception {
        if (spiceManager != null && spiceManager.isStarted()) {
            spiceManager.cancelAllRequests();
            spiceManager.removeAllDataFromCache();
            spiceManager.shouldStopAndJoin(SPICE_MANAGER_WAIT_TIMEOUT);
            spiceManager = null;
        }
        getInstrumentation().getTargetContext().stopService(new Intent(getInstrumentation().getTargetContext(), SpiceTestService.class));
        super.tearDown();
    }

    public void test_execute_should_fail_if_not_started() {
        // given

        // when
        try {
            spiceManager.execute(new CachedSpiceRequest<String>((SpiceRequest<String>) null, null, DurationInMillis.ALWAYS_RETURNED), null);
            // then
            fail();
        } catch (Exception ex) {
            Ln.d(ex);
            // then
            assertTrue(true);
        }
    }

    public void test_execute_should_stop_if_started_with_null_context() throws InterruptedException {
        // given

        // when
        spiceManager.start(null);

        // then
        assertNull(spiceManager.getException(SERVICE_TIME_OUT_WHEN_THROW_EXCEPTION));
        assertFalse(spiceManager.isStarted());
    }

    public void test_execute_should_succeed_if_started_from_context_with_declared_service_and_permissions() throws InterruptedException {
        // given

        // when
        spiceManager.start(getInstrumentation().getContext());
        assertNull(spiceManager.getException(SERVICE_TIME_OUT_WHEN_THROW_EXCEPTION));
    }

    public void test_execute_should_fail_if_stopped() throws InterruptedException {
        // given
        spiceManager.start(getInstrumentation().getTargetContext());
        spiceManager.shouldStopAndJoin(SPICE_MANAGER_WAIT_TIMEOUT);

        // when
        try {
            spiceManager.execute(new CachedSpiceRequest<String>((SpiceRequest<String>) null, null, DurationInMillis.ALWAYS_RETURNED), null);
            // then
            fail();
        } catch (Exception ex) {
            // then
            assertTrue(true);
        }
    }

    public void test_execute_executes_1_request_that_succeeds() throws InterruptedException {
        // when
        spiceManager.start(getInstrumentation().getTargetContext());
        SpiceRequestStub<String> spiceRequestStub = new SpiceRequestSucceedingStub<String>(TEST_CLASS, TEST_RETURNED_DATA);
        RequestListenerStub<String> requestListenerStub = new RequestListenerStub<String>();

        // when
        spiceManager.execute(spiceRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub);
        requestListenerStub.await(REQUEST_COMPLETION_TIME_OUT);

        // test
        assertTrue(spiceRequestStub.isLoadDataFromNetworkCalled());
        assertTrue(requestListenerStub.isSuccessful());
        assertTrue(requestListenerStub.isExecutedInUIThread());
    }

    public void test_execute_executes_1_request_that_fails() throws InterruptedException {
        // when
        spiceManager.start(getInstrumentation().getTargetContext());
        SpiceRequestStub<String> spiceRequestStub = new SpiceRequestFailingStub<String>(TEST_CLASS);
        RequestListenerStub<String> requestListenerStub = new RequestListenerStub<String>();

        // when
        spiceManager.execute(spiceRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub);
        spiceRequestStub.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);
        requestListenerStub.await(REQUEST_COMPLETION_TIME_OUT);

        // test
        assertTrue(spiceRequestStub.isLoadDataFromNetworkCalled());
        assertTrue(requestListenerStub.isExecutedInUIThread());
        assertFalse(requestListenerStub.isSuccessful());
    }

    public void test_execute_without_using_cache() throws InterruptedException {

        // when
        // this test is complex : we rely on the fact that the SpiceService as a
        // IntegerPersister that
        // always return a value in cache. Nevertheless, the request will fail
        // as we don't use cache.
        spiceManager.start(getInstrumentation().getTargetContext());
        SpiceRequestStub<Integer> spiceRequestStub = new SpiceRequestFailingStub<Integer>(TEST_CLASS2);
        RequestListenerStub<Integer> requestListenerStub = new RequestListenerStub<Integer>();

        // when
        spiceManager.execute(spiceRequestStub, requestListenerStub);
        spiceRequestStub.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);
        requestListenerStub.await(REQUEST_COMPLETION_TIME_OUT);

        // test
        assertTrue(spiceRequestStub.isLoadDataFromNetworkCalled());
        assertTrue(requestListenerStub.isExecutedInUIThread());
        assertFalse(requestListenerStub.isSuccessful());
    }

    public void test_execute_rejecting_cache() throws InterruptedException {
        // same as above but precise cache usage

        // when
        // this test is complex : we rely on the fact that the SpiceService as a
        // IntegerPersister that
        // always return a value in cache. Nevertheless, the request will fail
        // as we don't use cache.
        spiceManager.start(getInstrumentation().getTargetContext());
        SpiceRequestStub<Integer> spiceRequestStub = new SpiceRequestFailingStub<Integer>(TEST_CLASS2);
        RequestListenerStub<Integer> requestListenerStub = new RequestListenerStub<Integer>();

        // when
        spiceManager.execute(spiceRequestStub, "", DurationInMillis.ALWAYS_EXPIRED, requestListenerStub);
        spiceRequestStub.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);
        requestListenerStub.await(REQUEST_COMPLETION_TIME_OUT);

        // test
        assertTrue(spiceRequestStub.isLoadDataFromNetworkCalled());
        assertTrue(requestListenerStub.isExecutedInUIThread());
        assertFalse(requestListenerStub.isSuccessful());
    }

    public void test_getFromCacheAndLoadFromNetworkIfExpired_should_return_cache_data_if_not_expired_and_not_go_to_network()
        throws InterruptedException {
        // same as above but precise cache usage

        // when
        // this test is complex : we rely on the fact that the SpiceService as a
        // IntegerPersister that
        // always return a value in cache. Nevertheless, the request will fail
        // as we will finally get data from network (and fail) after getting it
        // from cache.
        spiceManager.start(getInstrumentation().getTargetContext());
        SpiceRequestStub<Integer> spiceRequestStub = new SpiceRequestFailingStub<Integer>(TEST_CLASS2);
        RequestListenerWithProgressStub<Integer> requestListenerStub = new RequestListenerWithProgressStub<Integer>();

        // when
        spiceManager.getFromCacheAndLoadFromNetworkIfExpired(spiceRequestStub, "", DurationInMillis.ONE_SECOND, requestListenerStub);
        spiceRequestStub.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);
        requestListenerStub.awaitComplete(REQUEST_COMPLETION_TIME_OUT);
        requestListenerStub.await(REQUEST_COMPLETION_TIME_OUT);

        // test
        assertFalse(spiceRequestStub.isLoadDataFromNetworkCalled());
        assertTrue(requestListenerStub.isSuccessful());
        assertTrue(requestListenerStub.isComplete());
    }

    public void test_getFromCacheAndLoadFromNetworkIfExpired_should_return_cache_data_if_expired_and_go_to_network() throws InterruptedException {
        // same as above but precise cache usage

        // when
        // this test is complex : we rely on the fact that the SpiceService as a
        // IntegerPersister that
        // always return a value in cache. Nevertheless, the request will fail
        // as we will finally get data from network (and fail) after getting it
        // from cache.
        spiceManager.start(getInstrumentation().getTargetContext());
        SpiceRequestStub<Integer> spiceRequestStub = new SpiceRequestFailingStub<Integer>(TEST_CLASS2);
        RequestListenerWithProgressStub<Integer> requestListenerStub = new RequestListenerWithProgressStub<Integer>();

        // when
        spiceManager.getFromCacheAndLoadFromNetworkIfExpired(spiceRequestStub, "", DurationInMillis.ONE_MINUTE * 2, requestListenerStub);
        spiceRequestStub.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);
        requestListenerStub.awaitComplete(REQUEST_COMPLETION_TIME_OUT);

        // test
        assertTrue(spiceRequestStub.isLoadDataFromNetworkCalled());
        assertTrue(requestListenerStub.isExecutedInUIThread());
        assertTrue(requestListenerStub.isComplete());

    }

    public void test_cancel_cancels_1_request() throws InterruptedException {
        // given
        SpiceRequestStub<String> spiceRequestStub = new SpiceRequestSucceedingStub<String>(String.class, TEST_RETURNED_DATA);
        spiceManager.start(getInstrumentation().getTargetContext());
        // when
        spiceManager.cancel(spiceRequestStub);
        Thread.sleep(REQUEST_COMPLETION_TIME_OUT);

        // test
        assertTrue(spiceRequestStub.isCancelled());
    }

    public void test_cancelAllRequests_cancels_2_requests() throws InterruptedException {
        // given
        spiceManager.start(getInstrumentation().getTargetContext());
        SpiceRequestStub<String> spiceRequestStub = new SpiceRequestFailingStub<String>(TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST_LARGE);
        SpiceRequestStub<String> spiceRequestStub2 = new SpiceRequestFailingStub<String>(TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST_LARGE);
        RequestListenerWithProgressStub<String> requestListenerStub = new RequestListenerWithProgressStub<String>();
        RequestListenerWithProgressStub<String> requestListenerStub2 = new RequestListenerWithProgressStub<String>();

        // when
        spiceManager.execute(spiceRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub);
        spiceManager.execute(spiceRequestStub2, TEST_CACHE_KEY2, TEST_DURATION, requestListenerStub2);
        spiceManager.cancelAllRequests();

        requestListenerStub.awaitComplete(REQUEST_COMPLETION_TIME_OUT);
        requestListenerStub2.awaitComplete(REQUEST_COMPLETION_TIME_OUT);

        requestListenerStub.await(REQUEST_COMPLETION_TIME_OUT);
        requestListenerStub2.await(REQUEST_COMPLETION_TIME_OUT);

        // spiceRequestStub.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);
        // spiceRequestStub2.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);

        // test
        assertTrue(spiceRequestStub.isCancelled());
        assertTrue(spiceRequestStub2.isCancelled());
        // can we guarantee that ? If cancel is too fast, spiceManager won't
        // have passed the request to spice service.
        assertTrue(requestListenerStub.isComplete());
        assertTrue(requestListenerStub2.isComplete());
        assertFalse(requestListenerStub.isSuccessful());
        assertFalse(requestListenerStub2.isSuccessful());
        System.out.println(requestListenerStub.getReceivedException());
        System.out.println(requestListenerStub2.getReceivedException());
        assertTrue(requestListenerStub.getReceivedException() instanceof RequestCancelledException);
        assertTrue(requestListenerStub2.getReceivedException() instanceof RequestCancelledException);
    }

    public void test_addListenerIfPending_receives_no_events_when_there_is_no_request_pending() throws InterruptedException {
        // given
        spiceManager.start(getInstrumentation().getTargetContext());
        SpiceRequestStub<String> spiceRequestStub = new SpiceRequestFailingStub<String>(TEST_CLASS);
        RequestListenerWithProgressStub<String> requestListenerStub = new RequestListenerWithProgressStub<String>();

        // when
        spiceManager.addListenerIfPending(TEST_CLASS, TEST_CACHE_KEY, requestListenerStub);

        spiceRequestStub.awaitForLoadDataFromNetworkIsCalled(WAIT_BEFORE_EXECUTING_REQUEST_LARGE + REQUEST_COMPLETION_TIME_OUT);

        // test
        assertNull(requestListenerStub.isSuccessful());
        assertFalse(requestListenerStub.isComplete());
        assertNull(requestListenerStub.getReceivedException());
    }

    public void test_shouldStop_stops_requests_immediatly() throws InterruptedException {
        // given
        spiceManager.start(getInstrumentation().getTargetContext());
        SpiceRequestStub<String> spiceRequestStub = new SpiceRequestFailingStub<String>(TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST_LARGE);
        SpiceRequestStub<String> spiceRequestStub2 = new SpiceRequestFailingStub<String>(TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST_LARGE);
        RequestListenerStub<String> requestListenerStub = new RequestListenerStub<String>();
        RequestListenerStub<String> requestListenerStub2 = new RequestListenerStub<String>();

        // when
        spiceManager.execute(spiceRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub);
        spiceManager.execute(spiceRequestStub2, TEST_CACHE_KEY2, TEST_DURATION, requestListenerStub2);
        spiceManager.shouldStop();

        spiceRequestStub.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);
        spiceRequestStub2.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);

        // test
        // no guarantee on that
        // assertTrue( spiceRequestStub.isLoadDataFromNetworkCalled() );
        // assertTrue( spiceRequestStub2.isLoadDataFromNetworkCalled() );
        assertNull(requestListenerStub.isSuccessful());
        assertNull(requestListenerStub2.isSuccessful());
    }

    public void test_shouldStop_doesnt_notify_listeners_after_requests_are_executed() throws InterruptedException {
        // given
        spiceManager.start(getInstrumentation().getTargetContext());
        SpiceRequestSucceedingStub<String> spiceRequestStub = new SpiceRequestSucceedingStub<String>(TEST_CLASS, TEST_RETURNED_DATA,
            WAIT_BEFORE_EXECUTING_REQUEST_LARGE);
        SpiceRequestSucceedingStub<String> spiceRequestStub2 = new SpiceRequestSucceedingStub<String>(TEST_CLASS, TEST_RETURNED_DATA,
            WAIT_BEFORE_EXECUTING_REQUEST_LARGE);
        RequestListenerStub<String> requestListenerStub = new RequestListenerStub<String>();
        RequestListenerStub<String> requestListenerStub2 = new RequestListenerStub<String>();

        // when
        spiceManager.execute(spiceRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub);
        spiceManager.execute(spiceRequestStub2, TEST_CACHE_KEY2, TEST_DURATION, requestListenerStub2);

        // wait for requests begin to be executed
        spiceRequestStub.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);
        spiceRequestStub2.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);
        // stop before
        spiceManager.shouldStop();

        requestListenerStub.await(REQUEST_COMPLETION_TIME_OUT);
        requestListenerStub2.await(REQUEST_COMPLETION_TIME_OUT);

        // test
        assertTrue(spiceRequestStub.isLoadDataFromNetworkCalled());
        assertTrue(spiceRequestStub2.isLoadDataFromNetworkCalled());
        assertNull(requestListenerStub.isSuccessful());
        assertNull(requestListenerStub2.isSuccessful());
    }

    public void test_dontNotifyRequestListenersForRequest_stops_only_targeted_request() throws InterruptedException {
        // given
        spiceManager.start(getInstrumentation().getTargetContext());
        SpiceRequestStub<String> spiceRequestStub = new SpiceRequestFailingStub<String>(TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST_LARGE);
        SpiceRequestStub<String> spiceRequestStub2 = new SpiceRequestFailingStub<String>(TEST_CLASS);
        RequestListenerStub<String> requestListenerStub = new RequestListenerStub<String>();
        RequestListenerStub<String> requestListenerStub2 = new RequestListenerStub<String>();

        // when
        spiceManager.execute(spiceRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub);
        spiceManager.dontNotifyRequestListenersForRequestInternal(spiceRequestStub);
        spiceManager.execute(spiceRequestStub2, TEST_CACHE_KEY2, TEST_DURATION, requestListenerStub2);

        spiceRequestStub.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);
        spiceRequestStub2.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);
        requestListenerStub2.await(REQUEST_COMPLETION_TIME_OUT);

        // test
        assertTrue(spiceRequestStub.isLoadDataFromNetworkCalled());
        assertTrue(spiceRequestStub2.isLoadDataFromNetworkCalled());
        assertNull(requestListenerStub.isSuccessful());
        assertFalse(requestListenerStub2.isSuccessful());
    }

    public void test_dontNotifyAnyRequestListeners_doesnt_notify_listeners_asap() throws InterruptedException {
        // given
        spiceManager.start(getInstrumentation().getTargetContext());
        SpiceRequestFailingStub<String> spiceRequestStub = new SpiceRequestFailingStub<String>(TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST_SHORT);
        SpiceRequestFailingStub<String> spiceRequestStub2 = new SpiceRequestFailingStub<String>(TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST_SHORT);
        RequestListenerStub<String> requestListenerStub = new RequestListenerStub<String>();
        RequestListenerStub<String> requestListenerStub2 = new RequestListenerStub<String>();

        // when

        spiceManager.execute(spiceRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub);
        spiceManager.execute(spiceRequestStub2, TEST_CACHE_KEY2, TEST_DURATION, requestListenerStub2);
        spiceManager.dontNotifyAnyRequestListenersInternal();

        spiceManager.dumpState();
        spiceRequestStub.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);
        spiceRequestStub2.awaitForLoadDataFromNetworkIsCalled(REQUEST_COMPLETION_TIME_OUT);

        // test
        // no guarantee on that
        // assertTrue( spiceRequestStub.isLoadDataFromNetworkCalled() );
        // assertTrue( spiceRequestStub2.isLoadDataFromNetworkCalled() );

        assertNull(requestListenerStub.isSuccessful());
        assertNull(requestListenerStub2.isSuccessful());
    }

    public void test_should_receive_request_progress_updates_in_right_order() throws InterruptedException {
        // TDD test for issue 36
        // given
        spiceManager.start(getInstrumentation().getTargetContext());
        SpiceRequestStub<String> spiceRequestStub = new SpiceRequestSucceedingStub<String>(TEST_CLASS, TEST_RETURNED_DATA);
        RequestListenerWithProgressHistoryStub<String> requestListenerStub = new RequestListenerWithProgressHistoryStub<String>();

        // when
        spiceManager.execute(spiceRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub);

        requestListenerStub.awaitComplete(REQUEST_COMPLETION_TIME_OUT);
        requestListenerStub.await(REQUEST_COMPLETION_TIME_OUT);

        // test
        assertTrue(requestListenerStub.isComplete());
        final int expectedRequestProgressCount = 4;
        synchronized (requestListenerStub.getRequestProgressesHistory()) {
            for (RequestProgress requestProgress : requestListenerStub.getRequestProgressesHistory()) {
                Ln.d("RequestProgress received : %s", requestProgress.getStatus());
            }
        }
        assertEquals(expectedRequestProgressCount, requestListenerStub.getRequestProgressesHistory().size());
        int progressStatusIndex = 0;
        assertEquals(RequestStatus.PENDING, requestListenerStub.getRequestProgressesHistory().get(progressStatusIndex++).getStatus());
        assertEquals(RequestStatus.LOADING_FROM_NETWORK, requestListenerStub.getRequestProgressesHistory().get(progressStatusIndex++).getStatus());
        assertEquals(RequestStatus.WRITING_TO_CACHE, requestListenerStub.getRequestProgressesHistory().get(progressStatusIndex++).getStatus());
        assertEquals(RequestStatus.COMPLETE, requestListenerStub.getRequestProgressesHistory().get(progressStatusIndex++).getStatus());
        assertTrue(requestListenerStub.isSuccessful());
    }

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------

    /**
     * Class under test. Just a wrapper to get any exception that can occur in the spicemanager's thread. Inspired by http://stackoverflow.com/questions/
     * 2596493/junit-assert-in-thread-throws-exception/13712829#13712829
     */
    private final class SpiceManagerUnderTest extends SpiceManager {
        private Exception ex;

        private SpiceManagerUnderTest(Class<? extends SpiceService> spiceServiceClass) {
            super(spiceServiceClass);
        }

        @Override
        public void run() {
            try {
                super.run();
            } catch (Exception ex) {
                this.ex = ex;
            }
        }

        public Exception getException(long timeout) throws InterruptedException {
            runner.join(timeout);
            return ex;
        }
    }

}
