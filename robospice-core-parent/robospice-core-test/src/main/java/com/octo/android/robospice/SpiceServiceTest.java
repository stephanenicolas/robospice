package com.octo.android.robospice;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.core.test.SpiceTestService;
import com.octo.android.robospice.priority.PriorityThreadPoolExecutor;

//Thanks to http://stackoverflow.com/questions/2300029/servicetestcaset-getservice
@SmallTest
public class SpiceServiceTest extends ServiceTestCase<SpiceTestService> {

    public SpiceServiceTest() {
        super(SpiceTestService.class);
    }

    public void test_service_not_null() {
        // given

        // when
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SpiceTestService.class);
        startService(startIntent);

        // then
        assertNotNull(getService());
    }

    public void test_service_is_bindable() {
        // given

        // when
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SpiceTestService.class);
        IBinder service = bindService(startIntent);

        // test
        assertNotNull(service);
    }

    public void testGetExecutorService_defaults() {
        // given

        // when
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SpiceTestService.class);
        startService(startIntent);

        // then
        assertTrue(getService().getExecutorService() instanceof PriorityThreadPoolExecutor);
        PriorityThreadPoolExecutor executorService = (PriorityThreadPoolExecutor) getService().getExecutorService();

        assertEquals(getService().getThreadCount(), executorService.getCorePoolSize());

        assertEquals(getService().getThreadPriority(), executorService.getThreadFactory().newThread(null).getPriority());

    }
}
