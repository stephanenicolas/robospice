package com.octo.android.robospice.persistence.googlehttpclient.json;

import android.app.Application;
import android.test.suitebuilder.annotation.SmallTest;

@SmallTest
public class InFileWeatherPersisterJacksonTest extends
    JsonObjectPersisterFactoryTest {

    @Override
    protected JsonObjectPersisterFactory getJsonObjectPersisterFactory(
        Application application) {
        return new JacksonObjectPersisterFactory(application);
    }
}
