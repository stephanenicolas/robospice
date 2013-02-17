package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.io.IOException;

import android.app.Application;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

@MediumTest
public class InFileObjectPersisterTest extends InstrumentationTestCase {

    InFileObjectPersister<Object> inFileObjectPersister;

    private static final String TEST_CACHE_KEY = "TEST_CACHE_KEY";
    private static final String TEST_CACHE_KEY2 = "TEST_CACHE_KEY2";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        inFileObjectPersister = new InFileObjectPersisterUnderTest(application);
    }

    @Override
    protected void tearDown() throws Exception {
        inFileObjectPersister.removeAllDataFromCache();
        super.tearDown();
    }

    public void testGetCachePrefix() {
        String actual = inFileObjectPersister.getCachePrefix();
        assertEquals(InFileObjectPersisterUnderTest.class.getSimpleName() + "_", actual);
    }

    public void testRemoveDataFromCache() throws Exception {
        inFileObjectPersister.saveDataToCacheAndReturnData(new Object(), TEST_CACHE_KEY);

        File cacheFile = inFileObjectPersister.getCacheFile(TEST_CACHE_KEY);
        assertTrue(cacheFile.exists());

        inFileObjectPersister.removeDataFromCache(TEST_CACHE_KEY);
        assertFalse(cacheFile.exists());
    }

    public void testRemoveAllDataFromCache() throws Exception {
        inFileObjectPersister.saveDataToCacheAndReturnData(new Object(), TEST_CACHE_KEY);

        inFileObjectPersister.saveDataToCacheAndReturnData(new Object(), TEST_CACHE_KEY2);

        File cacheFile = inFileObjectPersister.getCacheFile(TEST_CACHE_KEY);
        assertTrue(cacheFile.exists());

        File cacheFile2 = inFileObjectPersister.getCacheFile(TEST_CACHE_KEY2);
        assertTrue(cacheFile2.exists());

        inFileObjectPersister.removeAllDataFromCache();
        assertFalse(cacheFile.exists());
        assertFalse(cacheFile2.exists());
    }

    // ============================================================================================
    // CLASS UNDER TEST
    // ============================================================================================
    private final class InFileObjectPersisterUnderTest extends InFileObjectPersister<Object> {
        private InFileObjectPersisterUnderTest(Application application) {
            super(application, Object.class);
        }

        @Override
        public boolean canHandleClass(Class<?> arg0) {
            return false;
        }

        @Override
        public Object loadDataFromCache(Object arg0, long arg1) throws CacheLoadingException {
            return null;
        }

        @Override
        protected Object readCacheDataFromFile(File file) throws CacheLoadingException {
            return null;
        }

        @Override
        public Object saveDataToCacheAndReturnData(Object data, Object cacheKey) throws CacheSavingException {
            try {
                getCacheFile(cacheKey).createNewFile();
            } catch (IOException e) {
                throw new CacheSavingException(e);
            }
            return data;
        }
    }

}
