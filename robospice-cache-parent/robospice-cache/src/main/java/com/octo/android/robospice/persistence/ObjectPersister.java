package com.octo.android.robospice.persistence;

import java.util.List;

import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

/**
 * Super class of all entities responsible for loading/saving objects of a given
 * class in the cache.
 * @author sni
 * @param <T>
 *            the class of the objects this {@link ObjectPersister} can
 *            persist/unpersist.
 */
public abstract class ObjectPersister<T> implements Persister, CacheCleaner {

    private boolean isAsyncSaveEnabled;
    private Application application;
    private Class<T> clazz;

    public ObjectPersister(Application application, Class<T> clazz) {
        this.application = application;
        this.clazz = clazz;
    }

    public Application getApplication() {
        return application;
    }

    public Class<T> getHandledClass() {
        return clazz;
    }

    @Override
    public boolean canHandleClass(Class<?> clazz) {
        return clazz.equals(this.clazz);
    }

    /**
     * Load data from cache if not expired.
     * @param cacheKey
     *            the cacheKey of the data to load.
     * @param maxTimeInCache
     *            the maximum time the data can have been stored in cached
     *            before being considered expired. 0 means infinite.
     * @return the data if it could be loaded.
     * @throws CacheLoadingException
     *             if the data in cache is expired.
     */
    public abstract T loadDataFromCache(Object cacheKey, long maxTimeInCache) throws CacheLoadingException;

    public abstract List<T> loadAllDataFromCache() throws CacheLoadingException;

    public abstract List<Object> getAllCacheKeys();

    public abstract T saveDataToCacheAndReturnData(T data, Object cacheKey) throws CacheSavingException;

    public abstract boolean removeDataFromCache(Object cacheKey);

    @Override
    public abstract void removeAllDataFromCache();

    /**
     * Return the creation date of creation of cache. entry for a given
     * cacheKey.
     * @param cacheKey
     *            the cachekey identifying the object to look for.
     * @return a long corresponding to the creation date of creation of cache.
     * @throws CacheLoadingException
     *             if there is no such element in cache.
     */
    public abstract long getCreationDateInCache(Object cacheKey) throws CacheLoadingException;

    public boolean isAsyncSaveEnabled() {
        return isAsyncSaveEnabled;
    }

    public void setAsyncSaveEnabled(boolean isAsyncSaveEnabled) {
        this.isAsyncSaveEnabled = isAsyncSaveEnabled;
    }

    public abstract boolean isDataInCache(Object cacheKey, long maxTimeInCacheBeforeExpiry);

}
