package com.octo.android.robospice.request.googlehttpclient;

import roboguice.util.temp.Ln;

import com.google.api.client.http.HttpRequestFactory;
import com.octo.android.robospice.request.SpiceRequest;

public abstract class GoogleHttpClientSpiceRequest< RESULT > extends SpiceRequest< RESULT > {

    private HttpRequestFactory httpRequestFactory;

    public GoogleHttpClientSpiceRequest( Class< RESULT > clazz ) {
        super( clazz );
    }

    public void setHttpRequestFactory( HttpRequestFactory httpRequestFactory ) {
        this.httpRequestFactory = httpRequestFactory;
    }

    public HttpRequestFactory getHttpRequestFactory() {
        return httpRequestFactory;
    }

    @Override
    /**
     * This method doesn't really work within the Spring Android module : once the request is 
     * loading data from network, there is no way to interrupt it. This is weakness of the spring android framework,
     * and seems to come from even deeper. The IO operations on which it relies don't support the interrupt flag
     * properly.
     * Nevertheless, there are still some opportunities to cancel the request, basically during cache operations.
     */
    public void cancel() {
        super.cancel();
        Ln.w( GoogleHttpClientSpiceRequest.class.getName(), "Cancel can't be invoked directly on " + GoogleHttpClientSpiceRequest.class.getName()
                + " requests. You must call SpiceManager.cancelAllRequests()." );
    }
}