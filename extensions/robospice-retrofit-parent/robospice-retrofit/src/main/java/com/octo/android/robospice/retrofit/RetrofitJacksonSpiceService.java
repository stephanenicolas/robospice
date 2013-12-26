package com.octo.android.robospice.retrofit;

import android.app.Application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.retrofit.RetrofitObjectPersisterFactory;

import java.io.File;

import retrofit.RestAdapter;
import retrofit.RestAdapter.Builder;
import retrofit.converter.Converter;
import retrofit.converter.JacksonConverter;

/**
 * A pre-set, easy to use, retrofit service. It will use retrofit for network
 * requests and both networking and caching will use Jackson. To use it, just add
 * to your manifest :
 *
 * <pre>
 * &lt;service
 *   android:name="com.octo.android.robospice.retrofit.RetrofitJacksonSpiceService"
 *   android:exported="false" /&gt;
 * </pre>
 * @author Vlad Shvaydetskiy
 */
public abstract class RetrofitJacksonSpiceService extends RetrofitSpiceService {

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister(new RetrofitObjectPersisterFactory(application, getConverter(), getCacheFolder()));
        return cacheManager;
    }

    @Override
    protected Converter createConverter() {
        return new JacksonConverter(new ObjectMapper());
    }

    public File getCacheFolder() {
        return null;
    }

}