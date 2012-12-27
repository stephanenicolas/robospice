package com.octo.android.robospice.sample.ui.spicelist.network;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.octo.android.robospice.JacksonSpringAndroidSpiceService;

/**
 * 
 * @author jva
 * 
 */
public class ListTweetSpiceService extends JacksonSpringAndroidSpiceService {

    /** Timeout when calling a web service (in ms). */
    private static final int WEBSERVICES_TIMEOUT = 30000;

    @Override
    public RestTemplate createRestTemplate() {
        RestTemplate restTemplate = super.createRestTemplate();

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setReadTimeout( WEBSERVICES_TIMEOUT );
        httpRequestFactory.setConnectTimeout( WEBSERVICES_TIMEOUT );
        restTemplate.setRequestFactory( httpRequestFactory );

        return restTemplate;
    }

}
