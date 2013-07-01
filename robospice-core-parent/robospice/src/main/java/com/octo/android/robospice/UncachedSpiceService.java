package com.octo.android.robospice;

import android.app.Application;

import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

/***
 * Concrete implementation of {@link SpiceService} with an empty CacheManager.
 * Using this class, requests will not be cached.
 * 
 * @author rciovati
 */
public class UncachedSpiceService extends SpiceService {

    @Override
    public CacheManager createCacheManager(Application application) {
        // Just return an empty CacheManager
        return new CacheManager() {
            @Override
            public <T> T saveDataToCacheAndReturnData(T data, Object cacheKey)
                throws CacheSavingException, CacheCreationException {

                return data;
            }
        };
    }
}
