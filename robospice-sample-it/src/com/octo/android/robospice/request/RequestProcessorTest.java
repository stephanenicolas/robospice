package com.octo.android.robospice.request;

import java.util.HashSet;
import java.util.Set;

import org.easymock.EasyMock;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.exception.CacheLoadingException;
import com.octo.android.robospice.exception.CacheSavingException;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ICacheManager;
import com.octo.android.robospice.request.RequestListener;
import com.octo.android.robospice.request.RequestProcessor;
import com.octo.android.robospice.stub.CachedContentRequestStub;
import com.octo.android.robospice.stub.ContentRequestFailingStub;
import com.octo.android.robospice.stub.ContentRequestStub;
import com.octo.android.robospice.stub.ContentRequestSucceedingStub;
import com.octo.android.robospice.stub.RequestListenerStub;

@SmallTest
public class RequestProcessorTest extends InstrumentationTestCase {

	private final static Class<String> TEST_CLASS = String.class;
	private final static String TEST_CACHE_KEY = "12345";
	private final static long TEST_DURATION = DurationInMillis.ONE_SECOND;
	private final static String TEST_RETURNED_DATA = "coucou";
	private static final long REQUEST_COMPLETION_TIME_OUT = 1000;
	private static final long WAIT_BEFORE_REQUEST_EXECUTION = 1000;

