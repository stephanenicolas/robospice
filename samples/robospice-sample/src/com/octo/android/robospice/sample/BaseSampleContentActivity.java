package com.octo.android.robospice.sample;

import roboguice.activity.RoboActivity;

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
public class BaseSampleContentActivity extends RoboActivity {
    private SpiceManager contentManagerJson = new SpiceManager( SampleJsonPersistenceRestContentService.class );
    private SpiceManager contentManagerOrmlite = new SpiceManager( SampleOrmlitePersistenceRestContentService.class );

    @Override
    protected void onStart() {
        contentManagerJson.start( this );
        contentManagerOrmlite.start( this );
        super.onStart();
    }

    @Override
    protected void onStop() {
        contentManagerJson.shouldStop();
        contentManagerOrmlite.shouldStop();
        super.onStop();
    }

    public SpiceManager getJsonContentManager() {
        return contentManagerJson;
    }

    public SpiceManager getOrmLiteContentManager() {
        return contentManagerOrmlite;
    }
}
