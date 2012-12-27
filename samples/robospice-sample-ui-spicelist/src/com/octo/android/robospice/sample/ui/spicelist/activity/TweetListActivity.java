package com.octo.android.robospice.sample.ui.spicelist.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.sample.ui.spicelist.R;
import com.octo.android.robospice.sample.ui.spicelist.adapter.ListTweetArrayAdapter;
import com.octo.android.robospice.sample.ui.spicelist.model.ListTweets;
import com.octo.android.robospice.sample.ui.spicelist.network.ListTweetSpiceService;
import com.octo.android.robospice.sample.ui.spicelist.network.TweetsRequest;
import com.octo.android.robospice.spicelist.BigBinarySpiceManager;

public class TweetListActivity extends Activity {

    private ListView octoListView;
    private View loadingView;

    private ListTweetArrayAdapter listTweetArrayAdapter;

    private SpiceManager spiceManagerJson = new SpiceManager( ListTweetSpiceService.class );
    private BigBinarySpiceManager spiceManagerBinary = new BigBinarySpiceManager();

    // --------------------------------------------------------------------------------------------
    // ACTIVITY LIFECYCLE
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
        setProgressBarIndeterminateVisibility( false );
        setContentView( R.layout.activity_tweetlist );

        octoListView = (ListView) findViewById( R.id.listview_octos );
        loadingView = findViewById( R.id.loading_layout );
    }

    @Override
    public void onStart() {
        super.onStart();
        spiceManagerJson.start( this );
        spiceManagerBinary.start( this );

        loadListTweets();
    }

    @Override
    public void onStop() {
        spiceManagerJson.shouldStop();
        spiceManagerBinary.shouldStop();
        super.onStop();
    }

    // --------------------------------------------------------------------------------------------
    // PRIVATE
    // --------------------------------------------------------------------------------------------

    private void updateListViewContent( ListTweets listTweets ) {
        listTweetArrayAdapter = new ListTweetArrayAdapter( this, spiceManagerBinary, listTweets );
        octoListView.setAdapter( listTweetArrayAdapter );

        loadingView.setVisibility( View.GONE );
        octoListView.setVisibility( View.VISIBLE );
    }

    private void loadListTweets() {
        setProgressBarIndeterminateVisibility( true );

        spiceManagerJson.execute( new TweetsRequest( "android" ), "tweets", DurationInMillis.ONE_SECOND * 10, new OctosListListener() );
    }

    // --------------------------------------------------------------------------------------------
    // PRIVATE
    // --------------------------------------------------------------------------------------------

    private class OctosListListener implements RequestListener< ListTweets > {

        @Override
        public void onRequestFailure( SpiceException spiceException ) {
            setProgressBarIndeterminateVisibility( false );
            Toast.makeText( TweetListActivity.this, "Impossible to get the list of tweets", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onRequestSuccess( ListTweets result ) {
            setProgressBarIndeterminateVisibility( false );
            updateListViewContent( result );
        }
    }

}
