package com.octo.android.robospice.motivations.loader;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.octo.android.robospice.motivations.R;
import com.octo.android.robospice.motivations.common.BaseActivity;

/**
 * A simple ListActivity that display Tweets that contain the word Android in them.
 * 
 * @author Neil Goodman
 * 
 */
@ContentView(R.layout.activity_tweet_demo)
public class TwitterLoaderActivity extends BaseActivity implements LoaderCallbacks< RestLoader.RESTResponse > {
    private final static String BUNDLE_KEY_INDETERMINATE_PROGRESS_VISIBLE = "BUNDLE_KEY_INDETERMINATE_PROGRESS_VISIBLE";

    private static final String TAG = TwitterLoaderActivity.class.getName();

    private static final int LOADER_TWITTER_SEARCH = 0x1;

    private static final String ARGS_URI = "net.neilgoodman.android.restloadertutorial.ARGS_URI";
    private static final String ARGS_PARAMS = "net.neilgoodman.android.restloadertutorial.ARGS_PARAMS";
    private static final int SIZE_OF_BUFFER_SO_SIMULATE_OUT_OF_MEMORY = 1000000;
    private byte[] bufferToFillMemoryFaster = new byte[ SIZE_OF_BUFFER_SO_SIMULATE_OUT_OF_MEMORY ];

    private ArrayAdapter< String > mAdapter;

    @InjectView(R.id.listView_tweets)
    private ListView listView;

    @InjectView(R.id.webView_explanation)
    protected WebView webViewExplanation;

    @InjectView(R.id.button_start)
    protected Button buttonStart;

    @InjectView(R.id.button_cancel)
    protected Button buttonCancel;

    @InjectView(R.id.textView_memory)
    protected TextView textViewMemory;

    @InjectView(R.id.checkbox_delay)
    private CheckBox checkBoxDelay;

    private static final int REQUEST_DELAY = 10 * 1000;
    private boolean taskProgressVisibility;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );

        super.onCreate( savedInstanceState );
        getSupportLoaderManager();

        mAdapter = new ArrayAdapter< String >( this, R.layout.view_item_white );

        // Let's set our list adapter to a simple ArrayAdapter.
        listView.setAdapter( mAdapter );
        getSupportActionBar().setTitle( getDemoTitle() );
        getSupportActionBar().setSubtitle( getDemoSubtitle() );
        webViewExplanation.loadUrl( "file:///android_asset/" + getDemoExplanation() );
        setTaskProgressVisible( savedInstanceState != null && savedInstanceState.getBoolean( BUNDLE_KEY_INDETERMINATE_PROGRESS_VISIBLE, false ) );

        ActivityManager activityManager = (ActivityManager) getSystemService( ACTIVITY_SERVICE );
        MemoryInfo mi = new MemoryInfo();
        activityManager.getMemoryInfo( mi );
        bufferToFillMemoryFaster = new byte[ (int) Math.max( mi.availMem / 100, SIZE_OF_BUFFER_SO_SIMULATE_OUT_OF_MEMORY ) ];
        Log.v( getClass().getSimpleName(), "Keeping buffer in memory, size= " + bufferToFillMemoryFaster.length );
        textViewMemory.setText( getString( R.string.text_available_memory, mi.availMem / 1024 ) );

        startDemo();
    }

    @Override
    protected void onSaveInstanceState( Bundle outState ) {
        outState.putBoolean( BUNDLE_KEY_INDETERMINATE_PROGRESS_VISIBLE, this.taskProgressVisibility );
        super.onSaveInstanceState( outState );
    }

    public void setTaskProgressVisible( boolean visible ) {
        setProgressBarIndeterminateVisibility( visible );
        taskProgressVisibility = visible;
    }

    @Override
    public Loader< RestLoader.RESTResponse > onCreateLoader( int id, Bundle args ) {
        if ( args != null && args.containsKey( ARGS_URI ) && args.containsKey( ARGS_PARAMS ) ) {
            Uri action = args.getParcelable( ARGS_URI );
            Bundle params = args.getParcelable( ARGS_PARAMS );

            long delay = checkBoxDelay.isChecked() ? REQUEST_DELAY : 0;
            RestLoader loader = new RestLoader( this, RestLoader.HTTPVerb.GET, action, params );
            loader.setDelay( delay );
            setTaskProgressVisible( true );
            return loader;
        }

        return null;
    }

    @Override
    public void onLoadFinished( Loader< RestLoader.RESTResponse > loader, RestLoader.RESTResponse data ) {
        setTaskProgressVisible( false );

        int code = data.getCode();
        String json = data.getData();

        // Check to see if we got an HTTP 200 code and have some data.
        if ( code == 200 && !json.equals( "" ) ) {

            // For really complicated JSON decoding I usually do my heavy lifting
            // Gson and proper model classes, but for now let's keep it simple
            // and use a utility method that relies on some of the built in
            // JSON utilities on Android.
            List< String > tweets = getTweetsFromJson( json );

            // Load our list adapter with our Tweets.
            mAdapter.clear();
            for ( String tweet : tweets ) {
                mAdapter.add( tweet );
            }
        } else {
            Toast.makeText( this, "Failed to load Twitter data. Check your internet settings.", Toast.LENGTH_SHORT ).show();
        }
    }

    @Override
    public void onLoaderReset( Loader< RestLoader.RESTResponse > loader ) {
        setTaskProgressVisible( false );
    }

    private static List< String > getTweetsFromJson( String json ) {
        ArrayList< String > tweetList = new ArrayList< String >();

        try {
            JSONObject tweetsWrapper = (JSONObject) new JSONTokener( json ).nextValue();
            JSONArray tweets = tweetsWrapper.getJSONArray( "results" );

            for ( int i = 0; i < tweets.length(); i++ ) {
                JSONObject tweet = tweets.getJSONObject( i );
                tweetList.add( tweet.getString( "text" ) );
            }
        } catch ( JSONException e ) {
            Log.e( TAG, "Failed to parse JSON.", e );
        }

        return tweetList;
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getSupportMenuInflater().inflate( R.menu.activity_demo, menu );
        return true;
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
    public void startDemo() {
        // This is our REST action.
        Uri twitterSearchUri = Uri.parse( "http://search.twitter.com/search.json" );
        // Here we are going to place our REST call parameters. Note that
        // we could have just used Uri.Builder and appendQueryParameter()
        // here, but I wanted to illustrate how to use the Bundle params.
        Bundle params = new Bundle();
        params.putString( "q", "android" );

        // These are the loader arguments. They are stored in a Bundle because
        // LoaderManager will maintain the state of our Loaders for us and
        // reload the Loader if necessary. This is the whole reason why
        // we have even bothered to implement RestLoader.
        Bundle args = new Bundle();
        args.putParcelable( ARGS_URI, twitterSearchUri );
        args.putParcelable( ARGS_PARAMS, params );

        // Initialize the Loader.
        getSupportLoaderManager().initLoader( LOADER_TWITTER_SEARCH, args, this );
    }

    @Override
    public void stopDemo() {
        getSupportLoaderManager().destroyLoader( LOADER_TWITTER_SEARCH );

    }

    @Override
    public String getDemoTitle() {
        return getString( R.string.text_networking_example );
    }

    @Override
    public String getDemoSubtitle() {
        return getString( R.string.text_loader_rest_name );
    }

    @Override
    public String getDemoExplanation() {
        return "loader_rest.html";
    }
}