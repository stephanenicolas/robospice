package com.octo.android.robospice.request;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.easymock.EasyMock;

import roboguice.util.temp.Ln;
import android.content.Context;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.priority.PausableThreadPoolExecutor;
import com.octo.android.robospice.priority.PriorityThreadPoolExecutor;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.SpiceServiceListener;
import com.octo.android.robospice.request.listener.SpiceServiceListener.RequestProcessingContext;
import com.octo.android.robospice.request.notifier.DefaultRequestListenerNotifier;
import com.octo.android.robospice.request.notifier.SpiceServiceListenerNotifier;
import com.octo.android.robospice.retry.DefaultRetryPolicy;
import com.octo.android.robospice.stub.CachedSpiceRequestStub;
import com.octo.android.robospice.stub.PendingRequestListenerWithProgressStub;
import com.octo.android.robospice.stub.RequestListenerStub;
import com.octo.android.robospice.stub.RequestListenerWithProgressStub;
import com.octo.android.robospice.stub.SpiceRequestFailingStub;
import com.octo.android.robospice.stub.SpiceRequestStub;
import com.octo.android.robospice.stub.SpiceRequestSucceedingStub;

@SmallTest
public class RequestProcessorTest extends AndroidTestCase {

    private static final Class<String> TEST_CLASS = String.class;
    private static final String TEST_CACHE_KEY = "12345";
    private static final String TEST_CACHE_KEY2 = "12345_2";
    private static final long TEST_DURATION = DurationInMillis.ONE_SECOND;
    private static final String TEST_RETURNED_DATA = "coucou";
    private static final String TEST_RETURNED_DATA2 = "toto";
    private static final long REQUEST_COMPLETION_TIME_OUT = 2000;
    private static final long REQUEST_COMPLETION_TIME_OUT_LARGE = 10000;
    private static final long WAIT_BEFORE_REQUEST_EXECUTION = 200;
    private static final float TEST_RETRY_BACKOFF_MULTIPLIER = 1.0f;
    private static final long TEST_DELAY_BEFORE_RETRY = WAIT_BEFORE_REQUEST_EXECUTION;
    private static final int TEST_RETRY_COUNT = 3;

    private CacheManager mockCacheManager;
    private RequestProcessor requestProcessorUnderTest;
    private RequestProcessorListener requestProcessorListener;
    private MockNetworkStateChecker networkStateChecker;
    private DefaultRequestListenerNotifier progressReporter;
    private SpiceServiceListenerNotifier spiceServiceListenerNotifier;
    private RequestProgressManager mockRequestProgressManager;
    private DefaultRequestRunner mockRequestRunner;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // https://code.google.com/p/dexmaker/issues/detail?id=2
        System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
        mockCacheManager = EasyMock.createMock(CacheManager.class);
        requestProcessorListener = new RequestProcessorListener() {

            @Override
            public void allRequestComplete() {
            }

            @Override
            public void requestsInProgress() {
            }
        };
        ExecutorService executorService = PriorityThreadPoolExecutor.getPriorityExecutor(1);
        networkStateChecker = new MockNetworkStateChecker();
        progressReporter = new DefaultRequestListenerNotifier();
        spiceServiceListenerNotifier = new SpiceServiceListenerNotifier();
        mockRequestProgressManager = new RequestProgressManager(requestProcessorListener, progressReporter, spiceServiceListenerNotifier);
        mockRequestRunner = new DefaultRequestRunner(getContext(), mockCacheManager, executorService, mockRequestProgressManager, networkStateChecker);

