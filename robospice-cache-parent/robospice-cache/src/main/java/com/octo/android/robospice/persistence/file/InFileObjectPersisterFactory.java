package com.octo.android.robospice.persistence.file;

import java.util.List;

import android.app.Application;

import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.ObjectPersisterFactory;

/**
 * A factory that will create {@link ObjectPersister} instances will that
 * saves/loads data in a file.
 * @author sni
 * @param <T>
 *            the class of the data to load/save.
 */
public abstract class InFileObjectPersisterFactory extends ObjectPersisterFactory {

    public InFileObjectPersisterFactory(Application application) {
        super(application);
    }

    public InFileObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses) {
        super(application, listHandledClasses);
    }

    @Override
    public abstract <T> InFileObjectPersister<T> createObjectPersister(Class<T> clazz);

    protected String getCachePrefix() {
        return getClass().getSimpleName() + InFileObjectPersister.CACHE_PREFIX_END;
    }

}
