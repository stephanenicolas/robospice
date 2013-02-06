package com.octo.android.robospice.persistence.binary;

import android.app.Application;
import android.graphics.Bitmap;
import android.test.InstrumentationTestCase;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;

public class InFileBitmapObjectPersisterTest extends InstrumentationTestCase {

    private static final int BITMAP_HEIGHT = 10;
    private static final int BITMAP_WIDTH = 10;
    private static final int BYTES_PER_PIXEL = 4;
    private static final Bitmap testBitmap = Bitmap.createBitmap(BITMAP_WIDTH,
        BITMAP_HEIGHT, Bitmap.Config.ARGB_8888);

    private static final String CACHE_KEY_1 = "cacheKey1";

    private static final int BITMAP_SIZE = BITMAP_HEIGHT * BITMAP_WIDTH
        * BYTES_PER_PIXEL;

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
        assertNotNull(testPersister.saveDataToCacheAndReturnData(testBitmap,
            CACHE_KEY_1));
        assertTrue(testPersister.getCacheFile(CACHE_KEY_1).exists());
    }

    public void testLoadDataFromCacheNoExpiration() throws Exception {
        testPersister.saveDataToCacheAndReturnData(testBitmap, CACHE_KEY_1);
        Bitmap data = testPersister.loadDataFromCache(CACHE_KEY_1,
            DurationInMillis.ALWAYS);
        assertNotNull(data);
        assertBitmapSizeEquals(data, BITMAP_SIZE);

    }

    public void testLoadDataFromCacheNotExpired() throws Exception {
        testPersister.saveDataToCacheAndReturnData(testBitmap, CACHE_KEY_1);
        Bitmap data = testPersister.loadDataFromCache(CACHE_KEY_1,
            Long.MAX_VALUE);
        assertNotNull(data);
        assertBitmapSizeEquals(data, BITMAP_SIZE);
    }

    public void testLoadDataFromCacheExpired() throws Exception {
        testPersister.saveDataToCacheAndReturnData(testBitmap, CACHE_KEY_1);
        Thread.sleep(ONE_MILLISECOND);
        try {
            testPersister.loadDataFromCache(CACHE_KEY_1, ONE_MILLISECOND);
            throw new Exception(EXPIRED_DATA_MSG);
        } catch (CacheLoadingException e) {
            // throwing this error is the expected behavior
        }
    }

}
