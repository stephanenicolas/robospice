package com.octo.android.robospice.motivations.robospice.tweeter.googlehttpjavaclient;

import android.util.Log;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.jackson.JacksonFactory;
import com.octo.android.robospice.motivations.model.tweeter.json.ListTweets;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

public class TweetJsonGoogleHttpClientRequest extends GoogleHttpClientSpiceRequest< ListTweets > {

    private long delay;

    public TweetJsonGoogleHttpClientRequest( long delay ) {
        super( ListTweets.class );
        this.delay = delay;
    }

    @Override
    public ListTweets loadDataFromNetwork() throws Exception {
        if ( delay != 0 ) {
            Log.d( "request", "delaying loading from network" );
            try {
                Thread.sleep( delay );
            } catch ( InterruptedException e ) {
                Log.e( "request", "Exception while delaying request", e );
            }
        }
        Log.d( "request", "loading from network" );

        HttpRequest request = getHttpRequestFactory()//
                .buildGetRequest( new GenericUrl( "http://search.twitter.com/search.json?q=android&rpp=20" ) );
        request.setParser( new JacksonFactory().createJsonObjectParser() );
        return request.execute().parseAs( ListTweets.class );
    }
}