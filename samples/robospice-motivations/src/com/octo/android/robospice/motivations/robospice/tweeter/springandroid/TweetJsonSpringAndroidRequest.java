package com.octo.android.robospice.motivations.robospice.tweeter.springandroid;

import android.util.Log;

import com.octo.android.robospice.motivations.model.tweeter.json.ListTweets;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public class TweetJsonSpringAndroidRequest extends SpringAndroidSpiceRequest< ListTweets > {

    private long delay;

    public TweetJsonSpringAndroidRequest( long delay ) {
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
        return getRestTemplate().getForObject( "http://search.twitter.com/search.json?q=android&rpp=20", ListTweets.class );
    }
}