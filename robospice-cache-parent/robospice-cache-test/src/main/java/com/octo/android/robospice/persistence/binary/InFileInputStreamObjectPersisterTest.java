package com.octo.android.robospice.persistence.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;

import android.app.Application;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.octo.android.robospice.persistence.DurationInMillis;

@MediumTest
public class InFileInputStreamObjectPersisterTest extends InstrumentationTestCase {

    private static final long FIVE_SECONDS = 5 * DurationInMillis.ONE_SECOND;

    private static final String TEST_CACHE_KEY = "TEST_CACHE_KEY";

    private InFileInputStreamObjectPersister inputStreamCacheManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        inputStreamCacheManager = new InFileInputStreamObjectPersister(application);
    }

    public void testSaveDataToCacheAndReturnData() throws Exception {
        inputStreamCacheManager.saveDataToCacheAndReturnData(new ByteArrayInputStream("coucou".getBytes()), TEST_CACHE_KEY);

        File cachedFile = inputStreamCacheManager.getCacheFile(TEST_CACHE_KEY);
        assertTrue(cachedFile.exists());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileInputStream input = new FileInputStream(cachedFile);
        IOUtils.copy(input, bos);
        IOUtils.closeQuietly(input);

        assertTrue(Arrays.equals("coucou".getBytes(), bos.toByteArray()));
    }

    public void testLoadDataFromCache_no_expiracy() throws Exception {
        File cachedFile = inputStreamCacheManager.getCacheFile(TEST_CACHE_KEY);
        FileOutputStream fileOutputStream = new FileOutputStream(cachedFile);
        IOUtils.write("coucou", fileOutputStream);
        IOUtils.closeQuietly(fileOutputStream);

        InputStream inputStream = inputStreamCacheManager.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ALWAYS_RETURNED);
        byte[] actual = IOUtils.toByteArray(inputStream);
        IOUtils.closeQuietly(inputStream);
        assertTrue(Arrays.equals("coucou".getBytes(), actual));
    }

    public void testLoadDataFromCache_not_expired() throws Exception {
        File cachedFile = inputStreamCacheManager.getCacheFile(TEST_CACHE_KEY);
        FileOutputStream fileOutputStream = new FileOutputStream(cachedFile);
        IOUtils.write("coucou", fileOutputStream);
        IOUtils.closeQuietly(fileOutputStream);

        InputStream inputStream = inputStreamCacheManager.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ONE_SECOND);
        byte[] actual = IOUtils.toByteArray(inputStream);
        IOUtils.closeQuietly(inputStream);
        assertTrue(Arrays.equals("coucou".getBytes(), actual));
    }

    public void testLoadDataFromCache_expired() throws Exception {
        File cachedFile = inputStreamCacheManager.getCacheFile(TEST_CACHE_KEY);
        FileOutputStream fileOutputStream = new FileOutputStream(cachedFile);
        IOUtils.write("coucou", fileOutputStream);
        IOUtils.closeQuietly(fileOutputStream);
        cachedFile.setLastModified(System.currentTimeMillis() - FIVE_SECONDS);

        InputStream inputStream = inputStreamCacheManager.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ONE_SECOND);
        assertNull(inputStream);
    }

    @Override
    protected void tearDown() throws Exception {
        inputStreamCacheManager.removeAllDataFromCache();
        super.tearDown();
    }

}
