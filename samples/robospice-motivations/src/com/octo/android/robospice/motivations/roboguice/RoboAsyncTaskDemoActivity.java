package com.octo.android.robospice.motivations.roboguice;

import roboguice.util.RoboAsyncTask;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.octo.android.robospice.motivations.R;
import com.octo.android.robospice.motivations.common.DemoActivity;

/**
 * Basic asynchronous job implementation demo class. It presents usage of a simple {@link AsyncTask}. It will display
 * progress of the task.
 * 
 * 
 * @author sni
 * 
 */
public class RoboAsyncTaskDemoActivity extends DemoActivity {

    private AsyncTaskCounter asyncTask;

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
        return getString( R.string.text_roboasynctask_name );
    }

    @Override
    public String getDemoExplanation() {
        return "robo_async_task.html";
    }

    public static class AsyncTaskCounter extends RoboAsyncTask< Void > {

        protected AsyncTaskCounter( Context context ) {
            super( context );
        }

        private static final int SLEEP_TIME = 200;

        @Override
        public Void call() throws Exception {
            for ( int i = 0; i < MAX_COUNT; i++ ) {
                try {
                    Thread.sleep( SLEEP_TIME );
                } catch ( InterruptedException e ) {
                    e.printStackTrace();
                }
                Log.d( getClass().getSimpleName(), "Progress value is " + i );
                Log.d( getClass().getSimpleName(), "getActivity is " + getContext() );
                Log.d( getClass().getSimpleName(), "this is " + this );

                ( (RoboAsyncTaskDemoActivity) getContext() ).progressBar.setProgress( i );
            }
            return null;
        }

        @Override
        protected void onSuccess( Void t ) throws Exception {
            super.onSuccess( t );
            ( (RoboAsyncTaskDemoActivity) getContext() ).progressBar.setProgress( MAX_COUNT );
        }
    }
}
