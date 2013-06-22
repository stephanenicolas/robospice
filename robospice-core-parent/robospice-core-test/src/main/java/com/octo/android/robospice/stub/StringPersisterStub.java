package com.octo.android.robospice.stub;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

/**
 * Stub of an object persister. Doesn't use any real cache management. Will help
 * to pass the tests without any side-effect.
 * @author sni
 */
public class StringPersisterStub extends ObjectPersister<String> {

    public StringPersisterStub(Application application) {
        super(application, String.class);
    }

    @Override
    public String loadDataFromCache(Object cacheKey, long maxTimeInCache) throws CacheLoadingException {
        return null;
    }

    @Override
    public List<String> loadAllDataFromCache() throws CacheLoadingException {
        return new ArrayList<String>();
    }

    @Override
    public List<Object> getAllCacheKeys() {
        return new ArrayList<Object>();
    }

    @Override
    public String saveDataToCacheAndReturnData(String data, Object cacheKey) throws CacheSavingException {
        return data;
    }

    @Override
    public boolean removeDataFromCache(Object cacheKey) {
        return true;
    }

    @Override
    public void removeAllDataFromCache() {
    }

    @Override
    public long getCreationDateInCache(Object cacheKey) throws CacheLoadingException {
        throw new CacheLoadingException("This persisters stores no data.");
    }

    @Override
    public boolean isDataInCache(Object cacheKey, long maxTimeInCache) {
        return false;
    }

}
