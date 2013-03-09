package com.octo.android.robospice.stub;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

/**
 * Stub of an object persister. Doesn't use any real cache management. Will help to pass the tests without any side-effect.
 * @author sni
 */
public class IntegerPersisterStub extends ObjectPersister<Integer> {

    private static final Integer STUB_DATA = 2;

    public IntegerPersisterStub(Application application) {
        super(application, Integer.class);
    }

    @Override
    public Integer loadDataFromCache(Object cacheKey, long maxTimeInCache) throws CacheLoadingException {
        if (maxTimeInCache == DurationInMillis.ALWAYS_EXPIRED || maxTimeInCache > DurationInMillis.ONE_MINUTE) {
            return null;
        }
        return STUB_DATA;
    }

    @Override
    public List<Integer> loadAllDataFromCache() throws CacheLoadingException {
        ArrayList<Integer> listData = new ArrayList<Integer>();
        listData.add(STUB_DATA);
        return listData;

    }

    @Override
    public List<Object> getAllCacheKeys() {
        return new ArrayList<Object>();
    }

    @Override
    public Integer saveDataToCacheAndReturnData(Integer data, Object cacheKey) throws CacheSavingException {
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
        return 0;
    }

}
