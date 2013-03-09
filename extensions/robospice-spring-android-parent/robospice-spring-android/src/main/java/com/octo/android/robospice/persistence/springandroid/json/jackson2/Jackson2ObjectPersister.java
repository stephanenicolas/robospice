package com.octo.android.robospice.persistence.springandroid.json.jackson2;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import android.app.Application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.springandroid.SpringAndroidObjectPersister;

public final class Jackson2ObjectPersister<T> extends SpringAndroidObjectPersister<T> {

    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================

    private final ObjectMapper mJsonMapper;

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================
    public Jackson2ObjectPersister(Application application, Class<T> clazz, String factoryPrefix) {
        super(application, clazz, factoryPrefix);
        this.mJsonMapper = new ObjectMapper();
    }

    // ============================================================================================
    // METHODS
    // ============================================================================================

    @Override
    protected T deserializeData(String json) throws CacheLoadingException {
        try {
            return mJsonMapper.readValue(json, getHandledClass());
        } catch (Exception e) {
            throw new CacheLoadingException(e);
        }
    }

    @Override
    protected void saveData(T data, Object cacheKey) throws IOException, CacheSavingException {
        String resultJson;
        // transform the content in json to store it in the cache
        resultJson = mJsonMapper.writeValueAsString(data);

        // finally store the json in the cache
        if (!StringUtils.isEmpty(resultJson)) {
            FileUtils.writeStringToFile(getCacheFile(cacheKey), resultJson, CharEncoding.UTF_8);
        } else {
            throw new CacheSavingException("Data was null and could not be serialized in json");
        }
    }

}
