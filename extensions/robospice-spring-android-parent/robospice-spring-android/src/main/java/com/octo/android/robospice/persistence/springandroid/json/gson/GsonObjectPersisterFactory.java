package com.octo.android.robospice.persistence.springandroid.json.gson;

import java.io.File;
import java.util.List;

import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterFactory;

public class GsonObjectPersisterFactory extends InFileObjectPersisterFactory {

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------
    public GsonObjectPersisterFactory(Application application, File cacheFolder) throws CacheCreationException {
        super(application, cacheFolder);
    }

    public GsonObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses, File cacheFolder)
        throws CacheCreationException {
        super(application, listHandledClasses, cacheFolder);
    }

    public GsonObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses)
        throws CacheCreationException {
        super(application, listHandledClasses);
    }

    public GsonObjectPersisterFactory(Application application) throws CacheCreationException {
        super(application);
    }

    // ----------------------------------
    // API
    // ----------------------------------

    @Override
    public <DATA> InFileObjectPersister<DATA> createInFileObjectPersister(Class<DATA> clazz, File cacheFolder)
        throws CacheCreationException {
        return new GsonObjectPersister<DATA>(getApplication(), clazz, cacheFolder);
    }

}
