package com.octo.android.robospice.persistence.googlehttpclient.json;

import java.io.File;
import java.util.List;

import android.app.Application;

import com.google.api.client.json.JsonFactory;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterFactory;

public abstract class JsonObjectPersisterFactory extends InFileObjectPersisterFactory {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private JsonFactory jsonFactory;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------
    public JsonObjectPersisterFactory(Application application, JsonFactory jsonFactory) throws CacheCreationException {
        this(application, jsonFactory, null, null);
    }

    public JsonObjectPersisterFactory(Application application, JsonFactory jsonFactory, File cacheFolder)
        throws CacheCreationException {
        this(application, jsonFactory, null, cacheFolder);
    }

    public JsonObjectPersisterFactory(Application application, JsonFactory jsonFactory,
        List<Class<?>> listHandledClasses) throws CacheCreationException {
        this(application, jsonFactory, listHandledClasses, null);
    }

    public JsonObjectPersisterFactory(Application application, JsonFactory jsonFactory,
        List<Class<?>> listHandledClasses, File cacheFolder) throws CacheCreationException {
        super(application, listHandledClasses, cacheFolder);
        this.jsonFactory = jsonFactory;
    }

    // ----------------------------------
    // API
    // ----------------------------------

    @Override
    public <DATA> JsonObjectPersister<DATA> createInFileObjectPersister(Class<DATA> clazz, File cacheFolder)
        throws CacheCreationException {
        return new JsonObjectPersister<DATA>(getApplication(), jsonFactory, clazz, cacheFolder);
    }

}
