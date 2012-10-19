package com.octo.android.robospice.persistence.file;

import android.app.Application;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

@MediumTest
public class InFileObjectPersisterFactoryTest extends InstrumentationTestCase {

    InFileObjectPersisterFactory fileBasedClassCacheManagerFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        fileBasedClassCacheManagerFactory = new FileBaseClassCacheManagerFactoryUnderTest( application );
    }

    public void testGetCachePrefix() {
        String actual = fileBasedClassCacheManagerFactory.getCachePrefix();
        assertEquals( FileBaseClassCacheManagerFactoryUnderTest.class.getSimpleName() + "_", actual );
    }

    // ============================================================================================
    // CLASS UNDER TEST
    // ============================================================================================
    private final class FileBaseClassCacheManagerFactoryUnderTest extends InFileObjectPersisterFactory {
        private FileBaseClassCacheManagerFactoryUnderTest( Application application ) {
            super( application );
        }

        @Override
        public boolean canHandleClass( Class< ? > arg0 ) {
            return false;
        }

        @Override
        public < DATA > InFileObjectPersister< DATA > createObjectPersister( Class< DATA > clazz ) {
            return null;
        }

    }

}
