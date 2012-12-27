package com.octo.android.robospice.sample.googlehttpclient;

import java.io.IOException;

import roboguice.util.temp.Ln;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.jackson.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;
import com.octo.android.robospice.sample.googlehttpclient.model.json.WeatherResult;

public class SampleSpiceRequest extends GoogleHttpClientSpiceRequest< WeatherResult > {

    private String baseUrl;

    public SampleSpiceRequest( String zipCode ) {
        super( WeatherResult.class );
        this.baseUrl = String.format( "http://www.myweather2.com/developer/forecast.ashx?uac=AQmS68n6Ku&query=%s&output=json", zipCode );
    }

    @Override
    public WeatherResult loadDataFromNetwork() throws IOException {
        Ln.d( "Call web service " + baseUrl );
        HttpRequest request = getHttpRequestFactory()//
                .buildGetRequest( new GenericUrl( baseUrl ) );
        request.setParser( new JacksonFactory().createJsonObjectParser() );
        return request.execute().parseAs( getResultType() );
    }

}
