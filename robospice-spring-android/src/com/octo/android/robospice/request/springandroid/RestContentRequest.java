package com.octo.android.robospice.request.springandroid;

import org.springframework.web.client.RestTemplate;

import android.util.Log;

import com.octo.android.robospice.request.ContentRequest;

public abstract class RestContentRequest< RESULT > extends ContentRequest< RESULT > {

    private RestTemplate restTemplate;

    public RestContentRequest( Class< RESULT > clazz ) {
        super( clazz );
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate( RestTemplate restTemplate ) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Deprecated
    /**
     * This method should be invoked directly. It is invoked internally by the framework.
     * @see ContentManager#cancelAllRequests();
     */
    public void cancel() {
        super.cancel();
        Log.w( RestContentRequest.class.getName(), "Cancel can't be invoked directly on " + RestContentRequest.class.getName()
                + " requests. You must call ContentManager.cancelAllRequests()." );
    }
}