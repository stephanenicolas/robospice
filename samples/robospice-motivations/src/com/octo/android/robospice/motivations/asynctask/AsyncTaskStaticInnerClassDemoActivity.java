package com.octo.android.robospice.motivations.asynctask;

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
public class AsyncTaskStaticInnerClassDemoActivity extends DemoActivity {
    private static final int MAX_COUNT = 100;

    private AsyncTaskCounter asyncTask;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        asyncTask = (AsyncTaskCounter) getLastCustomNonConfigurationInstance();
        if ( asyncTask != null ) {
            asyncTask.mActivity = this;
        }

    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return asyncTask;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( asyncTask != null ) {
            asyncTask.mActivity = null;
        }
    }

    static class AsyncTaskCounter extends AsyncTask< Void, Integer, Void > {
        AsyncTaskStaticInnerClassDemoActivity mActivity;

        AsyncTaskCounter( AsyncTaskStaticInnerClassDemoActivity activity ) {
            mActivity = activity;
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
        protected void onPostExecute( Void result ) {
            super.onPostExecute( result );
        }

        @Override
        protected void onProgressUpdate( Integer... values ) {
            super.onProgressUpdate( values );
            if ( mActivity != null ) {
                mActivity.progressBar.setProgress( values[ 0 ] );
            }
        }
    }

    @Override
    public void startDemo() {
        asyncTask = new AsyncTaskCounter( this );
        asyncTask.execute();
    }

    @Override
    public void stopDemo() {
        if ( asyncTask != null ) {
            asyncTask.cancel( true );
        }
    }

    @Override
    public String getDemoTitle() {
        return getString( R.string.text_async_task_example );
    }

    @Override
    public String getDemoSubtitle() {
        return getString( R.string.text_async_task_static_inner_class_name );
    }

    @Override
    public String getDemoExplanation() {
        return "async_task_static_inner_class.html";
    }

}
