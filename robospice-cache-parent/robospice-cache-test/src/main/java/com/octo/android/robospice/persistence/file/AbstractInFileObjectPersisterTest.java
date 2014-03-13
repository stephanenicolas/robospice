package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.util.List;
import java.util.Map;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

@MediumTest
public abstract class AbstractInFileObjectPersisterTest extends AndroidTestCase {

    private static final int TEST_DATE_IN_CACHE = 5000;
    private static final long TEST_EXPIRATION_DURATION = 1000;
    private static final long TEST_EXPIRATION_DURATION_LONG = 3000;
    protected InFileObjectPersister<Object> inFileObjectPersister;

    protected void setUp(InFileObjectPersister<Object> inFileObjectPersister) throws Exception {
        super.setUp();
        this.inFileObjectPersister = inFileObjectPersister;
    }

    @Override
    protected void tearDown() throws Exception {
        inFileObjectPersister.removeAllDataFromCache();
        super.tearDown();
    }

    public void testGetCachePrefix() {
        String actual = inFileObjectPersister.getCachePrefix();
        assertEquals(inFileObjectPersister.getClass().getSimpleName() + "_" + inFileObjectPersister.getHandledClass().getSimpleName() + "_", actual);
    }

    public void testRemoveDataFromCache(Object data, Object cacheKey) throws Exception {
        // given

        // when
        inFileObjectPersister.saveDataToCacheAndReturnData(data, cacheKey);

        // then
        File cacheFile = inFileObjectPersister.getCacheFile(cacheKey);
        assertTrue(cacheFile.exists());

        // when
        inFileObjectPersister.removeDataFromCache(cacheKey);

        // then
        assertFalse(cacheFile.exists());
    }

    public void testRemoveAllDataFromCache(Map<Object, Object> mapDataToCacheKey) throws Exception {
        // given

        // when
        for (Map.Entry<Object, Object> entry : mapDataToCacheKey.entrySet()) {
            inFileObjectPersister.saveDataToCacheAndReturnData(entry.getKey(), entry.getValue());
        }

        // then
        for (Map.Entry<Object, Object> entry : mapDataToCacheKey.entrySet()) {
            File cacheFile = inFileObjectPersister.getCacheFile(entry.getValue());
            assertTrue(cacheFile.exists());
        }

        // when
        inFileObjectPersister.removeAllDataFromCache();

        // then
        for (Map.Entry<Object, Object> entry : mapDataToCacheKey.entrySet()) {
            File cacheFile = inFileObjectPersister.getCacheFile(entry.getValue());
            assertFalse(cacheFile.exists());
        }
    }

    public void testGetAllCacheKeys(Map<Object, Object> mapDataToCacheKey) throws Exception {
        // given
        for (Map.Entry<Object, Object> entry : mapDataToCacheKey.entrySet()) {
            inFileObjectPersister.saveDataToCacheAndReturnData(entry.getKey(), entry.getValue());
        }

        // when
        List<Object> allCacheKeys = inFileObjectPersister.getAllCacheKeys();

        // then
        assertEquals(mapDataToCacheKey.values().size(), allCacheKeys.size());

        allCacheKeys.removeAll(mapDataToCacheKey.values());
        assertTrue(allCacheKeys.isEmpty());
    }

    public void testIsDataInCache_not_expired(Object data, Object cacheKey) throws Exception {
        inFileObjectPersister.saveDataToCacheAndReturnData(data, cacheKey);
        assertTrue(inFileObjectPersister.isDataInCache(cacheKey, Long.MAX_VALUE));
    }

    public void testIsDataInCache_expired(Object data, Object cacheKey) throws Exception {
        inFileObjectPersister.saveDataToCacheAndReturnData(data, cacheKey);
        Thread.sleep(TEST_EXPIRATION_DURATION);
        assertFalse(inFileObjectPersister.isDataInCache(cacheKey, TEST_EXPIRATION_DURATION));
    }

    public void testIsDataInCache_with_removal(Object data, Object cacheKey) throws Exception {
        // given

        // when
        inFileObjectPersister.saveDataToCacheAndReturnData(data, cacheKey);

        // then
        assertTrue(inFileObjectPersister.isDataInCache(cacheKey, TEST_EXPIRATION_DURATION_LONG));

        // when
        inFileObjectPersister.removeDataFromCache(cacheKey);

        // then
        assertFalse(inFileObjectPersister.isDataInCache(cacheKey, Long.MAX_VALUE));
    }

    public void testGetDateOfDataInCache_when_there_is_some_data_in_cache(Object data, Object cacheKey) throws Exception {
        // given
        inFileObjectPersister.saveDataToCacheAndReturnData(data, cacheKey);

        // when
        inFileObjectPersister.getCacheFile(cacheKey).setLastModified(TEST_DATE_IN_CACHE);

        // then
        assertEquals(TEST_DATE_IN_CACHE, inFileObjectPersister.getCreationDateInCache(cacheKey));

    }

    public void testGetDateOfDataInCache_when_there_is_no_data_in_cache(Object data, Object cacheKey) throws Exception {
        // given
        inFileObjectPersister.removeDataFromCache(cacheKey);

        // when

        // then
        try {
            inFileObjectPersister.getCreationDateInCache(cacheKey);
            fail("Should have thrown an exception");
        } catch (Exception ex) {
            // nothing
            assertTrue(true);
        }

    }
}
