package com.octo.android.robospice.persistence.googlehttpclient.json;

import java.util.List;

import android.app.Application;

import com.google.api.client.json.JsonFactory;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterFactory;

public abstract class JsonObjectPersisterFactory extends
    InFileObjectPersisterFactory {

    private JsonFactory jsonFactory;

    public JsonObjectPersisterFactory(Application application,
        JsonFactory jsonFactory) {
        super(application);
        this.jsonFactory = jsonFactory;
    }

    public JsonObjectPersisterFactory(Application application,
        JsonFactory jsonFactory, List<Class<?>> listHandledClasses) {
        super(application, listHandledClasses);
        this.jsonFactory = jsonFactory;
    }

    @Override
    public <DATA> InFileObjectPersister<DATA> createObjectPersister(
        Class<DATA> clazz) {
        InFileObjectPersister<DATA> inFileObjectPersister = new JsonObjectPersister<DATA>(
            getApplication(), clazz, getCachePrefix(), jsonFactory);
        inFileObjectPersister.setAsyncSaveEnabled(isAsyncSaveEnabled());
        return inFileObjectPersister;
    }

}
