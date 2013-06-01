package com.octo.android.robospice.persistence.file;

import java.io.File;

import android.app.Application;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.octo.android.robospice.persistence.exception.CacheCreationException;

@MediumTest
public class InFileObjectPersisterFactoryTest extends InstrumentationTestCase {

    protected InFileObjectPersisterFactory inFileObjectPersisterFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
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
        assertEquals(
                new File(getInstrumentation().getTargetContext().getCacheDir(), InFileObjectPersister.DEFAULT_ROOT_CACHE_DIR),
                actual);

    }

    public void testGetCacheFolder_custom() throws CacheCreationException {

        // given
        final File cacheFolder = getInstrumentation().getTargetContext().getCacheDir();

        // when
        inFileObjectPersisterFactory.setCacheFolder(cacheFolder);
        File actual2 = inFileObjectPersisterFactory.getCacheFolder();

        // then
        assertEquals(cacheFolder, actual2);

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

}
