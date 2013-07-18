package com.octo.android.robospice.persistence;

import java.util.Date;
import java.util.List;

import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

/**
 * This interface is mainly used for mocking/testing. Developpers should use
 * directly the class {@link CacheManager} and should not have to implement this
 * interface. Defines the behavior of a cache manager, a bus of
 * {@link ObjectPersister}.
 * @author sni
 * @deprecated since version 1.4.6 of RS, easymock 3.2 makes this interface
 *             obsolete.
 */
@Deprecated
public interface ICacheManager {

    void addPersister(Persister persister);

    void removePersister(Persister persister);

    /**
     * Get all cache keys associated to a given class.
     * @param clazz
     *            the class for which to get all cache keys.
     * @return all cache keys associated to a given class. The empty list is
     *         nothing is found in cache.
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
     */
    <T> T loadDataFromCache(Class<T> clazz, Object cacheKey, long maxTimeInCacheBeforeExpiry) throws CacheLoadingException, CacheCreationException;

    /**
     * Loads all data stored in cache for a given class.
     * @param clazz
     *            the class for which to get all data stored in cache.
     * @return all data stored in cache for a given class.
     */
    <T> List<T> loadAllDataFromCache(Class<T> clazz) throws CacheLoadingException, CacheCreationException;

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
     */
    <T> T saveDataToCacheAndReturnData(T data, Object cacheKey) throws CacheCreationException, CacheSavingException;

    /**
     * Test whether or not some data is in cache.
     * @param clazz
     *            the class of the object that is supposed to be stored in
     *            cache.
     * @param cacheKey
     *            the key used to identify this item in cache.
     * @param maxTimeInCacheBeforeExpiry
     *            the maximum time (in ms) an item can be stored in cache before
     *            being considered expired.
     * @return a boolean indicating whether or not the given data is in the
     *         cache.
     * @throws CacheCreationException
     */
    boolean isDataInCache(Class<?> clazz, Object cacheKey, long maxTimeInCacheBeforeExpiry) throws CacheCreationException;

    /**
     * The date at which given data has been stored last in cache.
     * @param clazz
     *            the class of the object that is supposed to be stored in
     *            cache.
     * @param cacheKey
     *            the key used to identify this item in cache.
     * @return the date at which data has been stored last in cache. Null if no
     *         such data exists.
     * @throws CacheCreationException
     * @throws CacheLoadingException
     */
    Date getDateOfDataInCache(Class<?> clazz, Object cacheKey) throws CacheCreationException, CacheLoadingException;

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
