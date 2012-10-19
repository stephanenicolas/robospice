package com.octo.android.robospice.sample.request;

import org.springframework.web.client.RestClientException;

import android.util.Log;

import com.octo.android.robospice.request.springandroid.RestContentRequest;
import com.octo.android.robospice.sample.model.xml.Weather;

public final class WeatherRequestXml extends RestContentRequest< Weather > {

    private String baseUrl;

    public WeatherRequestXml( String zipCode ) {
        super( Weather.class );
        this.baseUrl = String.format( "http://www.myweather2.com/developer/forecast.ashx?uac=AQmS68n6Ku&query=%s&output=xml", zipCode );
    }

    @Override
    public Weather loadDataFromNetwork() throws RestClientException {
        Log.d( getClass().getName(), "Call web service " + baseUrl );
        return getRestTemplate().getForObject( baseUrl, Weather.class );
    }

}
