package com.octo.android.robospice.persistence.springandroid.json.gson;

import java.util.List;

import android.app.Application;

import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterFactory;

public class GsonObjectPersisterFactory extends InFileObjectPersisterFactory {

    public GsonObjectPersisterFactory(Application application) {
        super(application);
    }

    public GsonObjectPersisterFactory(Application application,
        List<Class<?>> listHandledClasses) {
        super(application, listHandledClasses);
    }

    @Override
    public <DATA> InFileObjectPersister<DATA> createObjectPersister(
        Class<DATA> clazz) {
        InFileObjectPersister<DATA> inFileObjectPersister = new GsonObjectPersister<DATA>(
            getApplication(), clazz, getCachePrefix());
        inFileObjectPersister.setAsyncSaveEnabled(isAsyncSaveEnabled());
        return inFileObjectPersister;
    }

}
