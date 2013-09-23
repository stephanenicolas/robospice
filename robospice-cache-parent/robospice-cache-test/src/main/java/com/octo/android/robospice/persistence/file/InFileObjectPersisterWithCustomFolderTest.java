package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.test.suitebuilder.annotation.MediumTest;

import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterTest.InFileObjectPersisterUnderTest;

@MediumTest
public class InFileObjectPersisterWithCustomFolderTest extends AbstractInFileObjectPersisterTest {

    InFileObjectPersister<Object> inFileObjectPersister;

    private static final String TEST_CACHE_FOLDER = "rs-test-folder";
    private static final String TEST_CACHE_KEY = "TEST_CACHE_KEY";
    private static final String TEST_CACHE_KEY2 = "TEST_CACHE_KEY2";

    @Override
    protected void setUp() throws Exception {
        Application application = (Application) getContext().getApplicationContext();
        inFileObjectPersister = new InFileObjectPersisterWithCustomFolderUnderTest(application);
        super.setUp(inFileObjectPersister);
    }

    public void testRemoveDataFromCache() throws Exception {
        super.testRemoveDataFromCache(new Object(), TEST_CACHE_KEY);
    }

    public void testRemoveAllDataFromCache() throws Exception {
        Map<Object, Object> mapDataToCacheKey = new HashMap<Object, Object>();
        mapDataToCacheKey.put(new Object(), TEST_CACHE_KEY);
        mapDataToCacheKey.put(new Object(), TEST_CACHE_KEY2);
        super.testRemoveAllDataFromCache(mapDataToCacheKey);
    }

    // ============================================================================================
    // CLASS UNDER TEST
    // ============================================================================================
    private final class InFileObjectPersisterWithCustomFolderUnderTest extends InFileObjectPersisterUnderTest {
        InFileObjectPersisterWithCustomFolderUnderTest(Application application) throws CacheCreationException {
            super(application);
            setCacheFolder(new File(application.getCacheDir(), TEST_CACHE_FOLDER));
        }
    }

}
