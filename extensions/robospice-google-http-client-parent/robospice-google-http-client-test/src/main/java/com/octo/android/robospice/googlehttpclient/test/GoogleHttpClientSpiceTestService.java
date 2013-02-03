package com.octo.android.robospice.googlehttpclient.test;

import android.app.Application;

import com.octo.android.robospice.GoogleHttpClientSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.googlehttpclient.json.JacksonObjectPersisterFactory;

public class GoogleHttpClientSpiceTestService extends
    GoogleHttpClientSpiceService {

    @Override
    public CacheManager createCacheManager(Application application) {
        CacheManager cacheManager = new CacheManager();
        JacksonObjectPersisterFactory jacksonObjectPersisterFactory = new JacksonObjectPersisterFactory(
            application);
        cacheManager.addPersister(jacksonObjectPersisterFactory);
        return cacheManager;
    }

}
