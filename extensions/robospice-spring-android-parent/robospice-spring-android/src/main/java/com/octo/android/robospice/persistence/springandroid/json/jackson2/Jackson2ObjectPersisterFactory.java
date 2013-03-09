package com.octo.android.robospice.persistence.springandroid.json.jackson2;

import java.util.List;

import android.app.Application;

import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterFactory;

public class Jackson2ObjectPersisterFactory extends InFileObjectPersisterFactory {

    public Jackson2ObjectPersisterFactory(Application application) {
        super(application);
    }

    public Jackson2ObjectPersisterFactory(Application application,
        List<Class<?>> listHandledClasses) {
        super(application, listHandledClasses);
    }

    @Override
    public <DATA> InFileObjectPersister<DATA> createObjectPersister(
        Class<DATA> clazz) {
        InFileObjectPersister<DATA> inFileObjectPersister = new Jackson2ObjectPersister<DATA>(
            getApplication(), clazz, getCachePrefix());
        inFileObjectPersister.setAsyncSaveEnabled(isAsyncSaveEnabled());
        return inFileObjectPersister;
    }

}
