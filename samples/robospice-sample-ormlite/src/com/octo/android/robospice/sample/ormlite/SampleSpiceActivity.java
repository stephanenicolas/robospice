package com.octo.android.robospice.sample.ormlite;

import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.sample.ormlite.model.Weather;

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

    private SampleXmlRequest weatherRequest;

    // ============================================================================================
    // ACTIVITY LIFE CYCLE
    // ============================================================================================

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_PROGRESS );
        setContentView( R.layout.main );

        mLoremTextView = (TextView) findViewById( R.id.textview_lorem_ipsum );

        weatherRequest = new SampleXmlRequest( "75000" );
    }

    @Override
    protected void onStart() {
        super.onStart();

        setProgressBarIndeterminate( false );
        setProgressBarVisibility( true );

        getSpiceManager().execute( weatherRequest, new Integer( 0 ), DurationInMillis.ONE_MINUTE, new WeatherRequestListener() );
    }

    // ============================================================================================
    // INNER CLASSES
    // ============================================================================================

    public final class WeatherRequestListener implements RequestListener< Weather > {

        @Override
        public void onRequestFailure( SpiceException spiceException ) {
            Toast.makeText( SampleSpiceActivity.this, "failure", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onRequestSuccess( final Weather result ) {
            Toast.makeText( SampleSpiceActivity.this, "success", Toast.LENGTH_SHORT ).show();
            String originalText = getString( R.string.textview_text );
            mLoremTextView.setText( originalText + result.getListWeather().iterator().next().getTemp() );
        }
    }

}
