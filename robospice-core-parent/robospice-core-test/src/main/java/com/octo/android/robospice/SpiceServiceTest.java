package com.octo.android.robospice;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.core.test.SpiceTestService;

//Thanks to http://stackoverflow.com/questions/2300029/servicetestcaset-getservice
@SmallTest
public class SpiceServiceTest extends ServiceTestCase<SpiceTestService> {

    public SpiceServiceTest() {
        super(SpiceTestService.class);
    }

    public void test_service_not_null() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SpiceTestService.class);
        startService(startIntent);
        assertNotNull(getService());
    }

    public void test_service_is_bindable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SpiceTestService.class);
        IBinder service = bindService(startIntent);
        assertNotNull(service);
    }

}
