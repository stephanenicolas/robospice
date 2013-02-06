package com.octo.android.robospice.persistence.memory;

import android.app.Application;
import android.os.SystemClock;
import android.support.v4.util.LruCache;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author David Stemmer
 * @author Mike Jancola Abstract in-memory object persister, based on the
 *         Android LRUCache.
 */

public abstract class InMemoryLRUCacheObjectPersister<T> extends
    ObjectPersister<T> {
    private static final String CACHE_MISS_EXPIRED = "%s: found in cache but the data was stale.";
    private static final String CACHE_MISS_NOT_FOUND = "%s: not found in cache";

    private LruCache<Object, CacheItem<T>> memoryCache;
    private final ObjectPersister<T> fallbackPersister;

    protected InMemoryLRUCacheObjectPersister(Application application,
        Class<T> clazz) {
        this(application, clazz, null);
    }

    protected InMemoryLRUCacheObjectPersister(Application application,
        Class<T> clazz, ObjectPersister<T> fallback) {
        super(application, clazz);
        this.fallbackPersister = fallback;

    }

    protected LruCache<Object, CacheItem<T>> getMemoryCache() {
        if (memoryCache == null) {
            memoryCache = instantiateLRUCache();
        }
        return memoryCache;
    }

    public ObjectPersister<T> getFallbackPersister() {
        return fallbackPersister;
    }

    /**
     * Subclasses must implement this method to instantiate the LRU cache.
     * @return the instantiated cache object
     */

    protected abstract LruCache<Object, CacheItem<T>> instantiateLRUCache();

    /**
     * The CacheItem class represents a cached object, consisting of a piece of
     * immutable data and a timestamp marking when the data was added to the
     * cache.
     * @param <T>
     *            the type of object that will be stored in the cache
     */

    protected static class CacheItem<T> {
        private final long created;
        private final T data;

        public CacheItem(long created, T data) {
            this.created = created;
            this.data = data;
        }

        public long getCreated() {
            return created;
        }

        public T getData() {
            return data;
        }
    }

    /**
     * @param cacheKey
     *            the cacheKey of the data to load.
     * @param maxTimeInCacheBeforeExpiry
     *            max time, in milliseconds, that the data should remain in the
     *            cache
     * @return the cached data
     * @throws CacheLoadingException
     *             when the cache data is expired or not found
     */

    @Override
    public T loadDataFromCache(Object cacheKey, long maxTimeInCacheBeforeExpiry)
        throws CacheLoadingException {
        String keyString = cacheKey.toString();
        CacheItem<T> cacheItem = getMemoryCache().get(keyString);

        T dataToReturn =  null;

        /*
         * Since this is an in-memory cache and will be dumped when the device
         * reboots, the timestamp is retrieved via the {@link
         * android.os.SystemClock#elapsedRealtime()} method. This method counts
         * the time since boot and is safer than {@link
         * System#currentTimeMillis()} which can be dependant on user
         * configuration.
         */

        if (cacheItem != null) {
            boolean dataDoesExpire = maxTimeInCacheBeforeExpiry != DurationInMillis.ALWAYS;
            boolean dataIsStale = SystemClock.elapsedRealtime() - cacheItem.created > maxTimeInCacheBeforeExpiry;
            if (dataDoesExpire && dataIsStale) {
                String errorMsg = String.format(CACHE_MISS_EXPIRED, cacheKey);
                throw new CacheLoadingException( errorMsg );
            } else {
                dataToReturn = cacheItem.data;
            }
        } else if (fallbackPersister != null ){
            dataToReturn = fallbackPersister.loadDataFromCache( cacheKey,
                                                        maxTimeInCacheBeforeExpiry );
        }

        boolean dataIsMissing = dataToReturn == null;
        if (dataIsMissing)  {
            String errorMsg = String.format( CACHE_MISS_NOT_FOUND, cacheKey );
            throw new CacheLoadingException(errorMsg);
        }

        return dataToReturn;
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
    public T saveDataToCacheAndReturnData(T data, Object cacheKey)
        throws CacheSavingException {
        CacheItem<T> itemToCache = new CacheItem<T>(
            SystemClock.elapsedRealtime(), data);
        getMemoryCache().put(cacheKey, itemToCache);

        if (fallbackPersister != null) {
            fallbackPersister.saveDataToCacheAndReturnData(data, cacheKey);
        }

        return data;
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
