package com.octo.android.robospice.persistence.retrofit;

import java.util.List;

import retrofit.http.Converter;
import android.app.Application;

import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterFactory;

public class RetrofitObjectPersisterFactory extends
    InFileObjectPersisterFactory {

    private Converter converter;

    public RetrofitObjectPersisterFactory(Application application,
        Converter converter) {
        super(application);
        this.converter = converter;
    }

    public RetrofitObjectPersisterFactory(Application application,
        Converter converter, List<Class<?>> listHandledClasses) {
        super(application, listHandledClasses);
        this.converter = converter;
    }

    @Override
    public <DATA> InFileObjectPersister<DATA> createObjectPersister(
        Class<DATA> clazz) {
        InFileObjectPersister<DATA> inFileObjectPersister = new RetrofitObjectPersister<DATA>(
            getApplication(), clazz, getCachePrefix(), converter);
        inFileObjectPersister.setAsyncSaveEnabled(isAsyncSaveEnabled());
        return inFileObjectPersister;
    }

}
