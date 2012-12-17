package com.octo.android.robospice.sample.googlehttpclient;

import android.app.Application;

import com.octo.android.robospice.GoogleHttpClientSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.googlehttpclient.json.JacksonObjectPersisterFactory;

/**
 * Simple service
 * 
 * @author sni
 * 
 */
public class SampleSpiceService extends GoogleHttpClientSpiceService {

    @Override
    public CacheManager createCacheManager( Application application ) {
        CacheManager cacheManager = new CacheManager();

        // init
        JacksonObjectPersisterFactory jacksonObjectPersisterFactory = new JacksonObjectPersisterFactory( application );

        cacheManager.addPersister( jacksonObjectPersisterFactory );
        return cacheManager;
    }
}
