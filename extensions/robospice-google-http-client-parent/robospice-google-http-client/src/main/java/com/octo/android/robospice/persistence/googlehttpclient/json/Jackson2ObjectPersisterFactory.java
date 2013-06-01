package com.octo.android.robospice.persistence.googlehttpclient.json;

import java.io.File;
import java.util.List;

import android.app.Application;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

/**
 * Allows to serialize objects using the google http java client jackson 2
 * module.
 * @author sni
 */
public class Jackson2ObjectPersisterFactory extends JsonObjectPersisterFactory {

    public Jackson2ObjectPersisterFactory(Application application, File cacheFolder) throws CacheCreationException {
        super(application, new JacksonFactory(), cacheFolder);
    }

    public Jackson2ObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses, File cacheFolder)
        throws CacheCreationException {
        super(application, new JacksonFactory(), listHandledClasses, cacheFolder);
    }

    public Jackson2ObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses)
        throws CacheCreationException {
        super(application, new JacksonFactory(), listHandledClasses);
    }

    public Jackson2ObjectPersisterFactory(Application application) throws CacheCreationException {
        super(application, new JacksonFactory());
    }

}
