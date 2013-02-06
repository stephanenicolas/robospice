package com.octo.android.robospice.persistence.binary;

import android.test.InstrumentationTestCase;
import android.support.v4.util.LruCache;
import android.app.Application;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;

public class InMemoryLRUCacheObjectPersisterTest extends
    InstrumentationTestCase {

    private static final int CACHE_SIZE = 2;
    private static final String CACHE_KEY_1 = "cacheKey1";
    private static final String CACHE_KEY_2 = "cacheKey2";
    private static final String CACHE_KEY_3 = "cacheKey3";
    private static final Object GENERIC_DATA = new Object();
    private static final long ONE_MILLISECOND = 1;

    private static final String EXPIRED_DATA_MSG = "Cache loaded expired data instead of throwing a CacheLoadingException.";
    private static final String DID_NOT_PURGE_LRU_MSG = "Cache loaded old data that should have been purged by the LRUCache.";

    private InMemoryLRUCacheObjectPersister<Object> testPersister;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Application application = (Application) getInstrumentation()
            .getTargetContext().getApplicationContext();

        testPersister = new InMemoryLRUCacheObjectPersister<Object>(
            application, Object.class) {

            @Override
            protected LruCache<Object, CacheItem<Object>> instantiateLRUCache() {
                return new LruCache<Object, CacheItem<Object>>(CACHE_SIZE);
            }
        };
    }

    public void testSaveDataToCacheAndReturnData() throws Exception {

        testPersister.saveDataToCacheAndReturnData(GENERIC_DATA, CACHE_KEY_1);
        assertNotNull(testPersister.getMemoryCache().get(CACHE_KEY_1));
    }

    public void testLoadDataFromCacheNoExpiration() throws Exception {
        testPersister.saveDataToCacheAndReturnData(GENERIC_DATA, CACHE_KEY_1);
        Object data = testPersister.loadDataFromCache(CACHE_KEY_1,
            DurationInMillis.ALWAYS);
        assertNotNull(data);
    }

    public void testLoadDataFromCacheNotExpired() throws Exception {
        testPersister.saveDataToCacheAndReturnData(GENERIC_DATA, CACHE_KEY_1);
        Object data = testPersister.loadDataFromCache(CACHE_KEY_1,
            Long.MAX_VALUE);
        assertNotNull(data);
    }

    public void testLoadDataFromCacheExpired() throws Exception {
        testPersister.saveDataToCacheAndReturnData(GENERIC_DATA, CACHE_KEY_1);
        Thread.sleep(ONE_MILLISECOND);
        try {
            Object data = testPersister.loadDataFromCache(CACHE_KEY_1,
                ONE_MILLISECOND);
            throw new Exception(EXPIRED_DATA_MSG);
        } catch (CacheLoadingException e) {
            // throwing this error is the expected behavior
        }
    }

    public void testPurgeLeastRecentlyUsed() throws Exception {
        testPersister.saveDataToCacheAndReturnData(GENERIC_DATA, CACHE_KEY_1);
        testPersister.saveDataToCacheAndReturnData(GENERIC_DATA, CACHE_KEY_2);
        testPersister.saveDataToCacheAndReturnData(GENERIC_DATA, CACHE_KEY_3);
        try {
            testPersister.loadDataFromCache(CACHE_KEY_1,
                DurationInMillis.ALWAYS);
            throw new Exception(DID_NOT_PURGE_LRU_MSG);
        } catch (CacheLoadingException e) {
            // throwing this error is the expected behavior
        }
    }

}
