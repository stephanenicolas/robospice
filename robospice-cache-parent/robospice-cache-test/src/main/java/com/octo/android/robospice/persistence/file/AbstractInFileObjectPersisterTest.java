package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.util.List;
import java.util.Map;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

@MediumTest
public abstract class AbstractInFileObjectPersisterTest extends InstrumentationTestCase {

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
        assertEquals(inFileObjectPersister.getClass().getSimpleName() + "_", actual);
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

}
