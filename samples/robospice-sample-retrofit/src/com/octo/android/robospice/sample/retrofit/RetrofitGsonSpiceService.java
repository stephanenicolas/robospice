package com.octo.android.robospice.sample.retrofit;

import org.apache.http.impl.client.DefaultHttpClient;

import retrofit.http.Converter;
import retrofit.http.GsonConverter;
import retrofit.http.RestAdapter;
import android.app.Application;
import cm.octo.android.robospice.persistence.retrofit.RetrofitObjectPersisterFactory;

import com.google.gson.Gson;
import com.octo.android.robospice.persistence.CacheManager;

public abstract class RetrofitGsonSpiceService extends RetrofitSpiceService {

    private Converter converter = new GsonConverter( new Gson() );

    @Override
    public RestAdapter.Builder createRestAdapterBuilder() {
        RestAdapter.Builder restAdapter = new RestAdapter.Builder()//
                .setClient( new DefaultHttpClient() )//
                .setConverter( converter );

        return restAdapter;
    }

    @Override
    public CacheManager createCacheManager( Application application ) {
        CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister( new RetrofitObjectPersisterFactory( application, converter ) );
        return cacheManager;
    }

}
