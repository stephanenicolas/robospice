package com.octo.android.robospice.persistence.springandroid.json.jackson;

import java.io.File;
import java.util.List;

import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterFactory;

public class JacksonObjectPersisterFactory extends InFileObjectPersisterFactory {

    // ----------------------------------
    // CONSTUCTOR
    // ----------------------------------
    public JacksonObjectPersisterFactory(Application application) throws CacheCreationException {
        super(application);
    }

    public JacksonObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses)
        throws CacheCreationException {
        super(application, listHandledClasses);
    }

    public JacksonObjectPersisterFactory(Application application, File cacheFolder) throws CacheCreationException {
        super(application, cacheFolder);
    }

    public JacksonObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses, File cacheFolder)
        throws CacheCreationException {
        super(application, listHandledClasses, cacheFolder);
    }

    // ----------------------------------
    // API
    // ----------------------------------
    @Override
    public <DATA> InFileObjectPersister<DATA> createInFileObjectPersister(Class<DATA> clazz, File cacheFolder)
        throws CacheCreationException {
        return new JacksonObjectPersister<DATA>(getApplication(), clazz, cacheFolder);
    }
}
