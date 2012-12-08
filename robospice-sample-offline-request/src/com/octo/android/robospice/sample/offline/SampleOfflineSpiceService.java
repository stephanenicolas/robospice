package com.octo.android.robospice.sample.offline;

import android.app.Application;
import android.content.Context;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;

public class SampleOfflineSpiceService extends SpiceService {

    @Override
    public CacheManager createCacheManager( Application application ) {
        CacheManager cacheManager = new CacheManager();

        // init

        return cacheManager;
    }

    @Override
    protected NetworkStateChecker getNetworkStateChecker() {
        return new NetworkStateChecker() {

            @Override
            public boolean isNetworkAvailable( Context context ) {
                return true;
            }
        };
    }

}
