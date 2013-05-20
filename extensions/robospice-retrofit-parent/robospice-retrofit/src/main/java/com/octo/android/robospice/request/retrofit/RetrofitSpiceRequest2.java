package com.octo.android.robospice.request.retrofit;

import com.octo.android.robospice.request.SpiceRequest;

/**
 * A simplified {@link SpiceRequest} that makes it even easier to use a retrofited REST service.
 * @author SNI
 * @param <T>
 *            the result type of this request.
 * @param <R>
 *            the retrofited interface used by this request.
 */
public abstract class RetrofitSpiceRequest2<T, R> extends RetrofitSpiceRequest<T> {

    private String serverUrl;
    private Class<R> retrofitedInterface;

    public RetrofitSpiceRequest2(Class<T> clazz, Class<R> retrofitedInterface, String serverUrl) {
        super(clazz);
        this.retrofitedInterface = retrofitedInterface;
        this.serverUrl = serverUrl;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public R getRetrofitedService() {
        return getRestAdapterBuilder().setServer(serverUrl).build().create(retrofitedInterface);
    }

}
