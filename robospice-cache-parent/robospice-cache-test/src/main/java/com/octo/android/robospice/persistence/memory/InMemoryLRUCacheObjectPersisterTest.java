package com.octo.android.robospice.persistence.memory;

import android.app.Application;
import android.support.v4.util.LruCache;
import android.test.InstrumentationTestCase;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.string.InFileStringObjectPersister;

public class InMemoryLRUCacheObjectPersisterTest extends
    InstrumentationTestCase {

    private static final int    CACHE_SIZE      = 2;
    private static final String CACHE_KEY_1     = "cacheKey1";
    private static final String CACHE_KEY_2     = "cacheKey2";
    private static final String CACHE_KEY_3     = "cacheKey3";
    private static final String GENERIC_DATA    = "hello world!";
    private static final long   ONE_MILLISECOND = 1;
    private static final long   TEN_MILLISECONDS = ONE_MILLISECOND * 10;

    private static final String EXPIRED_DATA_MSG      = "Cache loaded expired data instead of throwing a CacheLoadingException.";
    private static final String DID_NOT_PURGE_LRU_MSG = "Cache loaded old data that should have been purged by the LRUCache.";

    private TestLRUPersister testPersister;
    private TestLRUPersister testPersisterWithFallback;

    private class TestLRUPersister extends InMemoryLRUCacheObjectPersister<String>
    {

        private TestLRUPersister( Application application )
        {
            super( application, String.class );
        }

        private TestLRUPersister( Application application,
                                  ObjectPersister<String> fallback )
        {
            super( application, String.class, fallback );
        }

        @Override
        protected LruCache<Object, CacheItem<String>> instantiateLRUCache()
        {
            return new LruCache<Object, CacheItem<String>>(CACHE_SIZE);
        }

        // increase visibility
        @Override
        public LruCache<Object, CacheItem<String>> getMemoryCache()
        {
            return super.getMemoryCache();
        }


    }

    public void setUp() {
        Application application = (Application) getInstrumentation()
            .getTargetContext().getApplicationContext();
        testPersister = new TestLRUPersister( application );

        InFileStringObjectPersister fallbackPersister = new InFileStringObjectPersister( application );

        testPersisterWithFallback = new TestLRUPersister( application, fallbackPersister );
    }

    public void tearDown() {
        testPersisterWithFallback.getFallbackPersister().removeAllDataFromCache();
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
        Thread.sleep(TEN_MILLISECONDS);
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

    public void testFallback() throws Exception {
        testPersisterWithFallback.saveDataToCacheAndReturnData(GENERIC_DATA, CACHE_KEY_1);
        testPersisterWithFallback.getMemoryCache().evictAll();
        assertNotNull( testPersisterWithFallback.loadDataFromCache( CACHE_KEY_1, DurationInMillis.ALWAYS) );
    }

}
