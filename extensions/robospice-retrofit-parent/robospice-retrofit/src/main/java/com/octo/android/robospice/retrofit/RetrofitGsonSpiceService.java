package com.octo.android.robospice.retrofit;

import java.io.File;

import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import android.app.Application;

import com.google.gson.Gson;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.retrofit.GsonRetrofitObjectPersisterFactory;

/**
 * A pre-set, easy to use, retrofit service. It will use retrofit for network
 * requests and both networking and caching will use Gson. To use it, just add
 * to your manifest :
 * 
 * <pre>
 * &lt;service
 *   android:name="com.octo.android.robospice.retrofit.RetrofitGsonSpiceService"
 *   android:exported="false" /&gt;
 * </pre>
 * @author SNI
 */
public abstract class RetrofitGsonSpiceService extends RetrofitSpiceService {

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister(new GsonRetrofitObjectPersisterFactory(application, getConverter(), getCacheFolder()));
        return cacheManager;
    }

    @Override
    protected Converter createConverter() {
        return  new GsonConverter(new Gson());
    }

    public File getCacheFolder() {
        return null;
    }
}
