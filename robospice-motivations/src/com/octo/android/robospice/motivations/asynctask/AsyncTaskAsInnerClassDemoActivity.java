package com.octo.android.robospice.motivations.asynctask;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.octo.android.robospice.motivations.R;
import com.octo.android.robospice.motivations.common.DemoActivity;

/**
 * Basic asynchronous job implementation demo class. It presents usage of a simple {@link AsyncTask}. It will display
 * progress of the task.
 * 
 * @author sni
 * 
 */
public class AsyncTaskAsInnerClassDemoActivity extends DemoActivity {

    private AsyncTaskCounter asyncTask;

    @Override
    public void startDemo() {
        asyncTask = new AsyncTaskCounter();
        asyncTask.execute();
    }

    @Override
    public void stopDemo() {
        if ( asyncTask != null ) {
            asyncTask.cancel( true );
        }
    }

    @Override
    public String getDemoSubtitle() {
        return getString( R.string.text_basic_async_task_name );
    }

    @Override
    public String getDemoTitle() {
        return getString( R.string.text_async_task_example );
    }

    @Override
    public String getDemoExplanation() {
        return "async_task_inner_class.html";
    }

    /**
     * Basic async task usage. Note that this class is an inner class of an Activity. Thus, it holds an invisible
     * reference on the outer class instance of this Activity. This produces a memory leak : it the asyntask lasts for
     * long, it keeps the activity alive, whereas android would like to get rid of it as it can no longer be displayed.
     * If all activities use this technique of an inner AsyncTask, then you will quickly get a memory leak as none of
     * those activities will get garbage collected by the android system. If you don't understand why activities must
     * die, please refer to the activity life cycle of the {@link Activity} documentation.
     * 
     * @author sni
     * 
     */
    public class AsyncTaskCounter extends AsyncTask< Void, Integer, Void > {

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
                Log.d( getClass().getSimpleName(), "getActivity is " + AsyncTaskAsInnerClassDemoActivity.this );
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
            progressBar.setProgress( values[ 0 ] );
        }

    }
}
