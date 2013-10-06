package com.octo.android.robospice.persistence.string;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

import android.app.Application;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.octo.android.robospice.persistence.DurationInMillis;

@MediumTest
public class InFileStringObjectPersisterTest extends AndroidTestCase {


    private static final long FIVE_SECONDS = 5 * DurationInMillis.ONE_SECOND;
    private static final String TEST_DATA = "foo";
    private static final String TEST_CACHE_KEY = "TEST_CACHE_KEY";

    private InFileStringObjectPersister inFileStringObjectPersister;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Application application = (Application) getContext().getApplicationContext();
        inFileStringObjectPersister = new InFileStringObjectPersister(application);
    }

    public void testSaveDataToCacheAndReturnData() throws Exception {
        inFileStringObjectPersister.saveDataToCacheAndReturnData(TEST_DATA, TEST_CACHE_KEY);

        File cachedFile = inFileStringObjectPersister.getCacheFile(TEST_CACHE_KEY);
        assertTrue(cachedFile.exists());

        @SuppressWarnings("unchecked")
        List<String> actual = IOUtils.readLines(new FileInputStream(cachedFile), CharEncoding.UTF_8);
        assertEquals(1, actual.size());
        assertEquals(TEST_DATA, actual.get(0));
    }

    public void testLoadDataFromCache_no_expiracy() throws Exception {
        File cachedFile = inFileStringObjectPersister.getCacheFile(TEST_CACHE_KEY);

        FileOutputStream output = new FileOutputStream(cachedFile);
        IOUtils.write(TEST_DATA, output, CharEncoding.UTF_8);
        IOUtils.closeQuietly(output);

        String actual = inFileStringObjectPersister.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ALWAYS_RETURNED);
        assertEquals(TEST_DATA, actual);
    }

    public void testLoadDataFromCache_not_expired() throws Exception {
        File cachedFile = inFileStringObjectPersister.getCacheFile(TEST_CACHE_KEY);
        FileOutputStream output = new FileOutputStream(cachedFile);
        IOUtils.write(TEST_DATA, output, CharEncoding.UTF_8);
        IOUtils.closeQuietly(output);

        String actual = inFileStringObjectPersister.loadDataFromCache(TEST_CACHE_KEY, FIVE_SECONDS);
        assertEquals(TEST_DATA, actual);
    }

    public void testLoadDataFromCache_expired() throws Exception {
        File cachedFile = inFileStringObjectPersister.getCacheFile(TEST_CACHE_KEY);
        FileOutputStream output = new FileOutputStream(cachedFile);
        IOUtils.write(TEST_DATA, output, CharEncoding.UTF_8);
        IOUtils.closeQuietly(output);
        cachedFile.setLastModified(System.currentTimeMillis() - FIVE_SECONDS);

        String actual = inFileStringObjectPersister.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ONE_SECOND);
        assertNull(actual);
    }

    @Override
    protected void tearDown() throws Exception {
        inFileStringObjectPersister.removeAllDataFromCache();
    }

}
