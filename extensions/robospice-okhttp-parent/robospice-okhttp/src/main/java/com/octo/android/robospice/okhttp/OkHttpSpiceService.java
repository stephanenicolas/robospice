package com.octo.android.robospice.okhttp;

import java.util.Set;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;
import com.squareup.okhttp.OkHttpClient;

public abstract class OkHttpSpiceService extends SpiceService {

    private OkHttpClient okHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        okHttpClient = createOkHttpClient();
    }

    protected OkHttpClient createOkHttpClient() {
        return new OkHttpClient();
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void addRequest(CachedSpiceRequest<?> request, Set<RequestListener<?>> listRequestListener) {
        if (request.getSpiceRequest() instanceof OkHttpSpiceRequest) {
            OkHttpSpiceRequest okHttpSpiceRequest = (OkHttpSpiceRequest) request.getSpiceRequest();
            okHttpSpiceRequest.setOkHttpClient(okHttpClient);
        }
        super.addRequest(request, listRequestListener);
    }

    protected OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}
