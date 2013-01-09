package com.octo.android.robospice.persistence.springandroid.xml;

import java.io.IOException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.springandroid.SpringAndroidObjectPersister;

public final class SimpleSerializerObjectPersister<T> extends
    SpringAndroidObjectPersister<T> {

    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================

    private Serializer serializer = new Persister();

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================
    public SimpleSerializerObjectPersister(Application application,
        Class<T> clazz, String factoryPrefix) {
        super(application, clazz, factoryPrefix);
        this.serializer = new Persister();
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
    protected void saveData(T data, Object cacheKey) throws IOException,
        CacheSavingException {
        try {
            serializer.write(data, getCacheFile(cacheKey));
        } catch (Exception e) {
            throw new CacheSavingException(
                "Data was null and could not be serialized in xml");
        }
    }
}
