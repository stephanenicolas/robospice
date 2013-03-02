package com.octo.android.robospice.persistence.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roboguice.util.temp.Ln;
import android.support.v4.util.LruCache;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

/**
 * Abstract in-memory object persister, based on the Android LRUCache.
 * @author David Stemmer
 * @author Mike Jancola
 */
public class LruCacheObjectPersister<T> extends ObjectPersister<T> {
    private LruCache<Object, CacheItem<T>> lruCache;
    private ObjectPersister<T> decoratedPersister;

    public LruCacheObjectPersister(Class<T> clazz, LruCache<Object, CacheItem<T>> lruCache) {
        super(null, clazz);
        this.lruCache = lruCache;
    }

    public LruCacheObjectPersister(ObjectPersister<T> decoratedPersister, LruCache<Object, CacheItem<T>> lruCache) {
        super(decoratedPersister.getApplication(), decoratedPersister.getHandledClass());
        this.decoratedPersister = decoratedPersister;
        this.lruCache = lruCache;
    }

    public ObjectPersister<T> getDecoratedPersister() {
        return decoratedPersister;
    }

    public LruCache<Object, CacheItem<T>> getLruCache() {
        return lruCache;
    }

    @Override
    public T loadDataFromCache(Object cacheKey, long maxTimeInCacheBeforeExpiry) throws CacheLoadingException {
        CacheItem<T> cacheItem = lruCache.get(cacheKey);

        if (cacheItem == null) {
            Ln.d("Miss from lru cache for %s", cacheKey);
            if (decoratedPersister != null) {
                T data = decoratedPersister.loadDataFromCache(cacheKey, maxTimeInCacheBeforeExpiry);
                if (data == null) {
                    return null;
                }
                CacheItem<T> item = new CacheItem<T>(decoratedPersister.getCreationDateInCache(cacheKey), data);
                Ln.d("Put in lru cache after miss");
                lruCache.put(cacheKey, item);
                return data;
            }
            return null;
        } else {
            Ln.d("Hit from lru cache for %s", cacheKey);
            boolean dataCanExpire = maxTimeInCacheBeforeExpiry != DurationInMillis.ALWAYS_RETURNED;
            boolean dataIsNotExpired = System.currentTimeMillis() - cacheItem.getCreationDate() <= maxTimeInCacheBeforeExpiry;
            if (!dataCanExpire || dataIsNotExpired) {
                return cacheItem.getData();
            }
            return null;
        }
    }

    @Override
    public T saveDataToCacheAndReturnData(T data, Object cacheKey) throws CacheSavingException {
        CacheItem<T> itemToCache = new CacheItem<T>(data);
        lruCache.put(cacheKey, itemToCache);
        Ln.d("Put in lru cache for %s", cacheKey);

        if (decoratedPersister != null) {
            decoratedPersister.saveDataToCacheAndReturnData(data, cacheKey);
        }

        return data;
    }

    @Override
    public long getCreationDateInCache(Object cacheKey) throws CacheLoadingException {
        CacheItem<T> cacheItem = lruCache.get(cacheKey);

        if (cacheItem != null) {
            return cacheItem.getCreationDate();
        }
        throw new CacheLoadingException("Data could not be found in cache for cacheKey=" + cacheKey);
    }

    @Override
    public List<T> loadAllDataFromCache() throws CacheLoadingException {
        if (decoratedPersister != null) {
            return decoratedPersister.loadAllDataFromCache();
        } else {
            Map<Object, CacheItem<T>> cacheMap = lruCache.snapshot();
            List<T> allData = new ArrayList<T>();
            for (CacheItem<T> item : cacheMap.values()) {
                allData.add(item.getData());
            }
            return allData;
        }
    }

    @Override
    public List<Object> getAllCacheKeys() {
        if (decoratedPersister != null) {
            return decoratedPersister.getAllCacheKeys();
        } else {
            return new ArrayList<Object>(lruCache.snapshot().keySet());
        }
    }

    @Override
    public boolean removeDataFromCache(Object cacheKey) {
        boolean result = false;
        if (decoratedPersister != null) {
            result = decoratedPersister.removeDataFromCache(cacheKey);
        }
        return result || lruCache.remove(cacheKey) != null;
    }

    @Override
    public void removeAllDataFromCache() {
        lruCache.evictAll();
        if (decoratedPersister != null) {
            decoratedPersister.removeAllDataFromCache();
        }
    }
}
