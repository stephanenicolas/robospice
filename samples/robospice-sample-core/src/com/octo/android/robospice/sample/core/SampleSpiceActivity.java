package com.octo.android.robospice.sample.core;

import java.io.File;
import java.io.InputStream;

import roboguice.util.temp.Ln;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.simple.BigBinaryRequest;
import com.octo.android.robospice.request.simple.SimpleTextRequest;

/**
 * This sample demonstrates how to use RoboSpice to perform simple network requests.
 * 
 * @author sni
 * 
 */
public class SampleSpiceActivity extends BaseSampleSpiceActivity {

    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================

    private TextView mLoremTextView;
    private TextView mImageTextView;

    private SimpleTextRequest loremRequest;
    private BigBinaryRequest imageRequest;

    // ============================================================================================
    // ACTIVITY LIFE CYCLE
    // ============================================================================================

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_PROGRESS );
        setContentView( R.layout.main );

        mLoremTextView = (TextView) findViewById( R.id.textview_lorem_ipsum );
        mImageTextView = (TextView) findViewById( R.id.textview_image );

        loremRequest = new SimpleTextRequest( "http://www.loremipsum.de/downloads/original.txt" );
        File cacheFile = new File( getApplication().getCacheDir(), "earth.jpg" );
        imageRequest = new BigBinaryRequest( "http://earthobservatory.nasa.gov/blogs/elegantfigures/files/2011/10/globe_west_2048.jpg", cacheFile );
    }

    @Override
    protected void onStart() {
        super.onStart();

        setProgressBarIndeterminate( false );
        setProgressBarVisibility( true );

        getSpiceManager().execute( loremRequest, "txt", DurationInMillis.ONE_MINUTE, new LoremRequestListener() );
        getSpiceManager().execute( imageRequest, "image", DurationInMillis.ONE_MINUTE, new ImageRequestListener() );
    }

    // ============================================================================================
    // INNER CLASSES
    // ============================================================================================

    public final class LoremRequestListener implements RequestListener< String > {

        @Override
        public void onRequestFailure( SpiceException spiceException ) {
            Toast.makeText( SampleSpiceActivity.this, "failure", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onRequestSuccess( final String result ) {
            Toast.makeText( SampleSpiceActivity.this, "success", Toast.LENGTH_SHORT ).show();
            String originalText = getString( R.string.textview_text );
            mLoremTextView.setText( originalText + result );
        }
    }

    public final class ImageRequestListener implements RequestListener< InputStream >, RequestProgressListener {

        @Override
        public void onRequestFailure( SpiceException spiceException ) {
            Toast.makeText( SampleSpiceActivity.this, "failure", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onRequestSuccess( final InputStream result ) {
            Bitmap bitmap = BitmapFactory.decodeStream( result );
            BitmapDrawable drawable = new BitmapDrawable( bitmap );
            Toast.makeText( SampleSpiceActivity.this, "success", Toast.LENGTH_SHORT ).show();
            mImageTextView.setBackgroundDrawable( drawable );
            mImageTextView.setText( "" );
            setProgressBarVisibility( false );
            setProgressBarIndeterminateVisibility( false );
        }

        @Override
        public void onRequestProgressUpdate( RequestProgress progress ) {
            switch ( progress.getStatus() ) {
                case LOADING_FROM_NETWORK:
                    setProgressBarIndeterminate( false );
                    setProgress( (int) ( progress.getProgress() * 10000 ) );
                    break;
                default:
                    break;
            }
            Ln.d( "Binary progress : %s = %d", progress.getStatus(), Math.round( 100 * progress.getProgress() ) );
        }
    }

}
