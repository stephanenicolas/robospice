package com.octo.android.robospice;

import android.test.InstrumentationTestCase;

import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.sample.SampleJsonPersistenceRestContentService;
import com.octo.android.robospice.stub.ContentRequestFailingStub;
import com.octo.android.robospice.stub.ContentRequestStub;
import com.octo.android.robospice.stub.ContentRequestSucceedingStub;
import com.octo.android.robospice.stub.ContentRequestWaitingStub;
import com.octo.android.robospice.stub.RequestListenerStub;
import com.octo.android.robospice.stub.RequestListenerWithProgressStub;

public class SpiceManagerTest extends InstrumentationTestCase {

    private final static Class< String > TEST_CLASS = String.class;
    private final static String TEST_CACHE_KEY = "12345";
    private final static String TEST_CACHE_KEY2 = "123456";
    private final static long TEST_DURATION = DurationInMillis.ONE_SECOND;
    private final static String TEST_RETURNED_DATA = "coucou";
    private static final long WAIT_BEFORE_EXECUTING_REQUEST = 1500;
    private static final long REQUEST_COMPLETION_TIME_OUT = 1000;
    private static final long CONTENT_MANAGER_WAIT_TIMEOUT = 500;

    private SpiceManagerUnderTest spiceManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        spiceManager = new SpiceManagerUnderTest( SampleJsonPersistenceRestContentService.class );
    }

    @Override
    protected void tearDown() throws Exception {
        if ( spiceManager != null && spiceManager.isStarted() ) {
            spiceManager.shouldStopAndJoin( CONTENT_MANAGER_WAIT_TIMEOUT );
            spiceManager = null;
        }
        super.tearDown();
    }

    public void test_executeContentRequest_shouldFailIfNotStarted() {
        // given

        // when
        try {
            spiceManager.execute( new CachedSpiceRequest< String >( (SpiceRequest< String >) null, null, DurationInMillis.ALWAYS ), null );
            // then
            fail();
        } catch ( Exception ex ) {
            // then
            assertTrue( true );
        }
    }

    public void test_executeContentRequest_shouldFailIfStartedFromContextWithNoService() throws InterruptedException {
        // given

        // when
        spiceManager.start( getInstrumentation().getContext() );
        assertNotNull( spiceManager.getException() );
    }

    public void test_executeContentRequest_shouldFailIfStopped() throws InterruptedException {
        // given
        spiceManager.start( getInstrumentation().getTargetContext() );
        spiceManager.shouldStopAndJoin( CONTENT_MANAGER_WAIT_TIMEOUT );

        // when
        try {
            spiceManager.execute( new CachedSpiceRequest< String >( (SpiceRequest< String >) null, null, DurationInMillis.ALWAYS ), null );
            // then
            fail();
        } catch ( Exception ex ) {
            // then
            assertTrue( true );
        }
    }

    public void test_executeContentRequest_when_request_succeeds() throws InterruptedException {
        // when
        spiceManager.start( getInstrumentation().getTargetContext() );
        ContentRequestStub< String > contentRequestStub = new ContentRequestSucceedingStub< String >( TEST_CLASS, TEST_RETURNED_DATA );
        RequestListenerStub< String > requestListenerStub = new RequestListenerStub< String >();

        // when
        spiceManager.execute( contentRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub );
        requestListenerStub.await( REQUEST_COMPLETION_TIME_OUT );

        // test
        assertTrue( contentRequestStub.isLoadDataFromNetworkCalled() );
        assertTrue( requestListenerStub.isSuccessful() );
        assertTrue( requestListenerStub.isExecutedInUIThread() );
    }

    public void test_executeContentRequest_when_request_fails() throws InterruptedException {
        // when
        spiceManager.start( getInstrumentation().getTargetContext() );
        ContentRequestStub< String > contentRequestStub = new ContentRequestFailingStub< String >( TEST_CLASS );
        RequestListenerStub< String > requestListenerStub = new RequestListenerStub< String >();

        // when
        spiceManager.execute( contentRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub );
        requestListenerStub.await( REQUEST_COMPLETION_TIME_OUT );

        // test
        assertTrue( contentRequestStub.isLoadDataFromNetworkCalled() );
        assertTrue( requestListenerStub.isExecutedInUIThread() );
        assertFalse( requestListenerStub.isSuccessful() );
    }

    public void testCancel() throws InterruptedException {
        // given
        ContentRequestStub< String > contentRequestStub = new ContentRequestSucceedingStub< String >( String.class, TEST_RETURNED_DATA );
        spiceManager.start( getInstrumentation().getTargetContext() );
        // when
        spiceManager.cancel( contentRequestStub );
        Thread.sleep( REQUEST_COMPLETION_TIME_OUT );

        // test
        assertTrue( contentRequestStub.isCancelled() );
    }

    public void testCancelAllRequests() throws InterruptedException {
        // given
        spiceManager.start( getInstrumentation().getTargetContext() );
        ContentRequestStub< String > contentRequestStub = new ContentRequestFailingStub< String >( TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST );
        ContentRequestStub< String > contentRequestStub2 = new ContentRequestFailingStub< String >( TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST );
        RequestListenerWithProgressStub< String > requestListenerStub = new RequestListenerWithProgressStub< String >();
        RequestListenerWithProgressStub< String > requestListenerStub2 = new RequestListenerWithProgressStub< String >();

        // when
        spiceManager.execute( contentRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub );
        spiceManager.execute( contentRequestStub2, TEST_CACHE_KEY2, TEST_DURATION, requestListenerStub2 );
        spiceManager.cancelAllRequests();

        contentRequestStub.await( REQUEST_COMPLETION_TIME_OUT );
        contentRequestStub2.await( REQUEST_COMPLETION_TIME_OUT );

        // test
        assertTrue( contentRequestStub.isCancelled() );
        assertTrue( contentRequestStub2.isCancelled() );
        assertTrue( requestListenerStub.isComplete() );
        assertTrue( requestListenerStub2.isComplete() );
        assertFalse( requestListenerStub.isSuccessful() );
        assertFalse( requestListenerStub2.isSuccessful() );
        assertTrue( requestListenerStub.getReceivedException() instanceof RequestCancelledException );
        assertTrue( requestListenerStub2.getReceivedException() instanceof RequestCancelledException );
    }

    public void addListenerIfPending_receives_no_events() throws InterruptedException {
        // given
        spiceManager.start( getInstrumentation().getTargetContext() );
        ContentRequestStub< String > contentRequestStub = new ContentRequestFailingStub< String >( TEST_CLASS );
        RequestListenerWithProgressStub< String > requestListenerStub = new RequestListenerWithProgressStub< String >();

        // when
        spiceManager.addListenerIfPending( TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub );

        contentRequestStub.await( WAIT_BEFORE_EXECUTING_REQUEST + REQUEST_COMPLETION_TIME_OUT );

        // test
        assertNull( requestListenerStub.isSuccessful() );
        assertFalse( requestListenerStub.isComplete() );
        assertNull( requestListenerStub.getReceivedException() );
    }

    public void test_ShouldStopStopsRequests() throws InterruptedException {
        // given
        spiceManager.start( getInstrumentation().getTargetContext() );
        ContentRequestStub< String > contentRequestStub = new ContentRequestFailingStub< String >( TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST );
        ContentRequestStub< String > contentRequestStub2 = new ContentRequestFailingStub< String >( TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST );
        RequestListenerStub< String > requestListenerStub = new RequestListenerStub< String >();
        RequestListenerStub< String > requestListenerStub2 = new RequestListenerStub< String >();

        // when
        spiceManager.execute( contentRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub );
        spiceManager.execute( contentRequestStub2, TEST_CACHE_KEY2, TEST_DURATION, requestListenerStub2 );
        spiceManager.shouldStop();

        contentRequestStub.await( WAIT_BEFORE_EXECUTING_REQUEST + REQUEST_COMPLETION_TIME_OUT );
        contentRequestStub2.await( WAIT_BEFORE_EXECUTING_REQUEST + REQUEST_COMPLETION_TIME_OUT );

        // test
        // no guarantee on that
        // assertTrue( contentRequestStub.isLoadDataFromNetworkCalled() );
        // assertTrue( contentRequestStub2.isLoadDataFromNetworkCalled() );
        assertNull( requestListenerStub.isSuccessful() );
        assertNull( requestListenerStub2.isSuccessful() );
    }

    public void test_ShouldStopStopsRequests_dont_notify_listeners_after_requests_are_executed() throws InterruptedException {
        // given
        spiceManager.start( getInstrumentation().getTargetContext() );
        ContentRequestWaitingStub< String > contentRequestStub = new ContentRequestWaitingStub< String >( TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST );
        ContentRequestWaitingStub< String > contentRequestStub2 = new ContentRequestWaitingStub< String >( TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST );
        RequestListenerStub< String > requestListenerStub = new RequestListenerStub< String >();
        RequestListenerStub< String > requestListenerStub2 = new RequestListenerStub< String >();

        // when
        spiceManager.execute( contentRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub );
        spiceManager.execute( contentRequestStub2, TEST_CACHE_KEY2, TEST_DURATION, requestListenerStub2 );

        // wait for requests begin to be executed
        contentRequestStub.await( REQUEST_COMPLETION_TIME_OUT );
        contentRequestStub2.await( REQUEST_COMPLETION_TIME_OUT );
        // stop before
        spiceManager.shouldStop();

        requestListenerStub.await( REQUEST_COMPLETION_TIME_OUT );
        requestListenerStub2.await( REQUEST_COMPLETION_TIME_OUT );

        // test
        assertTrue( contentRequestStub.isLoadDataFromNetworkCalled() );
        assertTrue( contentRequestStub2.isLoadDataFromNetworkCalled() );
        assertNull( requestListenerStub.isSuccessful() );
        assertNull( requestListenerStub2.isSuccessful() );
    }

    public void test_dontNotifyRequestListenersForRequest() throws InterruptedException {
        // given
        spiceManager.start( getInstrumentation().getTargetContext() );
        ContentRequestStub< String > contentRequestStub = new ContentRequestFailingStub< String >( TEST_CLASS, WAIT_BEFORE_EXECUTING_REQUEST );
        ContentRequestStub< String > contentRequestStub2 = new ContentRequestFailingStub< String >( TEST_CLASS );
        RequestListenerStub< String > requestListenerStub = new RequestListenerStub< String >();
        RequestListenerStub< String > requestListenerStub2 = new RequestListenerStub< String >();

        // when
        spiceManager.execute( contentRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub );
        spiceManager.dontNotifyRequestListenersForRequestInternal( contentRequestStub );
        spiceManager.execute( contentRequestStub2, TEST_CACHE_KEY2, TEST_DURATION, requestListenerStub2 );

        contentRequestStub.await( WAIT_BEFORE_EXECUTING_REQUEST + REQUEST_COMPLETION_TIME_OUT );
        contentRequestStub2.await( REQUEST_COMPLETION_TIME_OUT );

        // test
        // no guarantee on that
        // assertTrue( contentRequestStub.isLoadDataFromNetworkCalled() );
        // assertTrue( contentRequestStub2.isLoadDataFromNetworkCalled() );
        assertNull( requestListenerStub.isSuccessful() );
        assertFalse( requestListenerStub2.isSuccessful() );
    }

    public void test_dontNotifyAnyRequestListeners() throws InterruptedException {
        // given
        spiceManager.start( getInstrumentation().getTargetContext() );
        ContentRequestStub< String > contentRequestStub = new ContentRequestFailingStub< String >( TEST_CLASS, 1000 );
        ContentRequestStub< String > contentRequestStub2 = new ContentRequestFailingStub< String >( TEST_CLASS );
        RequestListenerStub< String > requestListenerStub = new RequestListenerStub< String >();
        RequestListenerStub< String > requestListenerStub2 = new RequestListenerStub< String >();

        // when
        spiceManager.execute( contentRequestStub, TEST_CACHE_KEY, TEST_DURATION, requestListenerStub );
        spiceManager.execute( contentRequestStub2, TEST_CACHE_KEY2, TEST_DURATION, requestListenerStub2 );
        spiceManager.dontNotifyAnyRequestListenersInternal();

        spiceManager.dumpState();
        contentRequestStub.await( WAIT_BEFORE_EXECUTING_REQUEST + REQUEST_COMPLETION_TIME_OUT );
        contentRequestStub2.await( REQUEST_COMPLETION_TIME_OUT );

        // test
        // no guarantee on that
        // assertTrue( contentRequestStub.isLoadDataFromNetworkCalled() );
        // assertTrue( contentRequestStub2.isLoadDataFromNetworkCalled() );

        assertNull( requestListenerStub.isSuccessful() );
        assertNull( requestListenerStub2.isSuccessful() );
    }

    /**
     * Class under test. Just a wrapper to get any exception that can occur in the spicemanager's thread. Inspired by
     * http://stackoverflow.com/questions/2596493/junit-assert-in-thread-throws-exception/13712829#13712829
     */
    private final class SpiceManagerUnderTest extends SpiceManager {
        private Exception ex;

        private SpiceManagerUnderTest( Class< ? extends SpiceService > contentServiceClass ) {
            super( contentServiceClass );
        }

        @Override
        public void run() {
            try {
                super.run();
            } catch ( Exception ex ) {
                this.ex = ex;
            }
        }

        public Exception getException() throws InterruptedException {
            runner.join();
            return ex;
        }
    }

}
