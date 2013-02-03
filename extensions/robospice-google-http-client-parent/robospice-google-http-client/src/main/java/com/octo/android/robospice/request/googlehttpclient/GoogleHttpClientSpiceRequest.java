package com.octo.android.robospice.request.googlehttpclient;

import com.google.api.client.http.HttpRequestFactory;
import com.octo.android.robospice.request.SpiceRequest;

public abstract class GoogleHttpClientSpiceRequest<RESULT> extends
    SpiceRequest<RESULT> {

    private HttpRequestFactory httpRequestFactory;

    public GoogleHttpClientSpiceRequest(Class<RESULT> clazz) {
        super(clazz);
    }

    public void setHttpRequestFactory(HttpRequestFactory httpRequestFactory) {
        this.httpRequestFactory = httpRequestFactory;
    }

    public HttpRequestFactory getHttpRequestFactory() {
        return httpRequestFactory;
    }

}
