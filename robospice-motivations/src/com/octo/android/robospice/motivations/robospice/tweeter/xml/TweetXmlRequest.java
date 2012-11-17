package com.octo.android.robospice.motivations.robospice.tweeter.xml;

import android.util.Log;

import com.octo.android.robospice.motivations.model.tweeter.xml.Feed;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

public class TweetXmlRequest extends SpringAndroidSpiceRequest< Feed > {

    private long delay;

    public TweetXmlRequest( long delay ) {
        super( Feed.class );
        this.delay = delay;
    }

    @Override
    public Feed loadDataFromNetwork() throws Exception {
        if ( delay != 0 ) {
            Log.d( "request", "delaying loading from network" );
            try {
                Thread.sleep( delay );
            } catch ( InterruptedException e ) {
                Log.e( "request", "Exception while delaying request", e );
            }
        }
        Log.d( "request", "loading from network" );
        return getRestTemplate().getForObject( "http://search.twitter.com/search.atom?q=android&rpp=20", Feed.class );
    }
}