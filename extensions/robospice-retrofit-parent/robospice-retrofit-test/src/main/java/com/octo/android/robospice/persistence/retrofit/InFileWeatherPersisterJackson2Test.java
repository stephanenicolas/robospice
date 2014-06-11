package com.octo.android.robospice.persistence.retrofit;

import android.app.Application;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.persistence.exception.CacheCreationException;

@SmallTest
public class InFileWeatherPersisterJackson2Test extends JsonObjectPersisterFactoryTest {

    @Override
    protected RetrofitObjectPersisterFactory getRetrofitObjectPersisterFactory(Application application)
        throws CacheCreationException {
        return new JacksonRetrofitObjectPersisterFactory(application);
    }
}
