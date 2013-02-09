package com.octo.android.robospice.persistence;

import java.util.List;

import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

/**
 * This interface is mainly used for mocking/testing. Developpers should use
 * directly the class {@link CacheManager} and should not have to implement this
 * interface. Defines the behavior of a cache manager, a bus of
 * {@link ObjectPersister}.
 * @author sni
 */
public interface ICacheManager {

    void addPersister(Persister persister);

    void removePersister(Persister persister);

    /**
     * Get all cache keys associated to a given class.
     * @param clazz
     *            the class for which to get all cache keys.
     * @return all cache keys associated to a given class.
     */
    <T> List<Object> getAllCacheKeys(Class<T> clazz);

    /**
     * Loads an instance of a class clazz, that is stored in cache under the key
     * cacheKey.
     * @param clazz
     *            the class of the object that is supposed to be stored in
     *            cache.
     * @param cacheKey
     *            the key used to identify this item in cache.
     * @param maxTimeInCacheBeforeExpiry
     *            the maximum time (in ms) an item can be stored in cache before
     *            being considered expired.
     * @return an instance of a class clazz, that is stored in cache under the
     *         key cacheKey. If the item is not found in cache or is older than
     *         maxTimeInCacheBeforeExpiry, then this method will return null.
     * @throws CacheLoadingException
     *             if an error occurs during cache reading, or instance
     *             creation.
     */
    <T> T loadDataFromCache(Class<T> clazz, Object cacheKey, long maxTimeInCacheBeforeExpiry) throws CacheLoadingException;

    /**
     * Loads all data stored in cache for a given class.
     * @param clazz
     *            the class for which to get all data stored in cache.
     * @return all data stored in cache for a given class.
     */
    <T> List<T> loadAllDataFromCache(Class<T> clazz) throws CacheLoadingException;

    /**
     * Save an instance of a given class, into the cache identified by cacheKey.
     * Some {@link ObjectPersister} can modify the data they receive before
     * saving it. Most {@link ObjectPersister} instances will just save the data
     * as-is, in this case, they can even return it and save it asynchronously
     * in a background thread for a better efficiency.
     * @param data
     *            the data to be saved in cache.
     * @param cacheKey
     *            the key used to identify this item in cache.
     * @return the data that was saved.
     * @throws CacheSavingException
     *             if an error occurs during cache writing.
     */
    <T> T saveDataToCacheAndReturnData(T data, Object cacheKey) throws CacheSavingException;

    /**
     * Removes a given data in the cache that is an instance of class clazz.
     * @param clazz
     *            the class of the data to be removed.
     * @param cacheKey
     *            the identifier of the data to be removed from cache.
     * @return a boolean indicating whether or not this data could be removed.
     */
    boolean removeDataFromCache(Class<?> clazz, Object cacheKey);

    /**
     * Removes all data in the cache that are instances of class clazz.
     * @param clazz
     *            the class of the data to be removed.
     */
    void removeAllDataFromCache(Class<?> clazz);

    /**
     * Removes all data in the cache.
     */
    void removeAllDataFromCache();

}
