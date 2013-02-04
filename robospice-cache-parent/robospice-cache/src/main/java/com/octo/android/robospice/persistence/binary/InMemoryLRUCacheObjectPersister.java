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
 * Persister for in-memory cache of Bitmaps
 * used by RoboSpice to automagically cache / fetch images
 * Stores data as bitmaps
 */

public abstract class InMemoryLRUCacheObjectPersister<T> extends ObjectPersister<T>
{
    private static final String CACHE_MISS_EXPIRED   =
        "%s: found in cache but the data was stale.";
    private static final String CACHE_MISS_NOT_FOUND =
        "%s: not found in cache";

    private LruCache<String, CacheItem<T>> memoryCache;

    protected InMemoryLRUCacheObjectPersister( Application application,
                                               Class<T> clazz )
    {
        super( application, clazz );
    }

    private LruCache<String, CacheItem<T>> getMemoryCache()
    {
        if ( memoryCache == null )
        {
            memoryCache = instantiateLRUCache();
        }
        return memoryCache;
    }

    protected abstract LruCache<String, CacheItem<T>> instantiateLRUCache();

    protected static class CacheItem<T>
    {
        public final long created;
        public final T    data;

        public CacheItem( long created, T data )
        {
            this.created = created;
            this.data = data;
        }
    }

    @Override
    public T loadDataFromCache( Object cacheKey,
                                     long maxTimeInCacheBeforeExpiry ) throws
                                                                       CacheLoadingException
    {
        String keyString = (String) cacheKey;
        CacheItem<T> cacheItem = getMemoryCache().get( keyString );
        String errorMsg = String.format( CACHE_MISS_NOT_FOUND, cacheKey );

        if ( cacheItem != null )
        {
            long timeInCache = SystemClock.elapsedRealtime() - cacheItem.created;
            if (timeInCache > maxTimeInCacheBeforeExpiry)
            {
                errorMsg = String.format( CACHE_MISS_EXPIRED, cacheKey );
            }
            else
            {
                return cacheItem.data;
            }
        }

        throw new CacheLoadingException(errorMsg);
    }

    @Override
    public List<T> loadAllDataFromCache() throws CacheLoadingException
    {
        Map<String, CacheItem<T>> cacheMap = getMemoryCache().snapshot();
        ArrayList<T> allData = new ArrayList<T>();
        for (CacheItem<T> item : cacheMap.values()) {
            allData.add( item.data ) ;
        }

        return allData;
    }

    @Override
    public List<Object> getAllCacheKeys()
    {
        return new ArrayList<Object>( getMemoryCache().snapshot().keySet() );
    }

    @Override
    public T saveDataToCacheAndReturnData( T bitmap, Object cacheKey ) throws
                                                                          CacheSavingException
    {
        CacheItem<T> itemToCache = new CacheItem<T>(SystemClock.elapsedRealtime(),
                                                 bitmap);
        getMemoryCache().put( (String) cacheKey, itemToCache );

        return bitmap;
    }

    @Override
    public boolean removeDataFromCache( Object o )
    {
        return (getMemoryCache().remove( (String) o ) != null);
    }

    @Override
    public void removeAllDataFromCache()
    {
        getMemoryCache().evictAll();
    }

}
