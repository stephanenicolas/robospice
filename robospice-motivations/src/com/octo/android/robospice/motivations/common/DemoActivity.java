package com.octo.android.robospice.motivations.common;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.octo.android.robospice.motivations.R;

@ContentView(R.layout.activity_demo)
public abstract class DemoActivity extends BaseActivity {

    public static final int MAX_COUNT = 100;
    private static final int SIZE_OF_BUFFER_SO_SIMULATE_OUT_OF_MEMORY = 1000000;
    private byte[] bufferToFillMemoryFaster = new byte[ SIZE_OF_BUFFER_SO_SIMULATE_OUT_OF_MEMORY ];

    @InjectView(R.id.webView_explanation)
    protected WebView webViewExplanation;

    @InjectView(R.id.progressBar)
    protected ProgressBar progressBar;

    @InjectView(R.id.button_start)
    protected Button buttonStart;

    @InjectView(R.id.button_cancel)
    protected Button buttonCancel;

    @InjectView(R.id.textView_memory)
    protected TextView textViewMemory;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setProgressBarIndeterminate( true );
        getSupportActionBar().setTitle( getDemoTitle() );
        getSupportActionBar().setSubtitle( getDemoSubtitle() );
        progressBar.setMax( MAX_COUNT );
        webViewExplanation.loadUrl( "file:///android_asset/" + getDemoExplanation() );

        ActivityManager activityManager = (ActivityManager) getSystemService( ACTIVITY_SERVICE );
        MemoryInfo mi = new MemoryInfo();
        activityManager.getMemoryInfo( mi );
        bufferToFillMemoryFaster = new byte[ (int) Math.max( mi.availMem / 100, SIZE_OF_BUFFER_SO_SIMULATE_OUT_OF_MEMORY ) ];
        Log.v( getClass().getSimpleName(), "Keeping buffer in memory, size= " + bufferToFillMemoryFaster.length );
        textViewMemory.setText( getString( R.string.text_available_memory, mi.availMem / 1024 ) );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getSupportMenuInflater().inflate( R.menu.activity_demo, menu );
        return true;
    }

}