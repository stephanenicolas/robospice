package com.octo.android.robospice.persistence.binary;

import android.test.InstrumentationTestCase;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.app.Application;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;

public class InFileBitmapObjectPersisterTest extends InstrumentationTestCase {

    private static final int BITMAP_HEIGHT = 10;
    private static final int BITMAP_WIDTH = 10;
    private static final int BYTES_PER_PIXEL_ARGB_8888 = 4;
    private static final int BYTES_PER_PIXEL_RGB_565 = 2;
    private static final Bitmap testBitmapLarge = Bitmap.createBitmap(
        BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.ARGB_8888);
    private static final Bitmap testBitmapSmall = Bitmap.createBitmap(
        BITMAP_WIDTH, BITMAP_HEIGHT, Bitmap.Config.RGB_565);

    private static final String CACHE_KEY_1 = "cacheKey1";

    private static final int LARGE_BITMAP_SIZE = BITMAP_HEIGHT * BITMAP_WIDTH
        * BYTES_PER_PIXEL_ARGB_8888;
    private static final int SMALL_BITMAP_SIZE = BITMAP_HEIGHT * BITMAP_WIDTH
        * BYTES_PER_PIXEL_RGB_565;
    private static final long ONE_MILLISECOND = 1;
    private static final String EXPIRED_DATA_MSG = "Cache loaded expired data instead of throwing a CacheLoadingException.";

    private Application application;
    private InFileBitmapObjectPersister testPersister;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        application = (Application) getInstrumentation().getTargetContext()
            .getApplicationContext();

        testPersister = new InFileBitmapObjectPersister(application);
        testPersister.removeAllDataFromCache();
    }

    private void assertBitmapSizeEquals(Bitmap bitmap, int bytes) {
        assertEquals(bitmap.getRowBytes() * bitmap.getHeight(), bytes);
    }

    public void testSaveDataToCache() throws Exception {
        assertNotNull(testPersister.saveDataToCacheAndReturnData(
            testBitmapLarge, CACHE_KEY_1));
        assertTrue(testPersister.getCacheFile(CACHE_KEY_1).exists());
    }

    public void testLoadDataFromCacheNoExpiration() throws Exception {
        testPersister
            .saveDataToCacheAndReturnData(testBitmapLarge, CACHE_KEY_1);
        Bitmap data = testPersister.loadDataFromCache(CACHE_KEY_1,
            DurationInMillis.ALWAYS);
        assertNotNull(data);
        assertBitmapSizeEquals(data, LARGE_BITMAP_SIZE);

    }

    public void testLoadDataFromCacheNotExpired() throws Exception {
        testPersister
            .saveDataToCacheAndReturnData(testBitmapLarge, CACHE_KEY_1);
        Bitmap data = testPersister.loadDataFromCache(CACHE_KEY_1,
            Long.MAX_VALUE);
        assertNotNull(data);
        assertBitmapSizeEquals(data, LARGE_BITMAP_SIZE);
    }

    public void testLoadDataFromCacheExpired() throws Exception {
        testPersister
            .saveDataToCacheAndReturnData(testBitmapLarge, CACHE_KEY_1);
        Thread.sleep(ONE_MILLISECOND);
        try {
            testPersister.loadDataFromCache(CACHE_KEY_1, ONE_MILLISECOND);
            throw new Exception(EXPIRED_DATA_MSG);
        } catch (CacheLoadingException e) {
            // throwing this error is the expected behavior
        }
    }

    public void testLoadDataFromCacheNonDefaultDecoding() throws Exception {

        testPersister
            .saveDataToCacheAndReturnData(testBitmapSmall, CACHE_KEY_1);
        Bitmap data = testPersister.loadDataFromCache(CACHE_KEY_1,
            DurationInMillis.ALWAYS);
        assertNotNull(data);
        assertBitmapSizeEquals(data, SMALL_BITMAP_SIZE);
    }

    public void testLoadDataFromCacheWithDecodingOptions() throws Exception {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 2;
        testPersister.setDecodingOptions(opts);

        testPersister
            .saveDataToCacheAndReturnData(testBitmapLarge, CACHE_KEY_1);
        Bitmap data = testPersister.loadDataFromCache(CACHE_KEY_1,
            DurationInMillis.ALWAYS);
        assertNotNull(data);
        assertBitmapSizeEquals(data, LARGE_BITMAP_SIZE / 4);
    }

}
