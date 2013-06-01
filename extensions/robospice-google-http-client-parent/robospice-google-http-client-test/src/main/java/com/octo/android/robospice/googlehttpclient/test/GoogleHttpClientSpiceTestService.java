package com.octo.android.robospice.googlehttpclient.test;

import java.io.File;

import roboguice.util.temp.Ln;
import android.app.Application;

import com.octo.android.robospice.GoogleHttpClientSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.googlehttpclient.json.JacksonObjectPersisterFactory;

public class GoogleHttpClientSpiceTestService extends GoogleHttpClientSpiceService {

    @Override
    public CacheManager createCacheManager(Application application) {
        CacheManager cacheManager = new CacheManager();
        try {
            JacksonObjectPersisterFactory jacksonObjectPersisterFactory = new JacksonObjectPersisterFactory(
                application, new File("/"));
            cacheManager.addPersister(jacksonObjectPersisterFactory);
        } catch (CacheCreationException e) {
            Ln.e(e);
        }
        return cacheManager;
    }

}
