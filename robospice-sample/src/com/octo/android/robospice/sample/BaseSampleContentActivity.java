package com.octo.android.robospice.sample;

import roboguice.activity.RoboActivity;

import com.octo.android.robospice.ContentManager;

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
    private ContentManager contentManager = new ContentManager( SampleContentService.class );

    @Override
    protected void onStart() {
        contentManager.start( this );
        super.onStart();
    }

    @Override
    protected void onStop() {
        contentManager.shouldStop();
        super.onStop();
    }

    public ContentManager getContentManager() {
        return contentManager;
    }
}
