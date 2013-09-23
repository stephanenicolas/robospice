package com.octo.android.robospice.persistence.file;

import java.util.HashMap;
import java.util.Map;

import android.app.Application;
import android.test.suitebuilder.annotation.MediumTest;

import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterTest.InFileObjectPersisterUnderTest;
import com.octo.android.robospice.persistence.keysanitation.DefaultKeySanitizer;

@MediumTest
public class InFileObjectPersisterWithSanitationTest extends AbstractInFileObjectPersisterTest {

    InFileObjectPersister<Object> inFileObjectPersister;

    private static final String TEST_CACHE_KEY = "TEST_CACHE_KEY";
    private static final String TEST_CACHE_KEY2 = "TEST_CACHE_KEY2";

    @Override
    protected void setUp() throws Exception {
        Application application = (Application) getContext().getApplicationContext();
        inFileObjectPersister = new InFileObjectPersisterWithSanitationUnderTest(application);
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
    private final class InFileObjectPersisterWithSanitationUnderTest extends InFileObjectPersisterUnderTest {
        InFileObjectPersisterWithSanitationUnderTest(Application application) throws CacheCreationException {
            super(application);
            setKeySanitizer(new DefaultKeySanitizer());
        }
    }

}
