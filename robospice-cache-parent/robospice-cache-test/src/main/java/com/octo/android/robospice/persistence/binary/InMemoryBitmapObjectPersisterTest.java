package com.octo.android.robospice.persistence.binary;

import android.test.InstrumentationTestCase;
import android.support.v4.util.LruCache;
import android.graphics.Bitmap;
import android.app.Application;

public class InMemoryBitmapObjectPersisterTest extends InstrumentationTestCase {

    private static final int BITMAP_HEIGHT = 10;
    private static final int BITMAP_WIDTH = 10;
    private static final int BYTES_PER_PIXEL = 4;
    private static final Bitmap testBitmap = Bitmap.createBitmap(BITMAP_HEIGHT,
        BITMAP_WIDTH, Bitmap.Config.ARGB_8888);

    private static final String CACHE_KEY_1 = "cacheKey1";

    private static final int BITMAP_SIZE = BITMAP_HEIGHT * BITMAP_WIDTH
        * BYTES_PER_PIXEL;

    // The cache is large enough to hold no more than 1 bitmap
    private static final int CACHE_SIZE = BITMAP_SIZE * 3 / 2;

    private TestBitmapPersister testPersister;

    private class TestBitmapPersister extends InMemoryBitmapObjectPersister {

        private TestBitmapPersister(Application application, int cacheSize) {
            super(application, cacheSize);
        }

        // increase visibility
        @Override
        public LruCache<Object, CacheItem<Bitmap>> getMemoryCache() {
            return super.getMemoryCache();
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Application application = (Application) getInstrumentation()
            .getTargetContext().getApplicationContext();

        testPersister = new TestBitmapPersister(application, CACHE_SIZE);

    }

    public void testCacheSizeCalculation() throws Exception {
        testPersister.saveDataToCacheAndReturnData(testBitmap, CACHE_KEY_1);
        assertEquals(testPersister.getMemoryCache().size(), BITMAP_SIZE);
    }

}
