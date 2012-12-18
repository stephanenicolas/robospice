package com.octo.android.robospice.persistence.googlehttpclient.json;

import android.app.Application;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.persistence.googlehttpclient.json.GsonObjectPersisterFactory;
import com.octo.android.robospice.persistence.googlehttpclient.json.JsonObjectPersisterFactory;

@SmallTest
public class InFileWeatherPersisterGsonTest extends JsonObjectPersisterFactoryTest {

    @Override
    protected JsonObjectPersisterFactory getJsonObjectPersisterFactory( Application application ) {
        return new GsonObjectPersisterFactory( application );
    }
}
