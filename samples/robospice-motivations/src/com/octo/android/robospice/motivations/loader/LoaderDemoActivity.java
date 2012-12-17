package com.octo.android.robospice.motivations.loader;

import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.octo.android.robospice.motivations.R;
import com.octo.android.robospice.motivations.common.DemoFragmentActivity;

/**
 * Based on http://stackoverflow.com/questions/9077212/update-progressbar-from- asynctaskloader
 * 
 * @author sni
 * 
 */
public class LoaderDemoActivity extends DemoFragmentActivity implements LoaderCallbacks< Void > {

    @Override
    public void startDemo() {
        getSupportLoaderManager().initLoader( 0, (Bundle) null, this );
    }

    @Override
    public void stopDemo() {
        getSupportLoaderManager().destroyLoader( 0 );
    }

    @Override
    public String getDemoTitle() {
        return getString( R.string.text_loader_example );
    }

    @Override
    public String getDemoSubtitle() {
        return getString( R.string.text_loader_name );
    }

    @Override
    public String getDemoExplanation() {
        return "loader.html";
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
    }

    @Override
    public Loader< Void > onCreateLoader( int id, Bundle args ) {
        AsyncTaskLoader< Void > asyncTaskLoader = new AsyncTaskCounter( this );
        asyncTaskLoader.forceLoad();
        return asyncTaskLoader;
    }

    @Override
    public void onLoadFinished( Loader< Void > arg0, Void arg1 ) {
        progressBar.setProgress( MAX_COUNT );
        Log.d( getClass().getSimpleName(), "Loader finished" );
    }

    @Override
    public void onLoaderReset( Loader< Void > arg0 ) {
        progressBar.setProgress( 0 );
        Log.d( getClass().getSimpleName(), "Loader reset" );
    }

    static class AsyncTaskCounter extends AsyncTaskLoader< Void > {

        AsyncTaskCounter( LoaderDemoActivity activity ) {
            super( activity );
        }

        private static final int SLEEP_TIME = 200;

        @Override
        public Void loadInBackground() {
            for ( int progress = 0; progress < MAX_COUNT && !isReset(); progress++ ) {
                try {
                    Thread.sleep( SLEEP_TIME );
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
                Log.d( getClass().getSimpleName(), "Progress value is " + progress );
                Log.d( getClass().getSimpleName(), "getActivity is " + getContext() );
                Log.d( getClass().getSimpleName(), "this is " + this );

            }
            return null;
        }

    }
}
