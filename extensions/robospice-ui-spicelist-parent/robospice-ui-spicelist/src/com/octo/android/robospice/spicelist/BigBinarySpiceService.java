package com.octo.android.robospice.spicelist;

import android.app.Application;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.binary.InFileBigInputStreamObjectPersister;

public class BigBinarySpiceService extends SpiceService {

    @Override
    public CacheManager createCacheManager( Application application ) {
        CacheManager cacheManager = new CacheManager();

        InFileBigInputStreamObjectPersister inFileInputStreamObjectPersister = new InFileBigInputStreamObjectPersister( application );
        cacheManager.addPersister( inFileInputStreamObjectPersister );

        return cacheManager;
    }

    @Override
    public int getThreadCount() {
        return 3;
    }

}
