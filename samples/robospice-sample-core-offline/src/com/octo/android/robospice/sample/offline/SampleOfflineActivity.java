package com.octo.android.robospice.sample.offline;

import java.io.InputStream;

import roboguice.util.temp.Ln;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;

public class SampleOfflineActivity extends BaseSampleContentActivity {

    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================

    private MandelbrotView mandelbrotview;

    // ============================================================================================
    // ACITVITY LIFE CYCLE
    // ============================================================================================

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );
        mandelbrotview = (MandelbrotView) findViewById( R.id.mandelbrotview );
        mandelbrotview.setSpiceManager( getSpiceManager() );
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    // ============================================================================================
    // INNER CLASSES
    // ============================================================================================

    public final class ImageRequestListener implements RequestListener< InputStream >, RequestProgressListener {

        @Override
        public void onRequestFailure( SpiceException spiceException ) {
            Toast.makeText( SampleOfflineActivity.this, "failure", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onRequestSuccess( final InputStream result ) {
            Bitmap bitmap = BitmapFactory.decodeStream( result );
            BitmapDrawable drawable = new BitmapDrawable( bitmap );
            Toast.makeText( SampleOfflineActivity.this, "success", Toast.LENGTH_SHORT ).show();
            mandelbrotview.setBackgroundDrawable( drawable );
        }

        @Override
        public void onRequestProgressUpdate( RequestProgress progress ) {
            Ln.d( "Binary progress : %s = %d", progress.getStatus(), Math.round( 100 * progress.getProgress() ) );
        }
    }

}
