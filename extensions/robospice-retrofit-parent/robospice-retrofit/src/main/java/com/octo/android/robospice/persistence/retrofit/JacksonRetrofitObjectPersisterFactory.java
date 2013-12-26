package com.octo.android.robospice.persistence.retrofit;

import android.app.Application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

import java.io.File;
import java.util.List;

import retrofit.converter.JacksonConverter;

public class JacksonRetrofitObjectPersisterFactory extends RetrofitObjectPersisterFactory {

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================
    public JacksonRetrofitObjectPersisterFactory(Application application, File cacheFolder) throws CacheCreationException {
        super(application, new JacksonConverter(new ObjectMapper()), cacheFolder);
    }

    public JacksonRetrofitObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses,
                                                 File cacheFolder) throws CacheCreationException {
        super(application, new JacksonConverter(new ObjectMapper()), listHandledClasses, cacheFolder);
    }

    public JacksonRetrofitObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses)
        throws CacheCreationException {
        super(application, new JacksonConverter(new ObjectMapper()), listHandledClasses);
    }

    public JacksonRetrofitObjectPersisterFactory(Application application) throws CacheCreationException {
        super(application, new JacksonConverter(new ObjectMapper()));
    }

}
