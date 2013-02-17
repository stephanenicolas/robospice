package com.octo.android.robospice.persistence.googlehttpclient.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import roboguice.util.temp.Ln;
import android.app.Application;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;

public final class JsonObjectPersister<T> extends InFileObjectPersister<T> {

    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================

    private final JsonFactory jsonFactory;

    private String mFactoryPrefix;

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================
    public JsonObjectPersister(Application application, Class<T> clazz,
        String factoryPrefix, JsonFactory jsonFactory) {
        super(application, clazz);
        this.jsonFactory = jsonFactory;
        this.mFactoryPrefix = factoryPrefix;
    }

    // ============================================================================================
    // METHODS
    // ============================================================================================

    @Override
    protected String getCachePrefix() {
        return mFactoryPrefix + super.getCachePrefix();
    }

    @Override
    protected T readCacheDataFromFile(File file) throws CacheLoadingException {
        try {
            JsonParser jsonParser = jsonFactory
                .createJsonParser(new FileReader(file));
            T result = jsonParser.parse(getHandledClass(), null);
            jsonParser.close();
            return result;
        } catch (FileNotFoundException e) {
            // Should not occur (we test before if file exists)
            // Do not throw, file is not cached
            Ln.w("file " + file.getAbsolutePath() + " does not exists",
                e);
            return null;
        } catch (Exception e) {
            throw new CacheLoadingException(e);
        }
    }

    @Override
    public T saveDataToCacheAndReturnData(final T data, final Object cacheKey)
        throws CacheSavingException {

        try {
            if (isAsyncSaveEnabled()) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            saveData(data, cacheKey);
                        } catch (IOException e) {
                            Ln.e(e, "An error occured on saving request "
                                + cacheKey + " data asynchronously");
                        } catch (CacheSavingException e) {
                            Ln.e(e, "An error occured on saving request "
                                + cacheKey + " data asynchronously");
                        }
                    };
                };
                t.start();
            } else {
                saveData(data, cacheKey);
            }
        } catch (CacheSavingException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheSavingException(e);
        }
        return data;
    }

    private void saveData(T data, Object cacheKey) throws IOException,
        CacheSavingException {
        // transform the content in json to store it in the cache
        JsonGenerator jsonGenerator = jsonFactory
            .createJsonGenerator(new FileWriter(getCacheFile(cacheKey)));
        jsonGenerator.serialize(data);
        jsonGenerator.flush();
        jsonGenerator.close();
    }

}
