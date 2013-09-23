package com.octo.android.robospice.persistence.file;

import java.io.File;

import android.app.Application;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.persistence.string.InFileStringObjectPersister;

@MediumTest
public class InFileObjectPersisterFactoryTest extends AndroidTestCase {
    private static final String TEST_CACHE_KEY = "FOO";
    private static final String TEST_PERSISTED_STRING = "TEST";

    protected InFileObjectPersisterFactory inFileObjectPersisterFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Application application = (Application) getContext().getApplicationContext();
        inFileObjectPersisterFactory = new InFileObjectPersisterFactoryUnderTest(application);
    }

    public void testGetCachePrefix_default() {
        // given

        // when
        String actual = inFileObjectPersisterFactory.getCachePrefix();

        // then
        assertEquals(InFileObjectPersisterFactoryUnderTest.class.getSimpleName() + "_", actual);
    }

    public void testGetCachePrefix_custom() {

        // given
        final String cachePrefix = "foo";

        // when
        inFileObjectPersisterFactory.setCachePrefix(cachePrefix);
        String actual2 = inFileObjectPersisterFactory.getCachePrefix();

        // then
        assertEquals(cachePrefix, actual2);

    }

    public void testGetCacheFolder_default() {
        // given

        // when
        File actual = inFileObjectPersisterFactory.getCacheFolder();

        // then
        assertEquals(new File(getContext().getCacheDir(), InFileObjectPersister.DEFAULT_ROOT_CACHE_DIR), actual);

    }

    public void testGetCacheFolder_custom() throws CacheCreationException {

        // given
        final File cacheFolder = getContext().getCacheDir();

        // when
        inFileObjectPersisterFactory.setCacheFolder(cacheFolder);
        File actual2 = inFileObjectPersisterFactory.getCacheFolder();

        // then
        assertEquals(cacheFolder, actual2);

    }

    public void testRemoveAllDataFromCache_cleans_cache_before_a_factory_creates_persisters() throws SpiceException {

        // given
        // create a persister but don't register it, use it directly to create
        // cache content
        InFileObjectPersister<String> inFileStringObjectPersister = new InFileStringObjectPersister((Application) getContext().getApplicationContext());
        // create a factory
        InFileObjectPersisterFactoryThatCreatesPersisterUnderTest mockFactoryPersister = new InFileObjectPersisterFactoryThatCreatesPersisterUnderTest((Application) getContext().getApplicationContext());
        inFileStringObjectPersister.setFactoryCachePrefix(mockFactoryPersister.getCachePrefix());
        inFileStringObjectPersister.saveDataToCacheAndReturnData(TEST_PERSISTED_STRING, TEST_CACHE_KEY);

        // when
        String dataInCache = inFileStringObjectPersister.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ALWAYS_RETURNED);

        // then
        assertEquals(TEST_PERSISTED_STRING, dataInCache);

        // when
        mockFactoryPersister.removeAllDataFromCache();
        String dataInCacheAfterClean = inFileStringObjectPersister.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ALWAYS_RETURNED);

        // then
        assertNull(dataInCacheAfterClean);
    }

    // ============================================================================================
    // CLASS UNDER TEST
    // ============================================================================================
    private final class InFileObjectPersisterFactoryUnderTest extends InFileObjectPersisterFactory {
        private InFileObjectPersisterFactoryUnderTest(Application application) throws CacheCreationException {
            super(application);
        }

        @Override
        public boolean canHandleClass(Class<?> arg0) {
            return false;
        }

        @Override
        public <DATA> InFileObjectPersister<DATA> createInFileObjectPersister(Class<DATA> clazz, File cacheFolder) {
            return null;
        }

    }

    private class InFileObjectPersisterFactoryThatCreatesPersisterUnderTest extends InFileObjectPersisterFactory {

        public InFileObjectPersisterFactoryThatCreatesPersisterUnderTest(Application application) throws CacheCreationException {
            super(application);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> InFileObjectPersister<T> createInFileObjectPersister(Class<T> clazz, File cacheFolder) throws CacheCreationException {
            if (clazz.equals(String.class)) {
                return (InFileObjectPersister<T>) new InFileStringObjectPersister(getApplication());
            } else {
                return null;
            }
        }
    }

}
