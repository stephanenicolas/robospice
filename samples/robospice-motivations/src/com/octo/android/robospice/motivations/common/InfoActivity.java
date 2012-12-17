package com.octo.android.robospice.motivations.common;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.webkit.WebView;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.octo.android.robospice.motivations.R;

@ContentView(R.layout.activity_info)
public class InfoActivity extends RoboSherlockFragmentActivity {

    public static final String BUNDLE_KEY_INFO_FILE_NAME = "BUNDLE_KEY_INFO_FILE_NAME";

    @InjectView(R.id.webView_explanation)
    private WebView webViewExplanation;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        webViewExplanation.loadUrl( "file:///android_asset/" + getIntent().getStringExtra( BUNDLE_KEY_INFO_FILE_NAME ) );
    }
}
