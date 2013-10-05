package com.octo.android.robospice;

import java.util.Set;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * This class offers a {@link SpiceService} that injects a
 * {@link HttpRequestFactory} from Google Http Client into every
 * {@link GoogleHttpClientSpiceRequest} it has to execute. Developpers can
 * override {@link #createRequestFactory()} in addition to the usual
 * {@link #createCacheManager(android.app.Application)} methods to create a
 * {@link HttpRequestFactory} and configure it.
 * @author sni
 */
public abstract class GoogleHttpClientSpiceService extends SpiceService {

    protected HttpRequestFactory httpRequestFactory;

    @Override
    public void onCreate() {
        super.onCreate();
        httpRequestFactory = createRequestFactory();
    }

    public static HttpRequestFactory createRequestFactory() {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        return httpTransport.createRequestFactory();
    }

    @Override
    public void addRequest(CachedSpiceRequest<?> request, Set<RequestListener<?>> listRequestListener) {
        if (request.getSpiceRequest() instanceof GoogleHttpClientSpiceRequest) {
            ((GoogleHttpClientSpiceRequest<?>) request.getSpiceRequest()).setHttpRequestFactory(httpRequestFactory);
        }
        super.addRequest(request, listRequestListener);
    }

}
