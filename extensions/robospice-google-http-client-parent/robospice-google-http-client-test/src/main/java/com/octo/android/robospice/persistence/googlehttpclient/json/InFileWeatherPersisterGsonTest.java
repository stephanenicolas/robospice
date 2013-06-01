package com.octo.android.robospice.persistence.googlehttpclient.json;

import android.app.Application;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.persistence.exception.CacheCreationException;

@SmallTest
public class InFileWeatherPersisterGsonTest extends JsonObjectPersisterFactoryTest {

    @Override
    protected JsonObjectPersisterFactory getJsonObjectPersisterFactory(Application application)
        throws CacheCreationException {
        return new GsonObjectPersisterFactory(application);
    }
}
