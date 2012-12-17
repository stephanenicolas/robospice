package com.octo.android.robospice.sample.googlehttpclient;

import java.util.List;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.app.Application;

import com.octo.android.robospice.SpringAndroidSpiceService;
import com.octo.android.robospice.persistence.CacheManager;

/**
 * Simple service
 * 
 * @author sni
 * 
 */
public class SampleSpiceService extends SpringAndroidSpiceService {

    @Override
    public CacheManager createCacheManager( Application application ) {
        CacheManager cacheManager = new CacheManager();
        cacheManager.addPersister( new com.octo.android.robospice.persistence.springandroid.json.jackson.JacksonObjectPersisterFactory( application ) );
        return cacheManager;
    }

    @Override
    public RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // web services support xml responses
        MappingJacksonHttpMessageConverter jsonConverter = new MappingJacksonHttpMessageConverter();
        final List< HttpMessageConverter< ? >> listHttpMessageConverters = restTemplate.getMessageConverters();

        listHttpMessageConverters.add( jsonConverter );
        restTemplate.setMessageConverters( listHttpMessageConverters );
        restTemplate.setMessageConverters( listHttpMessageConverters );
        return restTemplate;
    }
}
