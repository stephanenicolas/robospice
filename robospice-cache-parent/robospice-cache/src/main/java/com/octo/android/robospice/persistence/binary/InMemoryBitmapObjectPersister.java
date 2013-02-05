package com.octo.android.robospice.persistence.binary;

import android.support.v4.util.LruCache;
import android.graphics.Bitmap;
import android.content.Context;
import android.app.Application;
import android.app.ActivityManager;

/**
 * Concrete implementation of {@link InMemoryLRUCacheObjectPersister} for bitmap
 * objects. By default, it creates an LRU cache that can fill up to 1/4 of
 * application memory. This value can be changed by passing a different cache
 * size in the constructor method.
 */

public class InMemoryBitmapObjectPersister extends
    InMemoryLRUCacheObjectPersister<Bitmap> {

    private final int cacheSize;

    private static final int DEFAULT_CACHE_SIZE = 1024 * 1024 * 4;
    private static final int BASELINE_MEMCLASS = 16;

    /**
     * Convenience constructor with the default cache size.
     * @param application
     *            the Android application object
     */

    public InMemoryBitmapObjectPersister(Application application) {
        this(application, -1);
    }

    /**
     * Default constructor method.
     * @param application
     *            the Android application object
     * @param cacheSize
     *            the requested cache size, in bytes. If the size is less than
     *            one, the cache size is limited to 1/4 of the application
     *            memory.
     */

    public InMemoryBitmapObjectPersister(Application application, int cacheSize) {
        super(application, Bitmap.class);

        // base Android memory class is 16 MB per process
        // the cache should take up no more than 1/4 of the available app memory
        if (cacheSize > 0) {
            this.cacheSize = cacheSize;
        } else {
            int memClass = ((ActivityManager) application
                .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
            this.cacheSize = DEFAULT_CACHE_SIZE
                * (memClass / BASELINE_MEMCLASS);
        }
    }

    /**
     * Creates the LRUCache, calculating the size of the object based on the
     * number of bytes in the bitmap.
     * @return the instantiated cache
     */

    @Override
    protected LruCache<Object, CacheItem<Bitmap>> instantiateLRUCache() {
        return new LruCache<Object, CacheItem<Bitmap>>(cacheSize) {

            @Override
            protected int sizeOf(Object key, CacheItem<Bitmap> value) {
                Bitmap data = value.getData();
                return data.getRowBytes() * data.getHeight();
            }

            @Override
            protected void entryRemoved(boolean evicted, Object key,
                CacheItem<Bitmap> oldValue, CacheItem<Bitmap> newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
    }

}
