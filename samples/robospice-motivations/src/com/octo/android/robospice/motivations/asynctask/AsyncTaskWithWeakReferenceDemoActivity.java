package com.octo.android.robospice.motivations.asynctask;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.octo.android.robospice.motivations.R;
import com.octo.android.robospice.motivations.common.DemoActivity;

/**
 * Based on http://stackoverflow.com/questions/3357477/is-asynctask-really-conceptually
 * -flawed-or-am-i-just-missing-something
 * 
 * @author sni
 * 
 */
public class AsyncTaskWithWeakReferenceDemoActivity extends DemoActivity {
    private static final int MAX_COUNT = 100;

    private WeakReference< AsyncTaskCounter > asyncTask;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        asyncTask = (WeakReference< AsyncTaskCounter >) getLastCustomNonConfigurationInstance();
        if ( asyncTask != null && asyncTask.get() != null ) {
            asyncTask.get().mActivity = new WeakReference< AsyncTaskWithWeakReferenceDemoActivity >( this );
        }

    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return asyncTask;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( asyncTask != null && asyncTask.get() != null ) {
            asyncTask.get().mActivity = null;
        }
    }

    static class AsyncTaskCounter extends AsyncTask< Void, Integer, Void > {
        WeakReference< AsyncTaskWithWeakReferenceDemoActivity > mActivity;

        AsyncTaskCounter( AsyncTaskWithWeakReferenceDemoActivity activity ) {
            mActivity = new WeakReference< AsyncTaskWithWeakReferenceDemoActivity >( activity );
        }

        private static final int SLEEP_TIME = 200;

        @Override
        protected Void doInBackground( Void... params ) {
            for ( int i = 0; i < MAX_COUNT && !isCancelled(); i++ ) {
                try {
                    Thread.sleep( SLEEP_TIME );
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
                Log.d( getClass().getSimpleName(), "Progress value is " + i );
                Log.d( getClass().getSimpleName(), "getActivity is " + mActivity );
                Log.d( getClass().getSimpleName(), "this is " + this );

                publishProgress( i );
            }
            return null;
        }

        @Override
        protected void onProgressUpdate( Integer... values ) {
            super.onProgressUpdate( values );
            if ( mActivity != null && mActivity.get() != null ) {
                mActivity.get().progressBar.setProgress( values[ 0 ] );
            }
        }
    }

    @Override
    public void startDemo() {
        AsyncTaskCounter task = new AsyncTaskCounter( this );
        task.execute();
        asyncTask = new WeakReference< AsyncTaskCounter >( task );
    }

    @Override
    public void stopDemo() {
        if ( asyncTask != null && asyncTask.get() != null ) {
            asyncTask.get().cancel( true );
        }
    }

    @Override
    public String getDemoTitle() {
        return getString( R.string.text_async_task_example );
    }

    @Override
    public String getDemoSubtitle() {
        return getString( R.string.text_async_task_weakreference_name );
    }

    @Override
    public String getDemoExplanation() {
        return "async_task_weak_reference.html";
    }

}
