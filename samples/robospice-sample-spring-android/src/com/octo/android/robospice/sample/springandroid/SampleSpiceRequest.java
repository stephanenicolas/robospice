package com.octo.android.robospice.sample.springandroid;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.web.client.RestClientException;

import roboguice.util.temp.Ln;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import com.octo.android.robospice.sample.springandroid.model.json.WeatherResult;

public class SampleSpiceRequest extends SpringAndroidSpiceRequest< WeatherResult > {

    private String baseUrl;

    public SampleSpiceRequest( String zipCode ) {
        super( WeatherResult.class );
        this.baseUrl = String.format( "http://www.myweather2.com/developer/forecast.ashx?uac=AQmS68n6Ku&query=%s&output=json", zipCode );
    }

    @Override
    public WeatherResult loadDataFromNetwork() throws IOException, RestClientException, URISyntaxException {
        Ln.d( "Call web service " + baseUrl );

        return getRestTemplate().getForObject( new URI( baseUrl ), getResultType() );
    }

}
