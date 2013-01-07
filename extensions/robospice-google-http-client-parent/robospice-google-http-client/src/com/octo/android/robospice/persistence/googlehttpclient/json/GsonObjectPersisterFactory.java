package com.octo.android.robospice.persistence.googlehttpclient.json;

import java.util.List;

import android.app.Application;

import com.google.api.client.json.gson.GsonFactory;

/**
 * Allows to serialize objects using the google http java client gson module.
 * @author sni
 */
public class GsonObjectPersisterFactory extends JsonObjectPersisterFactory {

    public GsonObjectPersisterFactory(Application application,
        List<Class<?>> listHandledClasses) {
        super(application, new GsonFactory(), listHandledClasses);
    }

    public GsonObjectPersisterFactory(Application application) {
        this(application, null);
    }

}
