package com.octo.android.robospice.persistence.springandroid.xml;

import java.io.File;
import java.util.List;

import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterFactory;

public class SimpleSerializerObjectPersisterFactory extends InFileObjectPersisterFactory {

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public SimpleSerializerObjectPersisterFactory(Application application, File cacheFolder)
        throws CacheCreationException {
        super(application, cacheFolder);
    }

    public SimpleSerializerObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses,
        File cacheFolder) throws CacheCreationException {
        super(application, listHandledClasses, cacheFolder);
    }

    public SimpleSerializerObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses)
        throws CacheCreationException {
        super(application, listHandledClasses);
    }

    public SimpleSerializerObjectPersisterFactory(Application application) throws CacheCreationException {
        super(application);
    }

    // ----------------------------------
    // API
    // ----------------------------------

    @Override
    public <DATA> InFileObjectPersister<DATA> createInFileObjectPersister(Class<DATA> clazz, File cacheFolder)
        throws CacheCreationException {
        return new SimpleSerializerObjectPersister<DATA>(getApplication(), clazz, cacheFolder);
    }

}
