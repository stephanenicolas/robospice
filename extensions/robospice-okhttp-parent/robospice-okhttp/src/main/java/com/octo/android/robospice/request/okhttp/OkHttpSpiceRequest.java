package com.octo.android.robospice.request.okhttp;

import com.octo.android.robospice.request.SpiceRequest;
import com.squareup.okhttp.OkHttpClient;

/**
 * A simplified {@link SpiceRequest} that makes it even easier to use a
 * OkHttpClient.
 * @author SNI
 * @param <T>
 *            the result type of this request.
 */
public abstract class OkHttpSpiceRequest<T> extends SpiceRequest<T> {

    private OkHttpClient okHttpClient;

    public OkHttpSpiceRequest(Class<T> clazz) {
        super(clazz);
    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}
