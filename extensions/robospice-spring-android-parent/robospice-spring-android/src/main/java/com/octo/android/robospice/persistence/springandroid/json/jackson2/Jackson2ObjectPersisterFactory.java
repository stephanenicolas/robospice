package com.octo.android.robospice.persistence.springandroid.json.jackson2;

import java.io.File;
import java.util.List;

import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterFactory;

public class Jackson2ObjectPersisterFactory extends InFileObjectPersisterFactory {

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public Jackson2ObjectPersisterFactory(Application application) throws CacheCreationException {
        super(application);
    }

    public Jackson2ObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses)
        throws CacheCreationException {
        super(application, listHandledClasses);
    }

    public Jackson2ObjectPersisterFactory(Application application, File cacheFolder) throws CacheCreationException {
        super(application, cacheFolder);
    }

    public Jackson2ObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses, File cacheFolder)
        throws CacheCreationException {
        super(application, listHandledClasses, cacheFolder);
    }

    // ----------------------------------
    // API
    // ----------------------------------
    @Override
    public <DATA> InFileObjectPersister<DATA> createInFileObjectPersister(Class<DATA> clazz, File cacheFolder)
        throws CacheCreationException {
        return new Jackson2ObjectPersister<DATA>(getApplication(), clazz, cacheFolder);
    }

}
