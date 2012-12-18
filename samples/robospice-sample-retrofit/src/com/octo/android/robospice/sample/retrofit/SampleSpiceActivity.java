package com.octo.android.robospice.sample.retrofit;

import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.sample.retrofit.model.ListTweet;

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

    private SampleRetrofitSpiceRequest weatherRequest;

    // ============================================================================================
    // ACTIVITY LIFE CYCLE
    // ============================================================================================

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        requestWindowFeature( Window.FEATURE_PROGRESS );
        setContentView( R.layout.main );

        mLoremTextView = (TextView) findViewById( R.id.textview_lorem_ipsum );

        weatherRequest = new SampleRetrofitSpiceRequest( "horse_ebooks" );
    }

    @Override
    protected void onStart() {
        super.onStart();

        setProgressBarIndeterminate( false );
        setProgressBarVisibility( true );

        getSpiceManager().execute( weatherRequest, "json", DurationInMillis.ONE_MINUTE, new ListTweetRequestListener() );
    }

    // ============================================================================================
    // INNER CLASSES
    // ============================================================================================

    public final class ListTweetRequestListener implements RequestListener< ListTweet > {

        @Override
        public void onRequestFailure( SpiceException spiceException ) {
            Toast.makeText( SampleSpiceActivity.this, "failure", Toast.LENGTH_SHORT ).show();
        }

        @Override
        public void onRequestSuccess( final ListTweet result ) {
            Toast.makeText( SampleSpiceActivity.this, "success", Toast.LENGTH_SHORT ).show();
            String originalText = getString( R.string.textview_text );
            mLoremTextView.setText( originalText + result.get( 0 ).getText() );
        }
    }

}
