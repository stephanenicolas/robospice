package com.octo.android.robospice.stub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import roboguice.util.temp.Ln;
import android.app.Application;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

/**
 * Stub of an object persister. This one stores double in hashmap to implement
 * caching.
 * @author sni
 */
public class DoubleInMemoryPersisterStub extends ObjectPersister<Double> {

    private HashMap<Object, Double> map = new HashMap<Object, Double>();

    public DoubleInMemoryPersisterStub(Application application) {
        super(application, Double.class);
        Ln.d("New memory cache created");
        setAsyncSaveEnabled(false);
    }

    @Override
    public Double loadDataFromCache(Object cacheKey, long maxTimeInCache) throws CacheLoadingException {
        if (maxTimeInCache == DurationInMillis.ALWAYS_EXPIRED || maxTimeInCache > DurationInMillis.ONE_MINUTE) {
            return null;
        }
        Ln.d("Value in cache for " + cacheKey + " is " + map.get(cacheKey));
        return map.get(cacheKey);
    }

    @Override
    public List<Double> loadAllDataFromCache() throws CacheLoadingException {
        return new ArrayList<Double>(map.values());

    }

    @Override
    public List<Object> getAllCacheKeys() {
        return new ArrayList<Object>(map.keySet());
    }

    @Override
    public Double saveDataToCacheAndReturnData(Double data, Object cacheKey) throws CacheSavingException {
        Ln.d("Adding " + data + " to cache at " + cacheKey);
        map.put(cacheKey, data);
        return data;
    }

    @Override
    public boolean removeDataFromCache(Object cacheKey) {
        Ln.d("Clearing cache at" + cacheKey);
        map.remove(cacheKey);
        return true;
    }

    @Override
    public void removeAllDataFromCache() {
        Ln.d("Clearing all cache");
        map.clear();
    }

    @Override
    public long getCreationDateInCache(Object cacheKey) throws CacheLoadingException {
        return 0;
    }

    @Override
    public boolean isDataInCache(Object cacheKey, long maxTimeInCache) {
        return maxTimeInCache != DurationInMillis.ALWAYS_EXPIRED && maxTimeInCache < DurationInMillis.ONE_MINUTE;
    }

}
