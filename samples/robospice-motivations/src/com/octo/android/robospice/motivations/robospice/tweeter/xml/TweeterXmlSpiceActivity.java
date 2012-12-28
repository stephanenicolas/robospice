package com.octo.android.robospice.motivations.robospice.tweeter.xml;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.motivations.R;
import com.octo.android.robospice.motivations.common.BaseActivity;
import com.octo.android.robospice.motivations.model.tweeter.xml.Entry;
import com.octo.android.robospice.motivations.model.tweeter.xml.Feed;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;

/**
 * A simple ListActivity that display Tweets that contain the word Android in them.
 * 
 * @author sni
 * 
 */
@ContentView(R.layout.activity_tweet_demo)
public class TweeterXmlSpiceActivity extends BaseActivity {

    private static final String XML_CACHE_KEY = "tweets_xml";
    private static final int REQUEST_DELAY = 10 * 1000;
    private static final int SIZE_OF_BUFFER_SO_SIMULATE_OUT_OF_MEMORY = 1000000;
    private byte[] bufferToFillMemoryFaster = new byte[ SIZE_OF_BUFFER_SO_SIMULATE_OUT_OF_MEMORY ];

    private ArrayAdapter< String > mAdapter;

    @InjectView(R.id.listView_tweets)
    private ListView listView;

    @InjectView(R.id.webView_explanation)
    protected WebView webViewExplanation;

    @InjectView(R.id.textView_request_progress)
    protected TextView textViewProgress;

    @InjectView(R.id.textView_memory)
    protected TextView textViewMemory;

    @InjectView(R.id.checkbox_delay)
    private CheckBox checkBoxDelay;

    private SpiceManager spiceManager = new SpiceManager( TweeterXmlSpiceService.class );
    private TweetXmlRequest tweetXmlRequest;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        mAdapter = new ArrayAdapter< String >( this, R.layout.view_item_white );

        // Let's set our list adapter to a simple ArrayAdapter.
        listView.setAdapter( mAdapter );
        getSupportActionBar().setTitle( getDemoTitle() );
        getSupportActionBar().setSubtitle( getDemoSubtitle() );
        webViewExplanation.loadUrl( "file:///android_asset/" + getDemoExplanation() );

        ActivityManager activityManager = (ActivityManager) getSystemService( ACTIVITY_SERVICE );
        MemoryInfo mi = new MemoryInfo();
        activityManager.getMemoryInfo( mi );
        bufferToFillMemoryFaster = new byte[ (int) Math.max( mi.availMem / 100, SIZE_OF_BUFFER_SO_SIMULATE_OUT_OF_MEMORY ) ];
        Log.v( getClass().getSimpleName(), "Keeping buffer in memory, size= " + bufferToFillMemoryFaster.length );
        textViewMemory.setText( getString( R.string.text_available_memory, mi.availMem / 1024 ) );
    }

    // TODO service should stop when there is no more request and no bound activities.

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start( this );
        spiceManager.addListenerIfPending( Feed.class, XML_CACHE_KEY, new TweetRequestListener() );
        spiceManager.getFromCache( Feed.class, XML_CACHE_KEY, DurationInMillis.ALWAYS, new TweetRequestListener() );
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void startDemo() {
        long delay = checkBoxDelay.isChecked() ? REQUEST_DELAY : 0;
        tweetXmlRequest = new TweetXmlRequest( delay );
        spiceManager.execute( tweetXmlRequest, XML_CACHE_KEY, DurationInMillis.NEVER, new TweetRequestListener() );
    }

    @Override
    public void stopDemo() {
        if ( tweetXmlRequest != null ) {
            tweetXmlRequest.cancel();
        }
    }

    private class TweetRequestListener implements RequestListener< Feed >, RequestProgressListener {

        @Override
        public void onRequestFailure( SpiceException arg0 ) {
            if ( !( arg0 instanceof RequestCancelledException ) ) {
                Toast.makeText( TweeterXmlSpiceActivity.this, "Failed to load Twitter data.", Toast.LENGTH_SHORT ).show();
            }
        }

        @Override
        public void onRequestSuccess( Feed feed ) {

            if ( feed == null ) {
                return;
            }
            // Toast.makeText( RestSpiceActivity.this, "Success to load Twitter data.", Toast.LENGTH_SHORT ).show();
            mAdapter.clear();
            for ( Entry entry : feed.getListEntry() ) {
                mAdapter.add( entry.getTitle() );
            }
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onRequestProgressUpdate( RequestProgress progress ) {
            String status = convertProgressToString( progress );
            textViewProgress.setText( status );
        }

    }

    @Override
    public void onStartButtonClick( View v ) {
        startDemo();
    }

    @Override
    public void onCancelButtonClick( View v ) {
        stopDemo();
    }

    @Override
    public String getDemoTitle() {
        return getString( R.string.text_networking_example );
    }

    @Override
    public String getDemoSubtitle() {
        return getString( R.string.text_spice_rest_xml_name );
    }

    @Override
    public String getDemoExplanation() {
        return "spice_rest_ormlite.html";
    }

    private String convertProgressToString( RequestProgress progress ) {
        String status = "";
        switch ( progress.getStatus() ) {
            case READING_FROM_CACHE:
                status = "? cache -->";
                break;
            case LOADING_FROM_NETWORK:
                status = "^ network ^";
                break;
            case WRITING_TO_CACHE:
                status = "--> cache";
                break;

            default:
                status = "";
                break;

        }
        return status;
    }

}
