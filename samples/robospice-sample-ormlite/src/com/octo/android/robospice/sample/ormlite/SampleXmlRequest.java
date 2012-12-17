package com.octo.android.robospice.sample.ormlite;

import org.springframework.web.client.RestClientException;

import roboguice.util.temp.Ln;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import com.octo.android.robospice.sample.ormlite.model.Weather;

public class SampleXmlRequest extends SpringAndroidSpiceRequest< Weather > {

    private String baseUrl;

    public SampleXmlRequest( String zipCode ) {
        super( Weather.class );
        this.baseUrl = String.format( "http://www.myweather2.com/developer/forecast.ashx?uac=AQmS68n6Ku&query=%s&output=xml", zipCode );
    }

    @Override
    public Weather loadDataFromNetwork() throws RestClientException {
        Ln.d( "Call web service " + baseUrl );
        return getRestTemplate().getForObject( baseUrl, Weather.class );
    }

}
