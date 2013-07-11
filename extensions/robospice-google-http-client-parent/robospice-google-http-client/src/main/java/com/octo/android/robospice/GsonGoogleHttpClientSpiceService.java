package com.octo.android.robospice;

import android.app.Application;

import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.googlehttpclient.json.GsonObjectPersisterFactory;

/**
 * A {@link GoogleHttpClientSpiceService} dedicated to json web services via
 * gson. Provides caching.
 * @author sni
 */
public class GsonGoogleHttpClientSpiceService extends GoogleHttpClientSpiceService {

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister(new GsonObjectPersisterFactory(application));
        return cacheManager;
    }

}
