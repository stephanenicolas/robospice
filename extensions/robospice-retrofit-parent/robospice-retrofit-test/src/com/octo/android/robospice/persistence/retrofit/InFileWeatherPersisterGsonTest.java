package com.octo.android.robospice.persistence.retrofit;

import android.app.Application;
import android.test.suitebuilder.annotation.SmallTest;

@SmallTest
public class InFileWeatherPersisterGsonTest extends JsonObjectPersisterFactoryTest {

    @Override
    protected RetrofitObjectPersisterFactory getRetrofitObjectPersisterFactory( Application application ) {
        return new GsonRetrofitObjectPersisterFactory( application );
    }
}
