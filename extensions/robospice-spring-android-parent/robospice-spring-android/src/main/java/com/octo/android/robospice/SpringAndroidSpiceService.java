package com.octo.android.robospice;

import java.util.Set;

import org.springframework.web.client.RestTemplate;

import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * This class offers a {@link SpiceService} that injects a {@link RestTemplate}
 * from spring android into every {@link SpringAndroidSpiceRequest} it has to
 * execute. Developpers will have to implement {@link #createRestTemplate()} in
 * addition to the usual {@link #createCacheManager(android.app.Application)}
 * methods to create a {@link RestTemplate} and configure it.
 * @author sni
 */
public abstract class SpringAndroidSpiceService extends SpiceService {

    private RestTemplate restTemplate;

    @Override
    public void onCreate() {
        super.onCreate();
        restTemplate = createRestTemplate();
    }

    public abstract RestTemplate createRestTemplate();

    @Override
    public void addRequest(CachedSpiceRequest<?> request, Set<RequestListener<?>> listRequestListener) {
        if (request.getSpiceRequest() instanceof SpringAndroidSpiceRequest) {
            ((SpringAndroidSpiceRequest<?>) request.getSpiceRequest()).setRestTemplate(restTemplate);
        }
        super.addRequest(request, listRequestListener);
    }

}
