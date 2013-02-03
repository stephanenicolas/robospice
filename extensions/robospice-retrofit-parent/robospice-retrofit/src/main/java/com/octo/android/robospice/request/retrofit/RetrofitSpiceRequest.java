package com.octo.android.robospice.request.retrofit;

import retrofit.http.RestAdapter;

import com.octo.android.robospice.request.SpiceRequest;

public abstract class RetrofitSpiceRequest<T> extends SpiceRequest<T> {

    public RetrofitSpiceRequest(Class<T> clazz) {
        super(clazz);
    }

    private RestAdapter.Builder restAdapterBuilder;

    public RestAdapter.Builder getRestAdapterBuilder() {
        return restAdapterBuilder;
    }

    public void setRestAdapterBuilder(RestAdapter.Builder restAdapterBuilder) {
        this.restAdapterBuilder = restAdapterBuilder;
    }

}
