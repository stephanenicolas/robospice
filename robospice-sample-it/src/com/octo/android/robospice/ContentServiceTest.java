package com.octo.android.robospice;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.sample.SampleJsonPersistenceRestContentService;

//Thanks to http://stackoverflow.com/questions/2300029/servicetestcaset-getservice
@SmallTest
public class ContentServiceTest extends ServiceTestCase< SampleJsonPersistenceRestContentService > {

    public ContentServiceTest() {
        super( SampleJsonPersistenceRestContentService.class );
    }

    public void testServiceNotNull() {
        Intent startIntent = new Intent();
        startIntent.setClass( getContext(), SampleJsonPersistenceRestContentService.class );
        startService( startIntent );
        assertNotNull( getService() );
    }

    public void testBindable() {
        Intent startIntent = new Intent();
        startIntent.setClass( getContext(), SampleJsonPersistenceRestContentService.class );
        IBinder service = bindService( startIntent );
        assertNotNull( service );
    }

}
