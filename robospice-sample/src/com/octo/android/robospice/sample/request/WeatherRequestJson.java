package com.octo.android.robospice.sample.request;

import org.springframework.web.client.RestClientException;

import roboguice.util.Ln;

import com.octo.android.robospice.request.springandroid.RestContentRequest;
import com.octo.android.robospice.sample.model.json.WeatherResult;

public final class WeatherRequestJson extends RestContentRequest< WeatherResult > {

    private String baseUrl;

    public WeatherRequestJson( String zipCode ) {
        super( WeatherResult.class );
        this.baseUrl = String.format( "http://www.myweather2.com/developer/forecast.ashx?uac=AQmS68n6Ku&query=%s&output=json", zipCode );
    }

    @Override
    public WeatherResult loadDataFromNetwork() throws RestClientException {
        Ln.d( "Call web service " + baseUrl );
        return getRestTemplate().getForObject( baseUrl, WeatherResult.class );
    }

}
