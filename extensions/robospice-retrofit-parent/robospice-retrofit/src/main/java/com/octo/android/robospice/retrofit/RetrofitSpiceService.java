package com.octo.android.robospice.retrofit;

import java.util.Set;

import retrofit.http.RestAdapter;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public abstract class RetrofitSpiceService extends SpiceService {

    private RestAdapter.Builder restAdapterBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        restAdapterBuilder = createRestAdapterBuilder();
    }

    public abstract RestAdapter.Builder createRestAdapterBuilder();

    @Override
    public void addRequest(CachedSpiceRequest<?> request,
        Set<RequestListener<?>> listRequestListener) {
        if (request.getSpiceRequest() instanceof RetrofitSpiceRequest) {
            ((RetrofitSpiceRequest<?>) request.getSpiceRequest())
                .setRestAdapterBuilder(restAdapterBuilder);
        }
        super.addRequest(request, listRequestListener);
    }
}
