package com.octo.android.robospice.persistence.googlehttpclient.json;

import java.util.List;

import android.app.Application;

import com.google.api.client.json.jackson.JacksonFactory;

/**
 * Allows to serialize objects using the google http java client jackson 1
 * module.
 * @author sni
 */
public class JacksonObjectPersisterFactory extends JsonObjectPersisterFactory {

    public JacksonObjectPersisterFactory(Application application,
        List<Class<?>> listHandledClasses) {
        super(application, new JacksonFactory(), listHandledClasses);
    }

    public JacksonObjectPersisterFactory(Application application) {
        this(application, null);
    }

}
