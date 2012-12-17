package com.octo.android.robospice.motivations.common;

import android.view.View;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

public abstract class BaseActivity extends RoboSherlockFragmentActivity {

    public void onStartButtonClick( View v ) {
        startDemo();
    }

    public void onCancelButtonClick( View v ) {
        stopDemo();
    }

    public abstract void startDemo();

    public abstract void stopDemo();

    public abstract String getDemoTitle();

    public abstract String getDemoSubtitle();

    public abstract String getDemoExplanation();

}