package com.octo.android.robospice.persistence.memory;

import android.graphics.Bitmap;

import com.octo.android.robospice.persistence.ObjectPersister;

/**
 * {@link LruCacheObjectPersister} dedicated to {@link String}s.
 * @author David Stemmer
 * @author Mike Jancola
 */
public class LruCacheBitmapObjectPersister extends LruCacheObjectPersister<Bitmap> {

    public LruCacheBitmapObjectPersister(int lruCacheSize) {
        super(Bitmap.class, new BitmapLruCache(lruCacheSize));
    }

    public LruCacheBitmapObjectPersister(ObjectPersister<Bitmap> decoratedPersister, int lruCacheSize) {
        super(decoratedPersister, new BitmapLruCache(lruCacheSize));
    }
}
