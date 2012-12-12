package com.octo.android.robospice.motivations;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.octo.android.robospice.motivations.asynctask.AsyncTaskAsInnerClassDemoActivity;
import com.octo.android.robospice.motivations.asynctask.AsyncTaskStaticInnerClassDemoActivity;
import com.octo.android.robospice.motivations.asynctask.AsyncTaskWithWeakReferenceDemoActivity;
import com.octo.android.robospice.motivations.common.InfoActivity;
import com.octo.android.robospice.motivations.common.SectionAdapter;
import com.octo.android.robospice.motivations.loader.LoaderDemoActivity;
import com.octo.android.robospice.motivations.loader.LoaderWithProgressDemoActivity;
import com.octo.android.robospice.motivations.loader.TwitterLoaderActivity;
import com.octo.android.robospice.motivations.roboguice.RoboAsyncTaskDemoActivity;
import com.octo.android.robospice.motivations.robospice.image.ImageSpiceActivity;
import com.octo.android.robospice.motivations.robospice.tweeter.googlehttpjavaclient.TweeterJsonGoogleHttpClientSpiceActivity;
import com.octo.android.robospice.motivations.robospice.tweeter.springandroid.TweeterJsonSpringAndroidSpiceActivity;
import com.octo.android.robospice.motivations.robospice.tweeter.xml.TweeterXmlSpiceActivity;

/**
 * Main menu. Presents all tested ways to execute an asynchronous job.
 * 
 * @author sni
 * 
 */
// SOF references :
/*
 * 
 * done
 * 
 * http://stackoverflow.com/questions/12797550/android-asynctask-for-long-running-operations//
 * http://stackoverflow.com/questions/3365768/androids-asynctask-issue/13082232#13082232
 * http://stackoverflow.com/questions/10212489/android-issue-with-json-in-asynctask/13082263#13082263
 * http://stackoverflow.com/questions/12219930/asynctask-for-longer-than-a-few-seconds
 * http://stackoverflow.com/questions/2531336/asynctask-wont-stop-even-when-the-activity-has-destroyed
 * http://stackoverflow.com/questions/13214145/progressbar-and-asynctask/13214617#13214617
 * 
 * todo
 * 
 * flaws by design. Loaders way to go.
 * http://stackoverflow.com/questions/5097565/can-honeycomb-loaders-solve-problems-with-asynctask-ui-update
 * http://stackoverflow.com/questions/4285877/which-one-to-use-onsaveinstancestate-vs-onretainnonconfigurationinstance
 */
@ContentView(R.layout.activity_main)
public class MainActivity extends RoboSherlockActivity {

    @InjectView(R.id.listView_main)
    private ListView listViewMain;

    private SectionAdapter sectionAdapter;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        getSupportActionBar().setTitle( getTitle() );
        initializeListAdapter();

    }

    private void initializeListAdapter() {
        sectionAdapter = new SectionAdapter( this, R.layout.view_header, R.layout.view_item );
        sectionAdapter.addSection( "Why asynctasks are flawed" );
        sectionAdapter.addItem( getString( R.string.text_basic_async_task_name ) );
        sectionAdapter.addItem( getString( R.string.text_async_task_static_inner_class_name ) );
        sectionAdapter.addItem( getString( R.string.text_async_task_weakreference_name ) );
        sectionAdapter.addItem( getString( R.string.text_roboasynctask_name ) );
        sectionAdapter.addSection( "Why loaders don't fit networking" );
        sectionAdapter.addItem( getString( R.string.text_loader_name ) );
        sectionAdapter.addItem( getString( R.string.text_loader_with_progress_name ) );
        sectionAdapter.addItem( getString( R.string.text_loader_rest_name ) );
        sectionAdapter.addSection( "RoboSpice examples" );
        sectionAdapter.addItem( getString( R.string.text_spice_rest_name ) );
        sectionAdapter.addItem( getString( R.string.text_spice_rest_xml_name ) );
        sectionAdapter.addItem( getString( R.string.text_spice_image_name ) );
        sectionAdapter.addItem( getString( R.string.text_spice_google_http_client_name ) );

        listViewMain.setAdapter( sectionAdapter );
        listViewMain.setOnItemClickListener( new OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView< ? > arg0, View arg1, int arg2, long arg3 ) {
                displayDemo( arg2 );
            }
        } );
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getSupportMenuInflater().inflate( R.menu.activity_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        Intent intent = new Intent( this, InfoActivity.class );
        intent.putExtra( InfoActivity.BUNDLE_KEY_INFO_FILE_NAME, "info_main.html" );
        startActivity( intent );
        return true;
    }

    public void displayDemo( int demoId ) {
        Class< ? extends Activity > activityClass = null;
        switch ( demoId ) {
            case 1:
                activityClass = AsyncTaskAsInnerClassDemoActivity.class;
                break;
            case 2:
                activityClass = AsyncTaskStaticInnerClassDemoActivity.class;
                break;
            case 3:
                activityClass = AsyncTaskWithWeakReferenceDemoActivity.class;
                break;
            case 4:
                activityClass = RoboAsyncTaskDemoActivity.class;
                break;
            case 6:
                activityClass = LoaderDemoActivity.class;
                break;
            case 7:
                activityClass = LoaderWithProgressDemoActivity.class;
                break;
            case 8:
                activityClass = TwitterLoaderActivity.class;
                break;
            case 10:
                activityClass = TweeterJsonSpringAndroidSpiceActivity.class;
                break;
            case 11:
                activityClass = TweeterXmlSpiceActivity.class;
                break;
            case 12:
                activityClass = ImageSpiceActivity.class;
                break;
            case 13:
                activityClass = TweeterJsonGoogleHttpClientSpiceActivity.class;
                break;
            default:
                throw new ArrayIndexOutOfBoundsException();
        }

        startActivity( new Intent( this, activityClass ) );
    }

}
