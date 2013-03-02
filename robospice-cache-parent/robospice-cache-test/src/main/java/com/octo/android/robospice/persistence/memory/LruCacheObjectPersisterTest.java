package com.octo.android.robospice.persistence.memory;

import android.app.Application;
import android.test.InstrumentationTestCase;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.string.InFileStringObjectPersister;

public class LruCacheObjectPersisterTest extends InstrumentationTestCase {

    private static final String TEST_CACHE_KEY_1 = "cacheKey1";
    private static final String TEST_CACHE_KEY_2 = "cacheKey2";
    private static final String TEST_CACHE_KEY_3 = "cacheKey3";
    private static final String TEST_DATA = "hello world!";
    private static final int TEST_LRU_CACHE_SIZE = TEST_DATA.length() * 2;
    private static final long TEST_EXPIRATION_DURATION_SHORT = 1;
    private static final long TEST_EXPIRATION_DURATION_LONG = TEST_EXPIRATION_DURATION_SHORT * 10;

    private LruCacheStringObjectPersister testPersister;
    private LruCacheStringObjectPersister testPersisterWithFallback;

    @Override
    public void setUp() {
        testPersister = new LruCacheStringObjectPersister(TEST_LRU_CACHE_SIZE);
        Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        testPersisterWithFallback = new LruCacheStringObjectPersister(new InFileStringObjectPersister(application), TEST_LRU_CACHE_SIZE);
    }

    public void testSaveDataToCacheAndReturnData() throws Exception {
        testPersister.saveDataToCacheAndReturnData(TEST_DATA, TEST_CACHE_KEY_1);
        assertNotNull(testPersister.getLruCache().get(TEST_CACHE_KEY_1));
    }

    public void testLoadDataFromCache_with_duration_always() throws Exception {
        testPersister.saveDataToCacheAndReturnData(TEST_DATA, TEST_CACHE_KEY_1);
        assertNotNull(testPersister.loadDataFromCache(TEST_CACHE_KEY_1, DurationInMillis.ALWAYS_RETURNED));
    }

    public void testLoadDataFromCache_with_data_not_expired() throws Exception {
        testPersister.saveDataToCacheAndReturnData(TEST_DATA, TEST_CACHE_KEY_1);
        assertNotNull(testPersister.loadDataFromCache(TEST_CACHE_KEY_1, Long.MAX_VALUE));
    }

    public void testLoadDataFromCache_with_expired_data() throws Exception {
        testPersister.saveDataToCacheAndReturnData(TEST_DATA, TEST_CACHE_KEY_1);
        Thread.sleep(TEST_EXPIRATION_DURATION_LONG);
        assertNull(testPersister.loadDataFromCache(TEST_CACHE_KEY_1, TEST_EXPIRATION_DURATION_SHORT));
    }

    public void testPurgeLeastRecentlyUsed() throws Exception {
        testPersister.saveDataToCacheAndReturnData(TEST_DATA, TEST_CACHE_KEY_1);
        testPersister.saveDataToCacheAndReturnData(TEST_DATA, TEST_CACHE_KEY_2);
        testPersister.saveDataToCacheAndReturnData(TEST_DATA, TEST_CACHE_KEY_3);
        String data = testPersister.loadDataFromCache(TEST_CACHE_KEY_1, DurationInMillis.ALWAYS_RETURNED);
        assertNull("Cache loaded old data that should have been purged by the LRUCache.", data);
    }

    public void testFallback() throws Exception {
        testPersisterWithFallback.saveDataToCacheAndReturnData(TEST_DATA, TEST_CACHE_KEY_1);
        testPersisterWithFallback.getLruCache().evictAll();
        assertNotNull(testPersisterWithFallback.loadDataFromCache(TEST_CACHE_KEY_1, DurationInMillis.ALWAYS_RETURNED));
    }

}
