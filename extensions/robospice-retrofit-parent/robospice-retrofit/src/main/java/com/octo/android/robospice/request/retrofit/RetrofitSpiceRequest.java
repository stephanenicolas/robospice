package com.octo.android.robospice.request.retrofit;

import com.octo.android.robospice.request.SpiceRequest;

/**
 * A simplified {@link SpiceRequest} that makes it even easier to use a
 * retrofited REST service.
 * @author SNI
 * @param <T>
 *            the result type of this request.
 * @param <R>
 *            the retrofited interface used by this request.
 */
public abstract class RetrofitSpiceRequest<T, R> extends SpiceRequest<T> {

    private Class<R> retrofitedInterfaceClass;
    private R service;

    public RetrofitSpiceRequest(Class<T> clazz, Class<R> retrofitedInterfaceClass) {
        super(clazz);
        this.retrofitedInterfaceClass = retrofitedInterfaceClass;
    }

    public Class<R> getRetrofitedInterfaceClass() {
        return retrofitedInterfaceClass;
    }

    public void setService(R service) {
        this.service = service;
    }

    public R getService() {
        return service;
    }

}
