package com.octo.android.robospice.motivations.robospice.image;

import java.io.File;
import java.io.InputStream;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.exception.RequestCancelledException;
import com.octo.android.robospice.motivations.R;
import com.octo.android.robospice.motivations.common.BaseActivity;
import com.octo.android.robospice.motivations.common.InfoActivity;
import com.octo.android.robospice.motivations.robospice.tweeter.springandroid.TweeterJsonSpringAndroidSpiceService;
import com.octo.android.robospice.notification.SpiceNotificationService;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.simple.BigBinaryRequest;

/**
 * A simple ListActivity that display Tweets that contain the word Android in them.
 * 
 * @author Neil Goodman
 * 
 */
@ContentView(R.layout.activity_image)
public class ImageSpiceActivity extends BaseActivity {

    public static final String EARTH_IMAGE_CACHE_KEY = "earth";
    private static final int MAX_PROGRESS = 100;

    @InjectView(R.id.scrollView)
    private ScrollView scrollView;

    @InjectView(R.id.textView_empty)
    private TextView textViewEmpty;

    @InjectView(R.id.imageView_main)
    private ImageView imageView;

    @InjectView(R.id.progressBar)
    private ProgressBar progressBar;

    @InjectView(R.id.textView_memory)
    protected TextView textViewMemory;

    private SpiceManager spiceManager = new SpiceManager( TweeterJsonSpringAndroidSpiceService.class );
    private BigBinaryRequest imageRequest;

    protected long delay = 0;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        // Let's set our list adapter to a simple ArrayAdapter.

        getSupportActionBar().setTitle( getDemoTitle() );
        getSupportActionBar().setSubtitle( getDemoSubtitle() );
        progressBar.setMax( MAX_PROGRESS );

        ActivityManager activityManager = (ActivityManager) getSystemService( ACTIVITY_SERVICE );
        MemoryInfo mi = new MemoryInfo();
        activityManager.getMemoryInfo( mi );
        textViewMemory.setText( getString( R.string.text_available_memory, mi.availMem / 1024 ) );
        setImageVisible( false );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getSupportMenuInflater().inflate( R.menu.activity_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        Intent intent = new Intent( this, InfoActivity.class );
        intent.putExtra( InfoActivity.BUNDLE_KEY_INFO_FILE_NAME, "spice_image.html" );
        startActivity( intent );
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        spiceManager.start( this );

        spiceManager.addListenerIfPending( InputStream.class, EARTH_IMAGE_CACHE_KEY, new ImageRequestListener() );
        spiceManager.getFromCache( InputStream.class, EARTH_IMAGE_CACHE_KEY, DurationInMillis.ALWAYS, new ImageRequestListener() );
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void startDemo() {

        File cacheFile = new File( getCacheDir(), "earth.jpg" );
        imageRequest = new BigBinaryRequest( "http://earthobservatory.nasa.gov/blogs/elegantfigures/files/2011/10/globe_west_2048.jpg", cacheFile );
        spiceManager.execute( imageRequest, EARTH_IMAGE_CACHE_KEY, DurationInMillis.NEVER, new ImageRequestListener() );
        Intent intent = SpiceNotificationService.createIntent( this, ImageSpiceNotificationService.class, TweeterJsonSpringAndroidSpiceService.class, 70,
                InputStream.class, EARTH_IMAGE_CACHE_KEY, false );
        startService( intent );
    }

    @Override
    public void stopDemo() {
        if ( imageRequest != null ) {
            imageRequest.cancel();
        }
    }

    private void setImageVisible( boolean visible ) {
        if ( visible ) {
            textViewEmpty.setVisibility( View.GONE );
            scrollView.setVisibility( View.VISIBLE );
        } else {
            textViewEmpty.setVisibility( View.VISIBLE );
            scrollView.setVisibility( View.GONE );
        }
    }

    private class ImageRequestListener implements RequestListener< InputStream >, RequestProgressListener {

        @Override
        public void onRequestFailure( SpiceException arg0 ) {
            if ( !( arg0 instanceof RequestCancelledException ) ) {
                Toast.makeText( ImageSpiceActivity.this, "Failed to load Twitter data.", Toast.LENGTH_SHORT ).show();
                setImageVisible( false );
            }
        }

        @Override
        public void onRequestSuccess( InputStream inputStream ) {

            if ( inputStream == null ) {
                setImageVisible( false );
                return;
            }
            setImageVisible( true );

            Drawable drawable = new BitmapDrawable( getResources(), BitmapFactory.decodeStream( inputStream ) );
            imageView.setImageDrawable( drawable );
        }

        @Override
        public void onRequestProgressUpdate( RequestProgress progress ) {
            progressBar.setProgress( Math.round( progress.getProgress() * MAX_PROGRESS ) );
        }
    }

    @Override
    public void onStartButtonClick( View v ) {
        startDemo();
    }

    @Override
    public void onCancelButtonClick( View v ) {
        setImageVisible( false );
        stopDemo();
    }

    public void onClearButtonClick( View v ) {
        spiceManager.removeDataFromCache( InputStream.class, EARTH_IMAGE_CACHE_KEY );
        imageView.setImageDrawable( null );
        setImageVisible( false );
    }

    @Override
    public String getDemoTitle() {
        return getString( R.string.text_networking_example );
    }

    @Override
    public String getDemoSubtitle() {
        return getString( R.string.text_spice_image_name );
    }

    @Override
    public String getDemoExplanation() {
        return "spice_rest.html";
    }

}
