package com.octo.android.robospice.persistence.springandroid.xml;

import java.io.File;
import java.io.IOException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.springandroid.SpringAndroidObjectPersister;

public final class SimpleSerializerObjectPersister<T> extends SpringAndroidObjectPersister<T> {

    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================

    private Serializer serializer = new Persister();

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================

    public SimpleSerializerObjectPersister(Application application, Class<T> clazz, File cacheFolder)
        throws CacheCreationException {
        super(application, clazz, cacheFolder);
        this.serializer = new Persister();
    }

    public SimpleSerializerObjectPersister(Application application, Class<T> clazz) throws CacheCreationException {
        this(application, clazz, null);
    }

    // ============================================================================================
    // METHODS
    // ============================================================================================

    @Override
    protected T deserializeData(String xml) throws CacheLoadingException {
        try {
            return serializer.read(getHandledClass(), xml);
        } catch (Exception e) {
            throw new CacheLoadingException(e);
        }
    }

    @Override
    protected void saveData(T data, Object cacheKey) throws IOException, CacheSavingException {
        try {
            serializer.write(data, getCacheFile(cacheKey));
        } catch (Exception e) {
            throw new CacheSavingException("Data was null and could not be serialized in xml");
        }
    }
}
