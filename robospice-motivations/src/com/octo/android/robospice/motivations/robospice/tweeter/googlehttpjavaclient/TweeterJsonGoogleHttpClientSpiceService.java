package com.octo.android.robospice.motivations.robospice.tweeter.googlehttpjavaclient;

import android.app.Application;

import com.google.api.client.json.jackson.JacksonFactory;
import com.octo.android.robospice.GoogleHttpClientSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.binary.InFileInputStreamObjectPersister;
import com.octo.android.robospice.persistence.googlehttpjavaclient.json.JsonObjectPersisterFactory;
import com.octo.android.robospice.persistence.string.InFileStringObjectPersister;

public class TweeterJsonGoogleHttpClientSpiceService extends GoogleHttpClientSpiceService {

    @Override
    public int getThreadCount() {
        return 3;
    }

    @Override
    public CacheManager createCacheManager( Application application ) {
        CacheManager cacheManager = new CacheManager();

        // init
        InFileStringObjectPersister inFileStringObjectPersister = new InFileStringObjectPersister( application );
        InFileInputStreamObjectPersister inFileInputStreamObjectPersister = new InFileInputStreamObjectPersister( application );
        JsonObjectPersisterFactory inJSonFileObjectPersisterFactory = new JsonObjectPersisterFactory( application, new JacksonFactory() );

        inFileStringObjectPersister.setAsyncSaveEnabled( true );
        inFileInputStreamObjectPersister.setAsyncSaveEnabled( true );
        inJSonFileObjectPersisterFactory.setAsyncSaveEnabled( true );

        cacheManager.addPersister( inFileStringObjectPersister );
        cacheManager.addPersister( inFileInputStreamObjectPersister );
        cacheManager.addPersister( inJSonFileObjectPersisterFactory );
        return cacheManager;
    }

}
