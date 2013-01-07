package com.octo.android.robospice.core.test;

import android.app.Application;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;

public class InvalidSpiceTestService extends SpiceService {

    @Override
    public CacheManager createCacheManager(Application application) {
        return null;
    }

}
