package com.octo.android.robospice.persistence.memory;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * {@link LruCache} for {@link String}.
 * @author David Stemmer
 * @author Mike Jancola
 * @author SNI
 */
public class StringLruCache extends LruCache<Object, CacheItem<String>> {
    public StringLruCache(int maxSize) {
        super(maxSize);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    protected int sizeOf(Object key, CacheItem<String> value) {
        String data = value.getData();
        return data.length();
    }
}
