package com.octo.android.robospice.persistence.googlehttpclient.json;

import java.io.File;
import java.util.List;

import android.app.Application;

import com.google.api.client.json.gson.GsonFactory;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

/**
 * Allows to serialize objects using the google http java client gson module.
 * @author sni
 */
public class GsonObjectPersisterFactory extends JsonObjectPersisterFactory {

    public GsonObjectPersisterFactory(Application application, File cacheFolder) throws CacheCreationException {
        super(application, new GsonFactory(), cacheFolder);
    }

    public GsonObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses, File cacheFolder)
        throws CacheCreationException {
        super(application, new GsonFactory(), listHandledClasses, cacheFolder);
    }

    public GsonObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses)
        throws CacheCreationException {
        super(application, new GsonFactory(), listHandledClasses);
    }

    public GsonObjectPersisterFactory(Application application) throws CacheCreationException {
        super(application, new GsonFactory());
    }
}
