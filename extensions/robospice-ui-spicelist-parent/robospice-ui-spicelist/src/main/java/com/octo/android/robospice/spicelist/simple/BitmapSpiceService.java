package com.octo.android.robospice.spicelist.simple;

import android.app.Application;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.binary.InFileBitmapObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

public class BitmapSpiceService extends SpiceService {

    private static final int DEFAULT_NUM_THREADS = 3;

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        InFileBitmapObjectPersister inFileBitmapObjectPersister = new InFileBitmapObjectPersister(application);
        cacheManager.addPersister(inFileBitmapObjectPersister);

        return cacheManager;
    }

    @Override
    public int getThreadCount() {
        return DEFAULT_NUM_THREADS;
    }

}
