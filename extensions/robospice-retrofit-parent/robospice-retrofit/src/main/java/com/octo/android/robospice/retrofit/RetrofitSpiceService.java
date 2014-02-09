package com.octo.android.robospice.retrofit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit.RestAdapter;
import retrofit.converter.Converter;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

public abstract class RetrofitSpiceService extends SpiceService {

    private Map<Class<?>, Object> retrofitInterfaceToServiceMap = new HashMap<Class<?>, Object>();
    private RestAdapter.Builder builder;
    private RestAdapter restAdapter;
    protected List<Class<?>> retrofitInterfaceList = new ArrayList<Class<?>>();
    private Converter mConverter;

    @Override
    public void onCreate() {
        super.onCreate();
        builder = createRestAdapterBuilder();
        restAdapter = builder.build();
    }

    protected abstract String getServerUrl();

    protected RestAdapter.Builder createRestAdapterBuilder() {
        return new RestAdapter.Builder().setEndpoint(getServerUrl()).setConverter(getConverter());
    }

    protected abstract Converter createConverter();

    protected final Converter getConverter() {
        if (mConverter == null) {
            mConverter = createConverter();
        }

        return mConverter;
    }

    @SuppressWarnings("unchecked")
    protected <T> T getRetrofitService(Class<T> serviceClass) {
        T service = (T) retrofitInterfaceToServiceMap.get(serviceClass);
        if (service == null) {
            service = restAdapter.create(serviceClass);
            retrofitInterfaceToServiceMap.put(serviceClass, service);
        }
        return service;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void addRequest(CachedSpiceRequest<?> request, Set<RequestListener<?>> listRequestListener) {
        if (request.getSpiceRequest() instanceof RetrofitSpiceRequest) {
            RetrofitSpiceRequest retrofitSpiceRequest = (RetrofitSpiceRequest) request.getSpiceRequest();
            retrofitSpiceRequest.setService(getRetrofitService(retrofitSpiceRequest.getRetrofitedInterfaceClass()));
        }
        super.addRequest(request, listRequestListener);
    }

    public final List<Class<?>> getRetrofitInterfaceList() {
        return retrofitInterfaceList;
    }

    protected void addRetrofitInterface(Class<?> serviceClass) {
        retrofitInterfaceList.add(serviceClass);
    }
}
