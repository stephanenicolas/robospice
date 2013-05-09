package com.octo.android.robospice.retrofit;

import retrofit.http.Converter;
import retrofit.http.GsonConverter;
import retrofit.http.RestAdapter;
import android.app.Application;

import com.google.gson.Gson;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.retrofit.RetrofitObjectPersisterFactory;

public class RetrofitGsonSpiceService extends RetrofitSpiceService {

    private Converter converter = new GsonConverter(new Gson());

    @Override
    public RestAdapter.Builder createRestAdapterBuilder() {
        RestAdapter.Builder restAdapter = new RestAdapter.Builder()//
                .setConverter(converter);
        return restAdapter;
    }

    @Override
    public CacheManager createCacheManager(Application application) {
        CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister(new RetrofitObjectPersisterFactory(application, converter));
        return cacheManager;
    }

}
