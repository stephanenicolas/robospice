package com.octo.android.robospice;

import android.app.Activity;

/**
 * This class is more a sample than a real ready-to-use class. It shows how you
 * can build your base Activity class in your own project. Whatever super class
 * you use (sherlock, fragmentactivity, guice, etc.) you can just copy past the
 * methods below to enable all your activities to use the framework. The binding
 * can take place, at best during <a href=
 * "http://stackoverflow.com/questions/2304086/binding-to-service-in-oncreate-or-in-onresume"
 * >certain lifecycle operations </a>: {@link #onStart()} and {@link #onStop()}.
 * @author sni
 */
public class SpiceActivity extends Activity {

    private final SpiceManager spiceManager = new SpiceManager(
        SpiceService.class);

    @Override
    protected void onStart() {
        spiceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    public SpiceManager getSpiceManager() {
        return spiceManager;
    }
}
