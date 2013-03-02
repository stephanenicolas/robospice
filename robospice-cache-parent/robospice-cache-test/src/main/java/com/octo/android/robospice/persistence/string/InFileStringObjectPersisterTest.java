package com.octo.android.robospice.persistence.string;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

import android.app.Application;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.octo.android.robospice.persistence.DurationInMillis;

@MediumTest
public class InFileStringObjectPersisterTest extends InstrumentationTestCase {

    private static final long FIVE_SECONDS = 5 * DurationInMillis.ONE_SECOND;

    private static final String TEST_CACHE_KEY = "TEST_CACHE_KEY";

    private InFileStringObjectPersister inFileStringObjectPersister;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        inFileStringObjectPersister = new InFileStringObjectPersister(application);
    }

    public void testSaveDataToCacheAndReturnData() throws Exception {
        inFileStringObjectPersister.saveDataToCacheAndReturnData("coucou", TEST_CACHE_KEY);

        File cachedFile = inFileStringObjectPersister.getCacheFile(TEST_CACHE_KEY);
        assertTrue(cachedFile.exists());

        @SuppressWarnings("unchecked")
        List<String> actual = IOUtils.readLines(new FileInputStream(cachedFile), CharEncoding.UTF_8);
        assertEquals(1, actual.size());
        assertEquals("coucou", actual.get(0));
    }

    public void testLoadDataFromCache_no_expiracy() throws Exception {
        File cachedFile = inFileStringObjectPersister.getCacheFile(TEST_CACHE_KEY);
        IOUtils.write("coucou", new FileOutputStream(cachedFile), CharEncoding.UTF_8);

        String actual = inFileStringObjectPersister.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ALWAYS_RETURNED);
        assertEquals("coucou", actual);
    }

    public void testLoadDataFromCache_not_expired() throws Exception {
        File cachedFile = inFileStringObjectPersister.getCacheFile(TEST_CACHE_KEY);
        IOUtils.write("coucou", new FileOutputStream(cachedFile), CharEncoding.UTF_8);

        String actual = inFileStringObjectPersister.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ONE_SECOND);
        assertEquals("coucou", actual);
    }

    public void testLoadDataFromCache_expired() throws Exception {
        File cachedFile = inFileStringObjectPersister.getCacheFile(TEST_CACHE_KEY);
        IOUtils.write("coucou", new FileOutputStream(cachedFile), CharEncoding.UTF_8);
        cachedFile.setLastModified(System.currentTimeMillis() - FIVE_SECONDS);

        String actual = inFileStringObjectPersister.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ONE_SECOND);
        assertNull(actual);
    }

    @Override
    protected void tearDown() throws Exception {
        inFileStringObjectPersister.removeAllDataFromCache();
    }

}
