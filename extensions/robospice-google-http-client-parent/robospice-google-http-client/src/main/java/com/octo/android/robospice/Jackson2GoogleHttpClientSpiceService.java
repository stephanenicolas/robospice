package com.octo.android.robospice;

import android.app.Application;

import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.googlehttpclient.json.Jackson2ObjectPersisterFactory;

/**
 * A {@link GoogleHttpClientSpiceService} dedicated to json web services via
 * Jackson. Provides caching.
 * @author sni
 */
public class Jackson2GoogleHttpClientSpiceService extends GoogleHttpClientSpiceService {
    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister(new Jackson2ObjectPersisterFactory(application));
        return cacheManager;
    }

}
