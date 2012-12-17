package com.octo.android.robospice.sample.offline;

import android.app.Activity;

import com.octo.android.robospice.SpiceManager;

/**
 * This class is the base class of all activities of the sample project.
 * 
 * Typically, in a new project, you will have to create a base class like this one and copy the content of the
 * {@link BaseSampleContentActivity} into your own class.
 * 
 * @author sni
 * 
 */
public class BaseSampleContentActivity extends Activity {
    private SpiceManager contentManagerJson = new SpiceManager( SampleOfflineSpiceService.class );

    @Override
    protected void onStart() {
        contentManagerJson.start( this );
        super.onStart();
    }

    @Override
    protected void onStop() {
        contentManagerJson.shouldStop();
        super.onStop();
    }

    public SpiceManager getSpiceManager() {
        return contentManagerJson;
    }

}
