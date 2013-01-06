package com.octo.android.robospice;

import android.app.Application;

import com.octo.android.robospice.persistence.CacheManager;

/***
 * Concrete implementation of {@link SpiceService} with an empty CacheManager.
 * Using this class you requests are not cached.
 * @author rciovati
 */
public class UncachedSpiceService extends SpiceService {

    @Override
    public CacheManager createCacheManager(Application application) {
        // Just return an empty CacheManager
        return new CacheManager();
    }

}
