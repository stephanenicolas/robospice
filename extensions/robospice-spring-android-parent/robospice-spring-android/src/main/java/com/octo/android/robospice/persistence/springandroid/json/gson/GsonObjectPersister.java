package com.octo.android.robospice.persistence.springandroid.json.gson;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import android.app.Application;

import com.google.gson.Gson;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.springandroid.SpringAndroidObjectPersister;

public final class GsonObjectPersister<T> extends SpringAndroidObjectPersister<T> {

    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================

    private final Gson gson;

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================
    public GsonObjectPersister(Application application, Class<T> clazz, File cacheFolder) throws CacheCreationException {
        super(application, clazz, cacheFolder);
        this.gson = new Gson();
    }

    public GsonObjectPersister(Application application, Class<T> clazz) throws CacheCreationException {
        this(application, clazz, null);
    }

    // ============================================================================================
    // METHODS
    // ============================================================================================

    @Override
    protected T deserializeData(String json) {
        return gson.fromJson(json, getHandledClass());
    }

    @Override
    protected void saveData(T data, Object cacheKey) throws IOException, CacheSavingException {
        String resultJson;
        // transform the content in json to store it in the cache
        resultJson = gson.toJson(data);

        // finally store the json in the cache
        if (!StringUtils.isEmpty(resultJson)) {
            FileUtils.writeStringToFile(getCacheFile(cacheKey), resultJson, CharEncoding.UTF_8);
        } else {
            throw new CacheSavingException("Data was null and could not be serialized in json");
        }
    }

}
