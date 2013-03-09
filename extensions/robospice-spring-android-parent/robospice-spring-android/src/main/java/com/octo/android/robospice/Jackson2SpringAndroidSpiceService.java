package com.octo.android.robospice;

import java.util.List;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.app.Application;

import com.octo.android.robospice.persistence.CacheManager;

/**
 * A {@link SpringAndroidSpiceService} dedicated to json web services via Jackson. Provides caching.
 * @author sni
 */
public class Jackson2SpringAndroidSpiceService extends SpringAndroidSpiceService {
    @Override
    public CacheManager createCacheManager(Application application) {
        CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister(new com.octo.android.robospice.persistence.springandroid.json.jackson.JacksonObjectPersisterFactory(application));
        return cacheManager;
    }

    @Override
    public RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // web services support json responses
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        final List<HttpMessageConverter<?>> listHttpMessageConverters = restTemplate.getMessageConverters();

        listHttpMessageConverters.add(jsonConverter);
        restTemplate.setMessageConverters(listHttpMessageConverters);
        return restTemplate;
    }
}
