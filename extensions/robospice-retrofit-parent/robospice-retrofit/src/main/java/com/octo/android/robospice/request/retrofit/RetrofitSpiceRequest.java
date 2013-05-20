package com.octo.android.robospice.request.retrofit;

import retrofit.RestAdapter;

import com.octo.android.robospice.request.SpiceRequest;

public abstract class RetrofitSpiceRequest<T> extends SpiceRequest<T> {
    private RestAdapter.Builder restAdapterBuilder;

    public RetrofitSpiceRequest(Class<T> clazz) {
        super(clazz);
    }

    public RestAdapter.Builder getRestAdapterBuilder() {
        return restAdapterBuilder;
    }

    public void setRestAdapterBuilder(RestAdapter.Builder restAdapterBuilder) {
        this.restAdapterBuilder = restAdapterBuilder;
    }

}
