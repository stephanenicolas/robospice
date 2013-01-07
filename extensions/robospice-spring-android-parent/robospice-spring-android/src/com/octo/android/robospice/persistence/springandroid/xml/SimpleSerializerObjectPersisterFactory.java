package com.octo.android.robospice.persistence.springandroid.xml;

import java.util.List;

import android.app.Application;

import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterFactory;

public class SimpleSerializerObjectPersisterFactory extends
    InFileObjectPersisterFactory {

    public SimpleSerializerObjectPersisterFactory(Application application) {
        super(application);
    }

    public SimpleSerializerObjectPersisterFactory(Application application,
        List<Class<?>> listHandledClasses) {
        super(application, listHandledClasses);
    }

    @Override
    public <DATA> InFileObjectPersister<DATA> createObjectPersister(
        Class<DATA> clazz) {
        InFileObjectPersister<DATA> inFileObjectPersister = new SimpleSerializerObjectPersister<DATA>(
            getApplication(), clazz, getCachePrefix());
        inFileObjectPersister.setAsyncSaveEnabled(isAsyncSaveEnabled());
        return inFileObjectPersister;
    }

}
