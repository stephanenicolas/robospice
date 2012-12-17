package com.octo.android.robospice.sample.ormlite;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.SimpleXmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.app.Application;

import com.octo.android.robospice.SpringAndroidSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.ormlite.InDatabaseObjectPersisterFactory;
import com.octo.android.robospice.persistence.ormlite.RoboSpiceDatabaseHelper;
import com.octo.android.robospice.sample.ormlite.model.Curren_weather;
import com.octo.android.robospice.sample.ormlite.model.Day;
import com.octo.android.robospice.sample.ormlite.model.Forecast;
import com.octo.android.robospice.sample.ormlite.model.Night;
import com.octo.android.robospice.sample.ormlite.model.Weather;
import com.octo.android.robospice.sample.ormlite.model.Wind;

/**
 * Simple service
 * 
 * @author sni
 * 
 */
public class SampleSpiceService extends SpringAndroidSpiceService {

    private static final int WEBSERVICES_TIMEOUT = 10000;

    @Override
    public CacheManager createCacheManager( Application application ) {
        CacheManager cacheManager = new CacheManager();
        List< Class< ? >> classCollection = new ArrayList< Class< ? >>();

        // add persisted classes to class collection
        classCollection.add( Weather.class );
        classCollection.add( Curren_weather.class );
        classCollection.add( Day.class );
        classCollection.add( Forecast.class );
        classCollection.add( Night.class );
        classCollection.add( Wind.class );

        // init
        RoboSpiceDatabaseHelper databaseHelper = new RoboSpiceDatabaseHelper( application, "sample_database.db", 1 );
        InDatabaseObjectPersisterFactory inDatabaseObjectPersisterFactory = new InDatabaseObjectPersisterFactory( application, databaseHelper, classCollection );
        cacheManager.addPersister( inDatabaseObjectPersisterFactory );
        return cacheManager;
    }

    @Override
    public RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // set timeout for requests

        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setReadTimeout( WEBSERVICES_TIMEOUT );
        httpRequestFactory.setConnectTimeout( WEBSERVICES_TIMEOUT );
        restTemplate.setRequestFactory( httpRequestFactory );

        // web services support xml responses
        SimpleXmlHttpMessageConverter simpleXmlHttpMessageConverter = new SimpleXmlHttpMessageConverter();
        FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        final List< HttpMessageConverter< ? >> listHttpMessageConverters = restTemplate.getMessageConverters();

        listHttpMessageConverters.add( simpleXmlHttpMessageConverter );
        listHttpMessageConverters.add( formHttpMessageConverter );
        listHttpMessageConverters.add( stringHttpMessageConverter );
        restTemplate.setMessageConverters( listHttpMessageConverters );
        return restTemplate;
    }

}
