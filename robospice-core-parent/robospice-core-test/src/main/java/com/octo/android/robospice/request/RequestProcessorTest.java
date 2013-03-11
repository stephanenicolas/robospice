package com.octo.android.robospice.request;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.easymock.EasyMock;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ICacheManager;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.stub.CachedSpiceRequestStub;
import com.octo.android.robospice.stub.RequestListenerStub;
import com.octo.android.robospice.stub.RequestListenerWithProgressStub;
import com.octo.android.robospice.stub.SpiceRequestFailingStub;
import com.octo.android.robospice.stub.SpiceRequestStub;
import com.octo.android.robospice.stub.SpiceRequestSucceedingStub;

@SmallTest
public class RequestProcessorTest extends InstrumentationTestCase {

    private static final Class<String> TEST_CLASS = String.class;
    private static final String TEST_CACHE_KEY = "12345";
    private static final String TEST_CACHE_KEY2 = "12345_2";
    private static final long TEST_DURATION = DurationInMillis.ONE_SECOND;
    private static final String TEST_RETURNED_DATA = "coucou";
    private static final long REQUEST_COMPLETION_TIME_OUT = 2000;
    private static final long WAIT_BEFORE_REQUEST_EXECUTION = 200;

    private ICacheManager mockCacheManager;
    private RequestProcessor requestProcessorUnderTest;
    private RequestProcessorListener requestProcessorListener;
    private MockNetworkStateChecker networkStateChecker;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockCacheManager = EasyMock.createMock(ICacheManager.class);
        requestProcessorListener = new RequestProcessorListener() {

            @Override
            public void allRequestComplete() {
            }
        };
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        networkStateChecker = new MockNetworkStateChecker();
        requestProcessorUnderTest = new RequestProcessor(getInstrumentation().getTargetContext(), mockCacheManager, executorService,
            requestProcessorListener, networkStateChecker);
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

    public void testAddRequest_when_something_is_found_in_cache() throws CacheLoadingException, CacheSavingException, InterruptedException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION)))
            .andReturn(TEST_RETURNED_DATA);
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

    public void testAddRequest_when_nothing_is_found_in_cache_and_request_succeeds() throws CacheLoadingException, CacheSavingException,
        InterruptedException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION)))
            .andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(
            TEST_RETURNED_DATA);
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

    public void testAddRequest_when_nothing_is_found_in_cache_and_request_fails() throws CacheLoadingException, CacheSavingException,
        InterruptedException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createFailedRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION)))
            .andReturn(null);
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

    public void testAddRequest_when_saving_to_cache_throws_exception() throws CacheLoadingException, CacheSavingException, InterruptedException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION)))
            .andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andThrow(
            new CacheSavingException(""));
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

    public void testAddRequest_when_request_is_cancelled_and_new_one_relaunched_with_same_key() throws CacheLoadingException, CacheSavingException,
        InterruptedException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA,
            WAIT_BEFORE_REQUEST_EXECUTION);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION)))
            .andReturn(null);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(
            TEST_RETURNED_DATA);
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

    public void testAddRequest_with_null_listener() throws CacheLoadingException, CacheSavingException, InterruptedException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(null);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION)))
            .andReturn(null);
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(
            TEST_RETURNED_DATA);
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

    public void testAddRequest_fail_on_error_true_when_nothing_is_found_in_cache() throws CacheLoadingException, CacheSavingException,
        InterruptedException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION)))
            .andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(
            TEST_RETURNED_DATA);
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

    public void testAddRequest_when_fail_on_error_true_loading_from_cache_throws_exception() throws CacheLoadingException, CacheSavingException,
        InterruptedException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION)))
            .andThrow(new CacheLoadingException(""));
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

    public void testAddRequest_when_fail_on_error_true_saving_to_cache_throws_exception() throws CacheLoadingException, CacheSavingException,
        InterruptedException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

        RequestListenerWithProgressStub<String> mockRequestListener = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION)))
            .andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andThrow(
            new CacheSavingException(""));
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

    // ============================================================================================
    // DO NOT NOTIFY LISTENERS
    // ============================================================================================
    public void test_dontNotifyRequestListenersForRequest_with_2_request_and_one_not_notified() throws InterruptedException, CacheLoadingException,
        CacheSavingException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA,
            WAIT_BEFORE_REQUEST_EXECUTION);
        CachedSpiceRequestStub<String> stubRequest2 = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY2, TEST_DURATION, TEST_RETURNED_DATA);
        RequestListenerWithProgressStub<String> requestListenerStub = new RequestListenerWithProgressStub<String>();
        RequestListenerWithProgressStub<String> requestListenerStub2 = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(requestListenerStub);
        Set<RequestListener<?>> requestListenerSet2 = new HashSet<RequestListener<?>>();
        requestListenerSet2.add(requestListenerStub2);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY2), EasyMock.eq(TEST_DURATION)))
            .andReturn(null);
        EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY2))).andReturn(
            TEST_RETURNED_DATA);
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
    public void test_addRequest_with_2_requests_and_one_is_cancelled() throws InterruptedException, CacheLoadingException, CacheSavingException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
        CachedSpiceRequestStub<String> stubRequest2 = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY2, TEST_DURATION, TEST_RETURNED_DATA);
        RequestListenerWithProgressStub<String> requestListenerStub = new RequestListenerWithProgressStub<String>();
        RequestListenerWithProgressStub<String> requestListenerStub2 = new RequestListenerWithProgressStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(requestListenerStub);
        Set<RequestListener<?>> requestListenerSet2 = new HashSet<RequestListener<?>>();
        requestListenerSet2.add(requestListenerStub2);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY2), EasyMock.eq(TEST_DURATION)))
            .andReturn(TEST_RETURNED_DATA);
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
    // TESTING NETWORK MANAGER DEPENDENCY
    // ============================================================================================
    public void testAddRequestWhenNetworkIsDown() throws CacheLoadingException, CacheSavingException, InterruptedException {
        // given
        CachedSpiceRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

        RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
        Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
        requestListenerSet.add(mockRequestListener);

        EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION)))
            .andReturn(null);
        EasyMock.replay(mockCacheManager);

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
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ============================================================================================
    // PRIVATE METHODS
    // ============================================================================================

    private <T> CachedSpiceRequestStub<T> createSuccessfulRequest(Class<T> clazz, Object cacheKey, long maxTimeInCache, T returnedData) {
        SpiceRequestStub<T> stubContentRequest = new SpiceRequestSucceedingStub<T>(clazz, returnedData);
        return new CachedSpiceRequestStub<T>(stubContentRequest, cacheKey, maxTimeInCache);
    }

    private <T> CachedSpiceRequestStub<T> createSuccessfulRequest(Class<T> clazz, Object cacheKey, long maxTimeInCache, T returnedData,
        long waitBeforeExecution) {
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
