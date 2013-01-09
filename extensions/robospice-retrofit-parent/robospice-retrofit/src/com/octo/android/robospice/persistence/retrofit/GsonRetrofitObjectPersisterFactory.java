package com.octo.android.robospice.persistence.retrofit;

import java.util.List;

import retrofit.http.GsonConverter;
import android.app.Application;

import com.google.gson.Gson;

public class GsonRetrofitObjectPersisterFactory extends
    RetrofitObjectPersisterFactory {

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================
    public GsonRetrofitObjectPersisterFactory(Application application,
        List<Class<?>> listHandledClasses) {
        super(application, new GsonConverter(new Gson()), listHandledClasses);
    }

    public GsonRetrofitObjectPersisterFactory(Application application) {
        super(application, new GsonConverter(new Gson()));
    }
}
