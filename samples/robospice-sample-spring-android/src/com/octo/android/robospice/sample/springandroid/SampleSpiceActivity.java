package com.octo.android.robospice.sample.springandroid;

import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.sample.springandroid.model.json.WeatherResult;

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

    private SampleSpiceRequest weatherRequest;

    // ============================================================================================
    // ACTIVITY LIFE CYCLE
    // ============================================================================================

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_PROGRESS );
        setContentView( R.layout.main );

        mLoremTextView = (TextView) findViewById( R.id.textview_lorem_ipsum );

        weatherRequest = new SampleSpiceRequest( "75000" );
    }

    @Override
    protected void onStart() {
        super.onStart();

        setProgressBarIndeterminate( false );
        setProgressBarVisibility( true );

        getSpiceManager().execute( weatherRequest, "json", DurationInMillis.ONE_MINUTE, new WeatherRequestListener() );
    }

    // ============================================================================================
    // INNER CLASSES
    // ============================================================================================

    public final class WeatherRequestListener implements RequestListener< WeatherResult > {

        @Override
        public void onRequestFailure( SpiceException spiceException ) {
            Toast.makeText( SampleSpiceActivity.this, "failure", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onRequestSuccess( final WeatherResult result ) {
            Toast.makeText( SampleSpiceActivity.this, "success", Toast.LENGTH_SHORT ).show();
            String originalText = getString( R.string.textview_text );
            mLoremTextView.setText( originalText + result.getWeather().getCurren_weather().get( 0 ).getTemp() );
        }
    }

}
