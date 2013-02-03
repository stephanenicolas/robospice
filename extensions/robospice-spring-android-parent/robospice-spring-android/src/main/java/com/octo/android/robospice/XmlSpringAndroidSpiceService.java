package com.octo.android.robospice;

import java.util.List;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.SimpleXmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.app.Application;

import com.octo.android.robospice.persistence.CacheManager;

/**
 * A {@link SpringAndroidSpiceService} dedicated to xml web services. Provides
 * caching.
 * @author sni
 */
public class XmlSpringAndroidSpiceService extends SpringAndroidSpiceService {
    @Override
    public CacheManager createCacheManager(Application application) {
        CacheManager cacheManager = new CacheManager();
        cacheManager
            .addPersister(new com.octo.android.robospice.persistence.springandroid.xml.SimpleSerializerObjectPersisterFactory(
                application));
        return cacheManager;
    }

    @Override
    public RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // web services support xml responses
        SimpleXmlHttpMessageConverter jsonConverter = new SimpleXmlHttpMessageConverter();
        final List<HttpMessageConverter<?>> listHttpMessageConverters = restTemplate
            .getMessageConverters();

        listHttpMessageConverters.add(jsonConverter);
        restTemplate.setMessageConverters(listHttpMessageConverters);
        return restTemplate;
    }
}
