package com.octo.android.robospice.persistence.springandroid.json.jackson;

import java.util.List;

import android.app.Application;

import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterFactory;

public class JacksonObjectPersisterFactory extends InFileObjectPersisterFactory {

    public JacksonObjectPersisterFactory(Application application) {
        super(application);
    }

    public JacksonObjectPersisterFactory(Application application,
        List<Class<?>> listHandledClasses) {
        super(application, listHandledClasses);
    }

    @Override
    public <DATA> InFileObjectPersister<DATA> createObjectPersister(
        Class<DATA> clazz) {
        InFileObjectPersister<DATA> inFileObjectPersister = new JacksonObjectPersister<DATA>(
            getApplication(), clazz, getCachePrefix());
        inFileObjectPersister.setAsyncSaveEnabled(isAsyncSaveEnabled());
        return inFileObjectPersister;
    }

}
