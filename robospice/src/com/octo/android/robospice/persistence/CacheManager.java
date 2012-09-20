package com.octo.android.robospice.persistence;

import java.util.ArrayList;
import java.util.Collection;

import com.octo.android.robospice.exception.CacheLoadingException;
import com.octo.android.robospice.exception.CacheSavingException;

/**
 * An entity responsible for loading/saving data from/to cache. It implements a Chain of Responsability pattern,
 * delegating loading and saving operations to {@link ObjectPersister} or {@link ObjectPersisterFactory} elements.
 * 
 * The chain of responsibility is ordered. This means that the order used to register elements matters. All elements in
 * the chain of responsibility are questioned in order. The first element that can handle a given class for persistence
 * will be used to persist data of this class.
 * 
 * @author sni
 * 
 */
public class CacheManager implements ICacheManager {

    /** The Chain of Responsability list of all {@link ObjectPersisterFactory}. */
    private Collection< ObjectPersisterFactory > factoryList = new ArrayList< ObjectPersisterFactory >();

    /** {@inheritDoc} */
    public void addObjectPersisterFactory( ObjectPersisterFactory factory ) {
        factoryList.add( factory );
    }

    /** {@inheritDoc} */
    public void removeObjectPersisterFactory( ObjectPersisterFactory factory ) {
        factoryList.remove( factory );
    }

    /** {@inheritDoc} */
    public < T > T loadDataFromCache( Class< T > clazz, Object cacheKey, long maxTimeInCacheBeforeExpiry ) throws CacheLoadingException {
        return getClassCacheManager( clazz ).loadDataFromCache( cacheKey, maxTimeInCacheBeforeExpiry );
    }

    @SuppressWarnings("unchecked")
    /** {@inheritDoc}*/
    public < T > T saveDataToCacheAndReturnData( T data, Object cacheKey ) throws CacheSavingException {
        ObjectPersister< T > classCacheManager = (ObjectPersister< T >) getClassCacheManager( data.getClass() );
        return classCacheManager.saveDataToCacheAndReturnData( data, cacheKey );
    }

    /** {@inheritDoc} */
    public boolean removeDataFromCache( Class< ? > clazz, Object cacheKey ) {
        return getClassCacheManager( clazz ).removeDataFromCache( cacheKey );
    }

    /** {@inheritDoc} */
    public void removeAllDataFromCache( Class< ? > clazz ) {
        getClassCacheManager( clazz ).removeAllDataFromCache();
    }

    /** {@inheritDoc} */
    public void removeAllDataFromCache() {
        for ( ObjectPersisterFactory factory : this.factoryList ) {
            factory.removeAllDataFromCache();
        }
    }

    /** {@inheritDoc} */
    protected < T > ObjectPersister< T > getClassCacheManager( Class< T > clazz ) {
        for ( ObjectPersisterFactory factory : this.factoryList ) {
            if ( factory.canHandleClass( clazz ) ) {
                return factory.createClassCacheManager( clazz );
            }
        }
        throw new IllegalArgumentException( "Class " + clazz.getName() + " is not handled by any registered factoryList" );
    }
}
