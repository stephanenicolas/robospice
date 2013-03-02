package com.octo.android.robospice.persistence.binary;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.InstrumentationTestCase;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;

public class InFileBitmapObjectPersisterTest extends InstrumentationTestCase {

    private static final int TEST_SIZE_RATIO_AFTER_DOWNSAMPLING = 4;
    private static final int BITMAP_HEIGHT = 10;
    private static final int BITMAP_WIDTH = 10;
    private static final int BYTES_PER_PIXEL_ARGB_8888 = 4;
    private static final Bitmap TEST_BITMAP_LARGE = Bitmap.createBitmap(BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888);
    private static final int TEST_BITMAP_LARGE_SIZE = BITMAP_HEIGHT * BITMAP_WIDTH * BYTES_PER_PIXEL_ARGB_8888;

    private static final String TEST_CACHE_KEY = "cacheKey1";

    // in ms
    private static final long TEST_EXPIRATION_DURATION = 1;

    private Application application;
    private InFileBitmapObjectPersister testPersister;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        testPersister = new InFileBitmapObjectPersister(application);
        testPersister.removeAllDataFromCache();
    }

    @Override
    protected void tearDown() throws Exception {
        testPersister.removeAllDataFromCache();
        super.tearDown();
    }

    private void assertBitmapSizeEquals(int expectedBitmapSize, Bitmap bitmap) {
        assertEquals(expectedBitmapSize, bitmap.getRowBytes() * bitmap.getHeight());
    }

    public void testSaveDataToCache() throws Exception {
        assertNotNull(testPersister.saveDataToCacheAndReturnData(TEST_BITMAP_LARGE, TEST_CACHE_KEY));
        assertTrue(testPersister.getCacheFile(TEST_CACHE_KEY).exists());
    }

    public void testLoadDataFromCache_no_expiration() throws Exception {
        testPersister.saveDataToCacheAndReturnData(TEST_BITMAP_LARGE, TEST_CACHE_KEY);
        Bitmap data = testPersister.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ALWAYS_RETURNED);
        assertNotNull(data);
        assertBitmapSizeEquals(TEST_BITMAP_LARGE_SIZE, data);

    }

    public void testLoadDataFromCache_not_expired() throws Exception {
        testPersister.saveDataToCacheAndReturnData(TEST_BITMAP_LARGE, TEST_CACHE_KEY);
        Bitmap data = testPersister.loadDataFromCache(TEST_CACHE_KEY, Long.MAX_VALUE);
        assertNotNull(data);
        assertBitmapSizeEquals(TEST_BITMAP_LARGE_SIZE, data);
    }

    public void testLoadDataFromCache_expired() throws Exception {
        testPersister.saveDataToCacheAndReturnData(TEST_BITMAP_LARGE, TEST_CACHE_KEY);
        Thread.sleep(TEST_EXPIRATION_DURATION);
        try {
            Bitmap data = testPersister.loadDataFromCache(TEST_CACHE_KEY, TEST_EXPIRATION_DURATION);
            assertNull(data);
        } catch (CacheLoadingException e) {
            fail("A cache miss should not throw exception ");
        }
    }

    public void testLoadDataFromCache_with_default_decoding_options() throws Exception {
        testPersister.saveDataToCacheAndReturnData(TEST_BITMAP_LARGE, TEST_CACHE_KEY);
        Bitmap data = testPersister.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ALWAYS_RETURNED);
        assertNotNull(data);
        assertBitmapSizeEquals(TEST_BITMAP_LARGE_SIZE, data);
    }

    public void testLoadDataFromCache_with_decoding_options() throws Exception {
        BitmapFactory.Options decodingOptions = new BitmapFactory.Options();
        decodingOptions.inSampleSize = 2;
        testPersister = new InFileBitmapObjectPersister(application);
        testPersister.setDecodingOptions(decodingOptions);
        testPersister.removeAllDataFromCache();

        testPersister.saveDataToCacheAndReturnData(TEST_BITMAP_LARGE, TEST_CACHE_KEY);
        Bitmap data = testPersister.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ALWAYS_RETURNED);
        assertNotNull(data);
        assertBitmapSizeEquals(TEST_BITMAP_LARGE_SIZE / TEST_SIZE_RATIO_AFTER_DOWNSAMPLING, data);
    }

}
