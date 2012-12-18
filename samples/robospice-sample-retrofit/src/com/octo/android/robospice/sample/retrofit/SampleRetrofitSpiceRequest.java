package com.octo.android.robospice.sample.retrofit;

import javax.inject.Named;

import retrofit.http.GET;
import retrofit.http.Server;
import roboguice.util.temp.Ln;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.octo.android.robospice.sample.retrofit.model.ListTweet;

public class SampleRetrofitSpiceRequest extends RetrofitSpiceRequest< ListTweet > {

    private final static String BASE_URL = "https://api.twitter.com/1/";
    private String userName;

    public SampleRetrofitSpiceRequest( String userName ) {
        super( ListTweet.class );
        this.userName = userName;
    }

    @Override
    public ListTweet loadDataFromNetwork() {
        Ln.d( "Call web service " + BASE_URL );

        // Create an instance of our Twitter API interface.
        Twitter twitter = getRestAdapterBuilder()//
                .setServer( new Server( BASE_URL ) )//
                .build()//
                .create( Twitter.class );

        return twitter.tweets( userName );
    }

    public interface Twitter {
        @GET("statuses/user_timeline.json")
        ListTweet tweets( @Named("screen_name") String user );
    }
}
