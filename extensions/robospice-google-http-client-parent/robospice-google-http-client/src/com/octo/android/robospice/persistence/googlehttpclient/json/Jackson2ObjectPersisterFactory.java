package com.octo.android.robospice.persistence.googlehttpclient.json;

import java.util.List;

import android.app.Application;

import com.google.api.client.json.jackson2.JacksonFactory;

/**
 * Allows to serialize objects using the google http java client jackson 2
 * module.
 * @author sni
 */
public class Jackson2ObjectPersisterFactory extends JsonObjectPersisterFactory {

    public Jackson2ObjectPersisterFactory(Application application,
        List<Class<?>> listHandledClasses) {
        super(application, new JacksonFactory(), listHandledClasses);
    }

    public Jackson2ObjectPersisterFactory(Application application) {
        this(application, null);
    }

}
