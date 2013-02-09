package com.octo.android.robospice.persistence.memory;

import com.octo.android.robospice.persistence.ObjectPersister;

/**
 * {@link LruCacheObjectPersister} dedicated to {@link String}s.
 * @author David Stemmer
 * @author Mike Jancola
 */
public final class LruCacheStringObjectPersister extends LruCacheObjectPersister<String> {
    public LruCacheStringObjectPersister(int lruCacheSize) {
        super(String.class, new StringLruCache(lruCacheSize));
    }

    public LruCacheStringObjectPersister(ObjectPersister<String> decoratedPersister, int lruCacheSize) {
        super(decoratedPersister, new StringLruCache(lruCacheSize));
    }

}
