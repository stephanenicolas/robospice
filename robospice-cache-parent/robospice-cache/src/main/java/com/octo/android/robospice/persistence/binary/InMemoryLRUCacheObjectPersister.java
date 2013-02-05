package com.octo.android.robospice.persistence.binary;

import android.app.Application;
import android.os.SystemClock;
import android.support.v4.util.LruCache;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author David Stemmer
 * @author Mike Jancola
 *
 * Abstract in-memory object persister, based on the Android LRUCache.
 *
 */

public abstract class InMemoryLRUCacheObjectPersister<T> extends
    ObjectPersister<T> {
    private static final String CACHE_MISS_EXPIRED = "%s: found in cache but the data was stale.";
    private static final String CACHE_MISS_NOT_FOUND = "%s: not found in cache";

    private LruCache<Object, CacheItem<T>> memoryCache;

    protected InMemoryLRUCacheObjectPersister(Application application,
        Class<T> clazz) {
        super(application, clazz);
    }

    private LruCache<Object, CacheItem<T>> getMemoryCache() {
        if (memoryCache == null) {
            memoryCache = instantiateLRUCache();
        }
        return memoryCache;
    }

    /**
     * Subclasses must implement this method to instantiate the LRU cache.
     *
     * @return the instantiated cache object
     */

    protected abstract LruCache<Object, CacheItem<T>> instantiateLRUCache();

    /**
     * The CacheItem class represents a cached object, consisting of a piece of
     * immutable data and a timestamp marking when the data was added to the
     * cache.
     *
     * @param <T> the type of object that will be stored in the cache
     */

    protected static class CacheItem<T> {
        public final long created;
        public final T data;

        public CacheItem(long created, T data) {
            this.created = created;
            this.data = data;
        }
    }

    /**
     *
     * @param cacheKey the cacheKey of the data to load.
     * @param maxTimeInCacheBeforeExpiry max time, in milliseconds, that the data should remain in the cache
     * @return the cached data
     * @throws CacheLoadingException when the cache data is null
     */

    @Override
    public T loadDataFromCache(Object cacheKey, long maxTimeInCacheBeforeExpiry)
        throws CacheLoadingException {
        String keyString = cacheKey.toString();
        CacheItem<T> cacheItem = getMemoryCache().get(keyString);
        String errorMsg = String.format(CACHE_MISS_NOT_FOUND, cacheKey);

        /*
        Since this is an in-memory cache and will be dumped when the
        device reboots, the timestamp is retrieved via the
        {@link android.os.SystemClock#elapsedRealtime()} method. This method
        counts the time since boot and is safer than
        {@link System#currentTimeMillis()} which can be dependant on user
        configuration.
        */

        if (cacheItem != null) {
            long timeInCache = SystemClock.elapsedRealtime()
                - cacheItem.created;
            if (timeInCache > maxTimeInCacheBeforeExpiry) {
                errorMsg = String.format(CACHE_MISS_EXPIRED, cacheKey);
            } else {
                return cacheItem.data;
            }
        }

        throw new CacheLoadingException(errorMsg);
    }

    @Override
    public List<T> loadAllDataFromCache() throws CacheLoadingException {
        Map<Object, CacheItem<T>> cacheMap = getMemoryCache().snapshot();
        ArrayList<T> allData = new ArrayList<T>();
        for (CacheItem<T> item : cacheMap.values()) {
            allData.add(item.data);
        }

        return allData;
    }

    @Override
    public List<Object> getAllCacheKeys() {
        return new ArrayList<Object>(getMemoryCache().snapshot().keySet());
    }

    @Override
    public T saveDataToCacheAndReturnData(T bitmap, Object cacheKey)
        throws CacheSavingException {
        CacheItem<T> itemToCache = new CacheItem<T>(
            SystemClock.elapsedRealtime(), bitmap);
        getMemoryCache().put( cacheKey, itemToCache);

        return bitmap;
    }

    @Override
    public boolean removeDataFromCache(Object o) {
        return (getMemoryCache().remove(o) != null);
    }

    @Override
    public void removeAllDataFromCache() {
        getMemoryCache().evictAll();
    }

}