	private ICacheManager mockCacheManager;
	private RequestProcessor requestProcessorUnderTest;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mockCacheManager = EasyMock.createMock(ICacheManager.class);
		requestProcessorUnderTest = new RequestProcessor(getInstrumentation().getTargetContext(), mockCacheManager, 1);
	}

	// ============================================================================================
	// TESTING WITH FAIL ON ERROR = false
	// ============================================================================================

	public void testAddRequest_when_cache_is_not_used() throws CacheLoadingException, CacheSavingException, InterruptedException {
		// given
		String cacheKey = null;
		CachedContentRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, cacheKey, TEST_DURATION, TEST_RETURNED_DATA);

		RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
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
	}

	public void testAddRequest_when_something_is_found_in_cache() throws CacheLoadingException, CacheSavingException, InterruptedException {
		// given
		CachedContentRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

		RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
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
	}

	public void testAddRequest_when_nothing_is_found_in_cache_and_request_succeeds() throws CacheLoadingException, CacheSavingException, InterruptedException {
		// given
		CachedContentRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

		RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
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
	}

	public void testAddRequest_when_nothing_is_found_in_cache_and_request_fails() throws CacheLoadingException, CacheSavingException, InterruptedException {
		// given
		CachedContentRequestStub<String> stubRequest = createFailedRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION);

		RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
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
	}

	public void testAddRequest_when_saving_to_cache_throws_exception() throws CacheLoadingException, CacheSavingException, InterruptedException {
		// given
		CachedContentRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

		RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
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
	}

	// ============================================================================================
	// TESTING WITH FAIL ON ERROR = true
	// ============================================================================================

	public void testAddRequest_fail_on_error_true_when_nothing_is_found_in_cache() throws CacheLoadingException, CacheSavingException, InterruptedException {
		// given
		CachedContentRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

		RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
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
	}

	public void testAddRequest_when_fail_on_error_true_loading_from_cache_throws_exception() throws CacheLoadingException, CacheSavingException, InterruptedException {
		// given
		CachedContentRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

		RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
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
	}

	public void testAddRequest_when_fail_on_error_true_saving_to_cache_throws_exception() throws CacheLoadingException, CacheSavingException, InterruptedException {
		// given
		CachedContentRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);

		RequestListenerStub<String> mockRequestListener = new RequestListenerStub<String>();
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
	}

	// ============================================================================================
	// DO NOT NOTIFY LISTENERS
	// ============================================================================================
	public void test_dontNotifyRequestListenersForRequest_with_2_request_and_one_not_notified() throws InterruptedException, CacheLoadingException, CacheSavingException {
		// given
		CachedContentRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA, WAIT_BEFORE_REQUEST_EXECUTION);
		CachedContentRequestStub<String> stubRequest2 = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
		RequestListenerStub<String> requestListenerStub = new RequestListenerStub<String>();
		RequestListenerStub<String> requestListenerStub2 = new RequestListenerStub<String>();
		Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
		requestListenerSet.add(requestListenerStub);
		Set<RequestListener<?>> requestListenerSet2 = new HashSet<RequestListener<?>>();
		requestListenerSet2.add(requestListenerStub2);

		EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null).times(2);
		EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA).times(2);
		EasyMock.replay(mockCacheManager);

		// when
		requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);
		requestProcessorUnderTest.dontNotifyRequestListenersForRequest(stubRequest.getContentRequest(), requestListenerSet);
		requestProcessorUnderTest.addRequest(stubRequest2, requestListenerSet2);

		stubRequest.await(WAIT_BEFORE_REQUEST_EXECUTION + REQUEST_COMPLETION_TIME_OUT);
		requestListenerStub2.await(REQUEST_COMPLETION_TIME_OUT);

		// test
		EasyMock.verify(mockCacheManager);
		assertTrue(stubRequest.isLoadDataFromNetworkCalled());
		assertTrue(stubRequest2.isLoadDataFromNetworkCalled());
		assertNull(requestListenerStub.isSuccessful());
		assertTrue(requestListenerStub2.isSuccessful());
	}

	// ============================================================================================
	// DO NOT NOTIFY LISTENERS
	// ============================================================================================
	public void test_addRequest_with_2_requests_and_one_is_cancelled() throws InterruptedException, CacheLoadingException, CacheSavingException {
		// given
		CachedContentRequestStub<String> stubRequest = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
		CachedContentRequestStub<String> stubRequest2 = createSuccessfulRequest(TEST_CLASS, TEST_CACHE_KEY, TEST_DURATION, TEST_RETURNED_DATA);
		RequestListenerStub<String> requestListenerStub = new RequestListenerStub<String>();
		RequestListenerStub<String> requestListenerStub2 = new RequestListenerStub<String>();
		Set<RequestListener<?>> requestListenerSet = new HashSet<RequestListener<?>>();
		requestListenerSet.add(requestListenerStub);
		Set<RequestListener<?>> requestListenerSet2 = new HashSet<RequestListener<?>>();
		requestListenerSet2.add(requestListenerStub2);

		EasyMock.expect(mockCacheManager.loadDataFromCache(EasyMock.eq(TEST_CLASS), EasyMock.eq(TEST_CACHE_KEY), EasyMock.eq(TEST_DURATION))).andReturn(null);
		EasyMock.expect(mockCacheManager.saveDataToCacheAndReturnData(EasyMock.eq(TEST_RETURNED_DATA), EasyMock.eq(TEST_CACHE_KEY))).andReturn(TEST_RETURNED_DATA);
		EasyMock.replay(mockCacheManager);

		stubRequest.cancel();

		// when
		requestProcessorUnderTest.addRequest(stubRequest, requestListenerSet);
		requestProcessorUnderTest.addRequest(stubRequest2, requestListenerSet2);
		requestListenerStub2.await(REQUEST_COMPLETION_TIME_OUT);

		// test
		EasyMock.verify(mockCacheManager);
		assertFalse(stubRequest.isLoadDataFromNetworkCalled());
		assertTrue(stubRequest2.isLoadDataFromNetworkCalled());
		assertFalse(requestListenerStub.isSuccessful());
		assertTrue(requestListenerStub.getReceivedException() instanceof RequestCancelledException);
		assertTrue(requestListenerStub2.isSuccessful());
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
		// given
		final Class<?> TEST_CLASS = String.class;
		mockCacheManager.removeAllDataFromCache(TEST_CLASS);
		EasyMock.replay(mockCacheManager);

		// when
		requestProcessorUnderTest.removeAllDataFromCache(TEST_CLASS);

		// then
		EasyMock.verify(mockCacheManager);
	}

	public void testRemoveAllDataFromCache_for_given_class_and_cachekey() {
		// given
		final Class<?> TEST_CLASS = String.class;
		final String TEST_CACHE_KEY = "12345";
		EasyMock.expect(mockCacheManager.removeDataFromCache(TEST_CLASS, TEST_CACHE_KEY)).andReturn(true);
		EasyMock.replay(mockCacheManager);

		// when
		requestProcessorUnderTest.removeDataFromCache(TEST_CLASS, TEST_CACHE_KEY);

		// then
		EasyMock.verify(mockCacheManager);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// ============================================================================================
	// PRIVATE METHODS
	// ============================================================================================

	private <T> CachedContentRequestStub<T> createSuccessfulRequest(Class<T> clazz, String cacheKey, long maxTimeInCache, T returnedData) {
		ContentRequestStub<T> stubContentRequest = new ContentRequestSucceedingStub<T>(clazz, returnedData);
		return new CachedContentRequestStub<T>(stubContentRequest, cacheKey, maxTimeInCache);
	}

	private <T> CachedContentRequestStub<T> createSuccessfulRequest(Class<T> clazz, String cacheKey, long maxTimeInCache, T returnedData, long waitBeforeExecution) {
		ContentRequestStub<T> stubContentRequest = new ContentRequestSucceedingStub<T>(clazz, returnedData, waitBeforeExecution);
		return new CachedContentRequestStub<T>(stubContentRequest, cacheKey, maxTimeInCache);
	}

	private <T> CachedContentRequestStub<T> createFailedRequest(Class<T> clazz, String cacheKey, long maxTimeInCache) {
		ContentRequestStub<T> stubContentRequest = new ContentRequestFailingStub<T>(clazz);
		return new CachedContentRequestStub<T>(stubContentRequest, cacheKey, maxTimeInCache);
	}
}