        requestProcessorUnderTest = new RequestProcessor(mockCacheManager, mockRequestProgressManager, mockRequestRunner);
    }

    @Override
    protected void tearDown() throws Exception {
        requestProcessorUnderTest.shouldStop();
        super.tearDown();
    }

    @SuppressWarnings("rawtypes")
    public void testAddRequestsFromManyThreads() throws Exception {
        final ArrayList<RequestListenerStub> listeners = new ArrayList<RequestListenerStub>();
        final ArrayList<Thread> threads = new ArrayList<Thread>();
        final int threadCount = 50;
        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(TEST_RETURNED_DATA);
        EasyMock.expectLastCall().atLeastOnce();
        EasyMock.replay(mockCacheManager);
        
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
                    RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
                    synchronized (listeners) {
                        listeners.add(mockRequestListener);
                    }
                    Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
                    requestListenerSet.add(mockRequestListener);
                    requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);
                }
            });
            thread.start();
            threads.add(thread);
        }
        //wait for all threads to have added their requests and listeners
        for (Thread thread : threads) {
            thread.join(REQUEST_COMPLETION_TIME_OUT_LARGE);
        }
        
        int listenersCalledCount = 0;
        for (RequestListenerStub listener : listeners) {
            //wait for all listeners to have been invoked
            listener.await(REQUEST_COMPLETION_TIME_OUT_LARGE);
            if (listener.isSuccessful() != null) {
                listenersCalledCount++;
            }
        }
        EasyMock.verify(mockCacheManager);
        assertEquals(threadCount, listeners.size());
        assertEquals(threadCount, listenersCalledCount);
    }

    // ============================================================================================
    // TESTING WITH FAIL ON ERROR = false
    // ============================================================================================

    public void testAddRequest_when_cache_is_not_used() throws CacheLoadingException, CacheSavingException, InterruptedException {
        // given
        String cacheKey = null;
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, cacheKey, TEST_DURATION, TEST_RETURNED_DATA);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);
        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.isComplete());
    }

    public void testObservers_with_AddRequest_when_cache_is_not_used() throws InterruptedException {
        // given
        String cacheKey = null;
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, cacheKey, TEST_DURATION, TEST_RETURNED_DATA);

        ExecutorService executorService = PriorityThreadPoolExecutor.getPriorityExecutor(1);
        mockRequestRunner = new DefaultRequestRunner(getContext(), mockCacheManager, executorService, mockRequestProgressManager, networkStateChecker);
        requestProcessorUnderTest = new RequestProcessor(mockCacheManager, mockRequestProgressManager, mockRequestRunner);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);
        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.isComplete());
    }

    public void testAddRequest_when_something_is_found_in_cache() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(TEST_RETURNED_DATA);
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertFalse(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.isComplete());
    }

    public void testAddRequest_when_nothing_is_found_in_cache_and_request_succeeds() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.isComplete());
    }

    public void testAddRequest_when_nothing_is_found_in_cache_and_request_fails() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createFailedRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION);
        stubRequest.setRetryPolicy(null);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertFalse(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.isComplete());
    }

    public void testObservers_with_AddRequest_when_nothing_is_found_in_cache_and_request_fails() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createFailedRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION);
        stubRequest.setRetryPolicy(null);

        // prepare observers

        ExecutorService executorService = PriorityThreadPoolExecutor.getPriorityExecutor(1);
        mockRequestRunner = new DefaultRequestRunner(getContext(), mockCacheManager, executorService, mockRequestProgressManager, networkStateChecker);
        requestProcessorUnderTest = new RequestProcessor(mockCacheManager, mockRequestProgressManager, mockRequestRunner);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertFalse(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.isComplete());

    }

    public void testAddRequest_when_saving_to_cache_throws_exception() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andThrow(new CacheSavingException(""));
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.isComplete());
    }

    // TDD for issue #70
    public void testAddRequest_when_just_executed_addListenerIfPending() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA, TEST_DURATION);

        RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        // create a addlistener if pending request
        final CachedSpiceRequest<String> addListenerIfPendingCachedRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, DurationInMillis.ALWAYS_EXPIRED, null);
        addListenerIfPendingCachedRequest.setProcessable(false);

        RequestListenerStub<String> mockRequestListener2 = new RequestListenerStub<String>();
        Set<RequestListener<?>> requestListenerSet2 = new HashSet<RequestListener<?>>();
        requestListenerSet2.add(mockRequestListener2);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(addListenerIfPendingCachedRequest, requestListenerSet2);
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);
        mockRequestListener2.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(mockRequestListener.isSuccessful());
        assertNull(mockRequestListener2.isSuccessful());
    }

    public void testAddRequest_when_request_is_cancelled_and_new_one_relaunched_with_same_key() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA, WAIT_BEFORE_REQUEST_EXECUTION);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);
        stubRequest.cancel();
        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);
        mockRequestListener.awaitComplete(REQUEST_COMPLETION_TIME_OUT);

        stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
        mockRequestListener = new RequestListenerWithProgressStub<String>();
        requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);
        mockRequestListener.awaitComplete(REQUEST_COMPLETION_TIME_OUT);

        // then
        // EasyMock.verify( mockCacheManager );
        assertFalse(stubRequest.isCancelled());
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(mockRequestListener.isComplete());
        System.out.println(mockRequestListener.getReceivedException());
        assertTrue(mockRequestListener.isSuccessful());
    }

    public void testAddRequest_with_null_listener() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(null);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);
        stubRequest.await(REQUEST_COMPLETION_TIME_OUT);
        // then
        // EasyMock.verify( mockCacheManager );
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
    }

    // ============================================================================================
    // TESTING WITH FAIL ON ERROR = true
    // ============================================================================================

    public void testAddRequest_fail_on_error_true_when_nothing_is_found_in_cache() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.setFailOnCacheError(true);
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.isComplete());
    }

    public void testAddRequest_when_fail_on_error_true_loading_from_cache_throws_exception() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
        stubRequest.setRetryPolicy(null);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andThrow(new CacheLoadingException(""));
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.setFailOnCacheError(true);
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertFalse(stubRequest.isLoadDataFromNetworkCalled());
        assertFalse(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.isComplete());
    }

    public void testAddRequest_when_fail_on_error_true_saving_to_cache_throws_exception() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
        stubRequest.setRetryPolicy(null);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andThrow(new CacheSavingException(""));
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.setFailOnCacheError(true);
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertFalse(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.isComplete());
    }

    public void testAddRequest_doesnt_aggregate_requests_with_null_cache_key() throws InterruptedException {
        // given
        CachedSpiceRequestStub<String> stubRequest1 = createSuccessfulRequest(TEST_CLASS, null, TEST_DURATION, TEST_RETURNED_DATA);

        CachedSpiceRequestStub<String> stubRequest2 = createSuccessfulRequest(TEST_CLASS, null, TEST_DURATION, TEST_RETURNED_DATA);

        RequestListenerWithProgressStub<String> mockRequestListener1 = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet1 = new HashSet<RequestListener<?>>();
        requestListenerSet1.add(mockRequestListener1);
        RequestListenerWithProgressStub<String> mockRequestListener2 = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet2 = new HashSet<RequestListener<?>>();
        requestListenerSet2.add(mockRequestListener2);

        // mock should not be invoked for loading nor saving cache
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest1, requestListenerSet1);
        requestProcessorUnderTest.addRequest(stubRequest2, requestListenerSet2);

        mockRequestListener1.await(REQUEST_COMPLETION_TIME_OUT);
        mockRequestListener2.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequest1.isLoadDataFromNetworkCalled());
        assertTrue(stubRequest2.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener1.isExecutedInUIThread());
        assertTrue(mockRequestListener2.isExecutedInUIThread());
        assertTrue(mockRequestListener1.isSuccessful());
        assertTrue(mockRequestListener2.isSuccessful());
        assertTrue(mockRequestListener1.isComplete());
        assertTrue(mockRequestListener2.isComplete());

    }

    // TDD for https://github.com/octo-online/robospice/issues/215
    public void testAddRequest_doesnt_aggregate_requests_when_first_one_is_not_processable() throws InterruptedException {
        // given
        CachedSpiceRequestStub<String> stubRequest1 = createSuccessfulRequest(TEST_CLASS, null, TEST_DURATION, TEST_RETURNED_DATA);
        stubRequest1.setProcessable(false);
        CachedSpiceRequestStub<String> stubRequest2 = createSuccessfulRequest(TEST_CLASS, null, TEST_DURATION, TEST_RETURNED_DATA);

        PendingRequestListenerWithProgressStub<String> mockRequestListener1 = new PendingRequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet1 = new HashSet<RequestListener<?>>();
        requestListenerSet1.add(mockRequestListener1);
        RequestListenerWithProgressStub<String> mockRequestListener2 = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet2 = new HashSet<RequestListener<?>>();
        requestListenerSet2.add(mockRequestListener2);

        // mock should not be invoked for loading nor saving cache
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest1, requestListenerSet1);
        requestProcessorUnderTest.addRequest(stubRequest2, requestListenerSet2);

        mockRequestListener2.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertFalse(stubRequest1.isLoadDataFromNetworkCalled());
        assertTrue(stubRequest2.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener2.isExecutedInUIThread());
        assertTrue(mockRequestListener1.isRequestNotFound());
        assertTrue(mockRequestListener2.isSuccessful());
        assertTrue(mockRequestListener2.isComplete());
        assertNotNull(mockRequestListener2.getResultHistory().get(0));
    }

    // ============================================================================================
    // DO NOT NOTIFY LISTENERS
    // ============================================================================================
    public void test_dontNotifyRequestListenersForRequest_with_2_request_and_one_not_notified() throws InterruptedException, CacheLoadingException, CacheSavingException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA, WAIT_BEFORE_REQUEST_EXECUTION);
        CachedSpiceRequestStub<String> stubRequest2 = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY2, TEST_DURATION, TEST_RETURNED_DATA);
        RequestListenerWithProgressStub<String> requestListenerStub = new RequestListenerWithProgressStub<String>();
        RequestListenerWithProgressStub<String> requestListenerStub2 = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(requestListenerStub);
        Set<RequestListener<?>> requestListenerSet2 = new HashSet<RequestListener<?>>();
        requestListenerSet2.add(requestListenerStub2);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY2), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY2))).andReturn(TEST_RETURNED_DATA);
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);
        requestProcessorUnderTest.dontNotifyRequestListenersForRequest(stubRequest, requestListenerSet);
        requestProcessorUnderTest.addRequest(stubRequest2, requestListenerSet2);

        stubRequest.await(REQUEST_COMPLETION_TIME_OUT);
        requestListenerStub2.await(REQUEST_COMPLETION_TIME_OUT);

        // test
        EasyMock.verify(mockCacheManager);
        // TODO check this !! assertTrue(
        // stubRequest.isLoadDataFromNetworkCalled() );
        assertTrue(stubRequest2.isLoadDataFromNetworkCalled());
        assertNull(requestListenerStub.isSuccessful());
        assertFalse(requestListenerStub.isComplete());
        assertTrue(requestListenerStub2.isSuccessful());
        assertTrue(requestListenerStub2.isComplete());
    }

    // ============================================================================================
    // DO NOT NOTIFY LISTENERS
    // ============================================================================================
    public void test_addRequest_with_2_requests_and_one_is_cancelled() throws InterruptedException, CacheLoadingException, CacheSavingException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
        CachedSpiceRequestStub<String> stubRequest2 = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY2, TEST_DURATION, TEST_RETURNED_DATA);
        RequestListenerWithProgressStub<String> requestListenerStub = new RequestListenerWithProgressStub<String>();
        RequestListenerWithProgressStub<String> requestListenerStub2 = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(requestListenerStub);
        Set<RequestListener<?>> requestListenerSet2 = new HashSet<RequestListener<?>>();
        requestListenerSet2.add(requestListenerStub2);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY2), EasyMock.eq(TEST_DURATION))).andReturn(TEST_RETURNED_DATA);
        EasyMock.replay(mockCacheManager);

        stubRequest.cancel();

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);
        requestProcessorUnderTest.addRequest(stubRequest2, requestListenerSet2);
        requestListenerStub.await(REQUEST_COMPLETION_TIME_OUT);
        requestListenerStub2.await(REQUEST_COMPLETION_TIME_OUT);

        // test
        EasyMock.verify(mockCacheManager);
        assertFalse(stubRequest.isLoadDataFromNetworkCalled());

        assertNotNull(requestListenerStub.isSuccessful());
        assertFalse(requestListenerStub.isSuccessful());
        assertTrue(requestListenerStub.getReceivedException() instanceof RequestCancelledException);
        assertTrue(requestListenerStub.isComplete());

        assertNotNull(requestListenerStub2.isSuccessful());
        assertTrue(requestListenerStub2.isSuccessful());
        assertTrue(requestListenerStub2.isComplete());
    }

    // ============================================
    // DIRTY CACHE TESTING
    // ============================================

    public void testAddRequest_when_something_is_found_in_cache_after_expiry_and_requests_accepts_dirty_cache() throws CacheLoadingException, CacheSavingException, InterruptedException,
        CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA, WAIT_BEFORE_REQUEST_EXECUTION);
        stubRequest.setAcceptingDirtyCache(true);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.anyLong())).andReturn(TEST_RETURNED_DATA);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(mockRequestListener.isSuccessful());

        // when
        mockRequestListener.resetSuccess();
        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isSuccessful());
    }

    public void testAddRequest_when_nothing_is_found_in_cache_after_expiry_and_requests_accepts_dirty_cache() throws CacheLoadingException, CacheSavingException, InterruptedException,
        CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA, WAIT_BEFORE_REQUEST_EXECUTION);
        stubRequest.setAcceptingDirtyCache(true);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.anyLong())).andReturn(TEST_RETURNED_DATA);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(mockRequestListener.isSuccessful());

        // when
        mockRequestListener.resetSuccess();
        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isSuccessful());
    }

    // ============================================================================================
    // TESTING CACHE MANAGER DEPENDENCY
    // ============================================================================================

    public void testRemoveAllDataFromCache() {
        // given
        mockCacheManager.removeAllDataFromCache();
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.removeAllDataFromCache();

        // then
        EasyMock.verify(mockCacheManager);
    }

    public void testRemoveAllDataFromCache_for_given_class() {
        mockCacheManager.removeAllDataFromCache(TEST_CLASS);
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.removeAllDataFromCache(TEST_CLASS);

        // then
        EasyMock.verify(mockCacheManager);
    }

    public void testRemoveAllDataFromCache_for_given_class_and_cachekey() {
        EasyMock.expect(mockCacheManager.removeDataFromCache(TEST_CLASS, TEST_CACHE_KEY)).andReturn(true);
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.removeDataFromCache(TEST_CLASS, TEST_CACHE_KEY);

        // then
        EasyMock.verify(mockCacheManager);
    }

    // ============================================================================================
    // TESTING NETWORK MANAGER DEPENDENCY & SPICE LISTENER
    // ============================================================================================
    public void testAddRequestWhenNetworkIsDown() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
        stubRequest.setRetryPolicy(null);

        RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.replay(mockCacheManager);

        SpiceServiceListener mockSpiceServiceListener = EasyMock.createMock(SpiceServiceListener.class);
        mockSpiceServiceListener.onRequestAdded((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        mockSpiceServiceListener.onRequestProgressUpdated((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        mockSpiceServiceListener.onRequestFailed((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        mockSpiceServiceListener.onRequestProcessed((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockSpiceServiceListener);
        requestProcessorUnderTest.addSpiceServiceListener(mockSpiceServiceListener);

        // when
        requestProcessorUnderTest.setFailOnCacheError(true);
        networkStateChecker.setNetworkAvailable(false);
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);
        // then
        EasyMock.verify(mockCacheManager);
        assertFalse(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertFalse(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.getReceivedException() instanceof NoNetworkException);

        EasyMock.verify(mockSpiceServiceListener);

    }

    public void testAddRequest_should_process_offline_request_even_if_network_is_down() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
        stubRequest.setOffline(true);
        RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
        EasyMock.replay(mockCacheManager);

        SpiceServiceListener mockSpiceServiceListener = EasyMock.createMock(SpiceServiceListener.class);
        mockSpiceServiceListener.onRequestAdded((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        mockSpiceServiceListener.onRequestProgressUpdated((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        mockSpiceServiceListener.onRequestSucceeded((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        mockSpiceServiceListener.onRequestProcessed((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockSpiceServiceListener);
        requestProcessorUnderTest.addSpiceServiceListener(mockSpiceServiceListener);

        // when
        requestProcessorUnderTest.setFailOnCacheError(true);
        networkStateChecker.setNetworkAvailable(false);
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(mockRequestListener.isSuccessful());
        EasyMock.verify(mockSpiceServiceListener);
    }

    // ============================================================================================
    // TDD
    // ============================================================================================

    public void test_2_spiceservicelisteners_should_be_notified_of_all_events_from_request_processor() throws Exception {
        // TDD for issue 182
        // given
        PausableThreadPoolExecutor executorService = PriorityThreadPoolExecutor.getPriorityExecutor(1);
        networkStateChecker = new MockNetworkStateChecker();

        mockRequestRunner = new DefaultRequestRunner(getContext(), mockCacheManager, executorService, mockRequestProgressManager, networkStateChecker);
        requestProcessorUnderTest = new RequestProcessor(mockCacheManager, mockRequestProgressManager, mockRequestRunner);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY2), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA2), EasyMock.eq(TEST_CACHE_KEY2))).andReturn(TEST_RETURNED_DATA2);
        EasyMock.replay(mockCacheManager);

        CachedSpiceRequestStub<String> spiceRequestStub = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
        spiceRequestStub.setPriority(SpiceRequest.PRIORITY_LOW);
        spiceRequestStub.setRetryPolicy(null);

        CachedSpiceRequestStub<String> spiceRequestStub2 = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY2, TEST_DURATION, TEST_RETURNED_DATA2);
        spiceRequestStub2.setPriority(SpiceRequest.PRIORITY_HIGH);
        spiceRequestStub2.setRetryPolicy(null);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        RequestListenerWithProgressStub<String> mockRequestListener2 = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet2 = new HashSet<RequestListener<?>>();
        requestListenerSet2.add(mockRequestListener2);

        SpiceServiceListener listener1 = EasyMock.createMock(SpiceServiceListener.class);
        listener1.onRequestAdded((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().times(2);
        listener1.onRequestProgressUpdated((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        listener1.onRequestProcessed((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        listener1.onRequestSucceeded((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(listener1);

        SpiceServiceListener listener2 = EasyMock.createMock(SpiceServiceListener.class);
        listener2.onRequestAdded((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().times(2);
        listener2.onRequestProgressUpdated((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        listener2.onRequestProcessed((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        listener2.onRequestSucceeded((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(listener2);

        requestProcessorUnderTest.addSpiceServiceListener(listener1);
        requestProcessorUnderTest.addSpiceServiceListener(listener2);

        // when
        requestProcessorUnderTest.addRequest(spiceRequestStub, requestListenerSet);
        requestProcessorUnderTest.addRequest(spiceRequestStub2, requestListenerSet2);

        mockRequestListener.awaitComplete(REQUEST_COMPLETION_TIME_OUT);
        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);
        mockRequestListener2.awaitComplete(REQUEST_COMPLETION_TIME_OUT);
        mockRequestListener2.await(REQUEST_COMPLETION_TIME_OUT);

        // test
        assertTrue(mockRequestListener.isComplete());
        assertTrue(mockRequestListener2.isComplete());
        assertTrue(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener2.isSuccessful());
        EasyMock.verify(listener1);
        EasyMock.verify(listener2);
    }

    public void test_2_spiceservicelisteners_should_be_notified_of_all_events_from_request_processor_when_request_are_aggregated() throws Exception {
        // TDD for issue 182
        // given
        PausableThreadPoolExecutor executorService = PriorityThreadPoolExecutor.getPriorityExecutor(1);
        networkStateChecker = new MockNetworkStateChecker();
        mockRequestRunner = new DefaultRequestRunner(getContext(), mockCacheManager, executorService, mockRequestProgressManager, networkStateChecker);
        requestProcessorUnderTest = new RequestProcessor(mockCacheManager, mockRequestProgressManager, mockRequestRunner);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockCacheManager);

        CachedSpiceRequestStub<String> spiceRequestStub = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA, WAIT_BEFORE_REQUEST_EXECUTION);
        spiceRequestStub.setPriority(SpiceRequest.PRIORITY_LOW);
        spiceRequestStub.setRetryPolicy(null);

        CachedSpiceRequestStub<String> spiceRequestStub2 = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA, WAIT_BEFORE_REQUEST_EXECUTION);
        spiceRequestStub2.setPriority(SpiceRequest.PRIORITY_HIGH);
        spiceRequestStub2.setRetryPolicy(null);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        RequestListenerWithProgressStub<String> mockRequestListener2 = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet2 = new HashSet<RequestListener<?>>();
        requestListenerSet2.add(mockRequestListener2);

        SpiceServiceListener listener1 = EasyMock.createMock(SpiceServiceListener.class);
        listener1.onRequestAdded((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        listener1.onRequestAggregated((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        listener1.onRequestProgressUpdated((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        listener1.onRequestProcessed((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        listener1.onRequestSucceeded((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.replay(listener1);

        SpiceServiceListener listener2 = EasyMock.createMock(SpiceServiceListener.class);
        listener2.onRequestAdded((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        listener2.onRequestAggregated((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        listener2.onRequestProgressUpdated((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        listener2.onRequestProcessed((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        listener2.onRequestSucceeded((CachedSpiceRequest<?>) EasyMock.anyObject(), (RequestProcessingContext) EasyMock.anyObject());
        EasyMock.replay(listener2);

        requestProcessorUnderTest.addSpiceServiceListener(listener1);
        requestProcessorUnderTest.addSpiceServiceListener(listener2);

        // when
        requestProcessorUnderTest.addRequest(spiceRequestStub, requestListenerSet);
        requestProcessorUnderTest.addRequest(spiceRequestStub2, requestListenerSet2);

        mockRequestListener.awaitComplete(REQUEST_COMPLETION_TIME_OUT);
        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);
        mockRequestListener2.awaitComplete(REQUEST_COMPLETION_TIME_OUT);
        mockRequestListener2.await(REQUEST_COMPLETION_TIME_OUT);

        // test
        assertTrue(mockRequestListener.isComplete());
        assertTrue(mockRequestListener2.isComplete());
        assertTrue(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener2.isSuccessful());
        EasyMock.verify(listener1);
        EasyMock.verify(listener2);
    }

    // ============================================================================================
    // TESTING REQUEST PRIORITY
    // ============================================================================================

    // TODO
    // both tests need some rewrite. There is no reason to receive 21 times the
    // success result. They can be received twice before 1 reset, thus
    // one will be missing.
    // TODO

    /*
     * Those tests are really tricky. We want to test request priority. There
     * are some limitations to using a PriorityBlockingQueue inside an
     * ExecutorService. Here, to get a smooth test, we inject a lot of low
     * requests and make assertions on the last executed request. That is the
     * only way to get a stable test.
     */
    public void testRequestPriority_should_execute_asap_hight_priority_requests() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // when
        PausableThreadPoolExecutor executorService = PriorityThreadPoolExecutor.getPriorityExecutor(1);
        networkStateChecker = new MockNetworkStateChecker();
        mockRequestRunner = new DefaultRequestRunner(getContext(), mockCacheManager, executorService, mockRequestProgressManager, networkStateChecker);
        requestProcessorUnderTest = new RequestProcessor(mockCacheManager, mockRequestProgressManager, mockRequestRunner);

        CachedSpiceRequestStub<String> stubRequestHighPriority = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY2, TEST_DURATION, TEST_RETURNED_DATA2);
        stubRequestHighPriority.setPriority(SpiceRequest.PRIORITY_HIGH);

        RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.anyObject(), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA2), EasyMock.eq(TEST_CACHE_KEY2))).andReturn(TEST_RETURNED_DATA2);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockCacheManager);

        executorService.pause();
        final int lowRequestCount = 10;
        for (int i = 0; i < lowRequestCount; i++) {
            CachedSpiceRequestStub<String> stubRequestLowPriority = createSuccessfulRequest(TEST_CLASS, TEST_RETURNED_DATA);
            stubRequestLowPriority.setPriority(SpiceRequest.PRIORITY_LOW);
            requestProcessorUnderTest.addRequest(stubRequestLowPriority, requestListenerSet);
        }
        requestProcessorUnderTest.addRequest(stubRequestHighPriority, requestListenerSet);
        executorService.resume();

        for (int i = 0; i < lowRequestCount; i++) {
            mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);
            mockRequestListener.resetSuccess();
        }
        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequestHighPriority.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertEquals(lowRequestCount + 1, mockRequestListener.getResultHistory().size());
        assertNotSame(TEST_RETURNED_DATA2, mockRequestListener.getResultHistory().get(lowRequestCount));
    }

    public void testRequestPriority_should_execute_lazyly_low_priority_requests() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // when
        PausableThreadPoolExecutor executorService = PriorityThreadPoolExecutor.getPriorityExecutor(1);
        networkStateChecker = new MockNetworkStateChecker();
        mockRequestRunner = new DefaultRequestRunner(getContext(), mockCacheManager, executorService, mockRequestProgressManager, networkStateChecker);
        requestProcessorUnderTest = new RequestProcessor(mockCacheManager, mockRequestProgressManager, mockRequestRunner);

        CachedSpiceRequestStub<String> stubRequestLowPriority = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY2, TEST_DURATION, TEST_RETURNED_DATA2);
        stubRequestLowPriority.setPriority(SpiceRequest.PRIORITY_LOW);

        RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.anyObject(), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA2), EasyMock.eq(TEST_CACHE_KEY2))).andReturn(TEST_RETURNED_DATA2);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockCacheManager);

        executorService.pause();
        final int lowRequestCount = 10;
        for (int i = 0; i < lowRequestCount; i++) {
            CachedSpiceRequestStub<String> stubRequestNormalPriority = createSuccessfulRequest(TEST_CLASS, TEST_RETURNED_DATA);
            stubRequestNormalPriority.setPriority(SpiceRequest.PRIORITY_NORMAL);
            requestProcessorUnderTest.addRequest(stubRequestNormalPriority, requestListenerSet);
        }
        requestProcessorUnderTest.addRequest(stubRequestLowPriority, requestListenerSet);

        for (int i = 0; i < lowRequestCount; i++) {
            CachedSpiceRequestStub<String> stubRequestNormalPriority = createSuccessfulRequest(TEST_CLASS, TEST_RETURNED_DATA);
            stubRequestNormalPriority.setPriority(SpiceRequest.PRIORITY_NORMAL);
            requestProcessorUnderTest.addRequest(stubRequestNormalPriority, requestListenerSet);
        }
        executorService.resume();

        for (int i = 0; i < 2 * lowRequestCount; i++) {
            mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);
            mockRequestListener.resetSuccess();
            Ln.d("reset complete");
        }
        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequestLowPriority.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertEquals(2 * lowRequestCount + 1, mockRequestListener.getResultHistory().size());
        assertEquals(TEST_RETURNED_DATA2, mockRequestListener.getResultHistory().get(2 * lowRequestCount));
    }

    // ============================================================================================
    // RETRY POLICY
    // ============================================================================================

    public void testAddRequest_when_nothing_is_found_in_cache_and_request_has_retry_policy() throws CacheLoadingException, CacheSavingException, InterruptedException, CacheCreationException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createFailedRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(TEST_RETRY_COUNT, TEST_DELAY_BEFORE_RETRY, TEST_RETRY_BACKOFF_MULTIPLIER);
        stubRequest.setRetryPolicy(retryPolicy);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(RequestProcessorTest.REQUEST_COMPLETION_TIME_OUT);

        // then
        assertNotNull(stubRequest.getRetryPolicy());
        assertEquals(0, stubRequest.getRetryPolicy().getRetryCount());
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertNotNull(mockRequestListener.isSuccessful());
        assertFalse(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertTrue(mockRequestListener.isComplete());
    }

    public void testAddRequest_when_fail_on_error_true_and_request_has_retry_policy_loading_from_cache_throws_exception() throws SpiceException, InterruptedException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(TEST_RETRY_COUNT, TEST_DELAY_BEFORE_RETRY, TEST_RETRY_BACKOFF_MULTIPLIER);
        stubRequest.setRetryPolicy(retryPolicy);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andThrow(new CacheLoadingException(""));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.setFailOnCacheError(true);
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        assertNotNull(stubRequest.getRetryPolicy());
        assertEquals(0, stubRequest.getRetryPolicy().getRetryCount());
        EasyMock.verify(mockCacheManager);
        assertFalse(stubRequest.isLoadDataFromNetworkCalled());
        assertFalse(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.isComplete());
    }

    public void testAddRequest_when_fail_on_error_and_request_has_retry_polic_true_saving_to_cache_throws_exception() throws InterruptedException, SpiceException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(TEST_RETRY_COUNT, TEST_DELAY_BEFORE_RETRY, TEST_RETRY_BACKOFF_MULTIPLIER);
        stubRequest.setRetryPolicy(retryPolicy);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andThrow(new CacheSavingException(""));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.setFailOnCacheError(true);
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        assertNotNull(stubRequest.getRetryPolicy());
        assertEquals(0, stubRequest.getRetryPolicy().getRetryCount());
        EasyMock.verify(mockCacheManager);
        assertTrue(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertFalse(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.isComplete());
    }

    public void testAddRequestWhenNetworkIsDown_and_request_has_retry_policy() throws CacheLoadingException, SpiceException, InterruptedException, Exception {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(TEST_RETRY_COUNT, TEST_DELAY_BEFORE_RETRY, TEST_RETRY_BACKOFF_MULTIPLIER);
        stubRequest.setRetryPolicy(retryPolicy);

        RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expectLastCall().times(1);
        EasyMock.replay(mockCacheManager);

        // when
        requestProcessorUnderTest.setFailOnCacheError(true);
        networkStateChecker.setNetworkAvailable(false);
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);

        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        assertNotNull(stubRequest.getRetryPolicy());
        assertEquals(TEST_RETRY_COUNT, stubRequest.getRetryPolicy().getRetryCount());
        EasyMock.verify(mockCacheManager);
        assertFalse(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertFalse(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.getReceivedException() instanceof NoNetworkException);
    }

    public void testDefaultRetryPolicy_implements_retry_countdown_and_exponential_backoff() throws Exception {
        // define local values since class constant values didn't reveal
        // incremental backoff
        final long localDelayBeforeRetry = 200;
        final float localRetryBackoffMultiplier = 5.0f;
        final int localRetryCount = 3;

        // given
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(localRetryCount, localDelayBeforeRetry, localRetryBackoffMultiplier);

        assertEquals(localRetryCount, retryPolicy.getRetryCount());
        assertEquals(localDelayBeforeRetry, retryPolicy.getDelayBeforeRetry());

        // when
        SpiceException e = null;
        retryPolicy.retry(e);

        // then
        assertEquals(localRetryCount - 1, retryPolicy.getRetryCount());
        assertEquals((long) (localDelayBeforeRetry * localRetryBackoffMultiplier), retryPolicy.getDelayBeforeRetry());
    }

    // ============================================================================================
    // NETWORK STATE CHECKER DEPENDENCY
    // ============================================================================================

    public void testExecute_when_there_is_no_network() throws Exception {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
        stubRequest.setRetryPolicy(null);

        RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);
        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(mockCacheManager);

        // when
        networkStateChecker.setNetworkAvailable(false);
        requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);
        mockRequestListener.await(REQUEST_COMPLETION_TIME_OUT);

        // then
        EasyMock.verify(mockCacheManager);
        assertFalse(stubRequest.isLoadDataFromNetworkCalled());
        assertTrue(mockRequestListener.isExecutedInUIThread());
        assertFalse(mockRequestListener.isSuccessful());
        assertTrue(mockRequestListener.getReceivedException() instanceof NoNetworkException);
    }

    // ============================================================================================
    // EXECUTOR SERVICE DEPENDENCY
    // ============================================================================================
    public void testShouldStop_shuts_down_the_executor_service() throws Exception {
        requestProcessorUnderTest.shouldStop();

        // given
        ExecutorService mockExecutorService = EasyMock.createMock(ExecutorService.class);
        mockRequestRunner = new DefaultRequestRunner(getContext(), mockCacheManager, mockExecutorService, mockRequestProgressManager, networkStateChecker);
        requestProcessorUnderTest = new RequestProcessor(mockCacheManager, mockRequestProgressManager, mockRequestRunner);

        mockExecutorService.shutdown();
        EasyMock.replay(mockExecutorService);

        // when
        requestProcessorUnderTest.shouldStop();

        // then
        EasyMock.verify(mockExecutorService);
        EasyMock.resetToNice(mockExecutorService);
    }

    // ============================================================================================
    // PRIVATE METHODS
    // ============================================================================================

    private <T> CachedSpiceRequestStub<T> createSuccessfulRequest(Class<T> clazz, T returnedData) {
        SpiceRequestStub<T> stubContentRequest = new SpiceRequestSucceedingStub<T>(clazz, returnedData);
        return new CachedSpiceRequestStub<T>(stubContentRequest, null, 0 /*
                                                                          * What
                                                                          * ever
                                                                          * .
                                                                          */);
    }

    private <T> CachedSpiceRequestStub<T> createSuccessfulRequest(Class<T> clazz, Object cacheKey, long maxTimeInCache, T returnedData) {
        SpiceRequestStub<T> stubContentRequest = new SpiceRequestSucceedingStub<T>(clazz, returnedData);
        return new CachedSpiceRequestStub<T>(stubContentRequest, cacheKey, maxTimeInCache);
    }

    private <T> CachedSpiceRequestStub<T> createSuccessfulRequest(Class<T> clazz, Object cacheKey, long maxTimeInCache, T returnedData, long waitBeforeExecution) {
        SpiceRequestStub<T> stubContentRequest = new SpiceRequestSucceedingStub<T>(clazz, returnedData, waitBeforeExecution);
        return new CachedSpiceRequestStub<T>(stubContentRequest, cacheKey, maxTimeInCache);
    }

    private <T> CachedSpiceRequestStub<T> createFailedRequest(Class<T> clazz, Object cacheKey, long maxTimeInCache) {
        SpiceRequestStub<T> stubContentRequest = new SpiceRequestFailingStub<T>(clazz);
        return new CachedSpiceRequestStub<T>(stubContentRequest, cacheKey, maxTimeInCache);
    }

    private class MockNetworkStateChecker implements NetworkStateChecker {

        private boolean networkAvailable = true;

        public void setNetworkAvailable(boolean networkAvailable) {
            this.networkAvailable = networkAvailable;
        }

        @Override
        public boolean isNetworkAvailable(Context context) {
            return networkAvailable;
        }

        @Override
        public void checkPermissions(Context context) {
            // do nothing
        }
    }
}
