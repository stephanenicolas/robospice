package com.octo.android.robospice;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.core.test.SpiceTestService;
import com.octo.android.robospice.priority.PriorityThreadPoolExecutor;

import java.util.concurrent.TimeUnit;

//Thanks to http://stackoverflow.com/questions/2300029/servicetestcaset-getservice
@SmallTest
public class SpiceServiceTest extends ServiceTestCase<SpiceTestService> {

    public SpiceServiceTest() {
        super(SpiceTestService.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SpiceService.setIsJunit(true);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        SpiceService.setIsJunit(false);
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
        final PriorityThreadPoolExecutor executorService =
                (PriorityThreadPoolExecutor) getService().getExecutorService();

        assertEquals(getService().getCoreThreadCount(), executorService.getCorePoolSize());
        assertEquals(getService().getMaximumThreadCount(), executorService.getMaximumPoolSize());
        assertEquals(getService().getThreadPriority(), executorService.getThreadFactory()
                .newThread(null).getPriority());
        assertEquals(getService().getKeepAliveTime(),
                executorService.getKeepAliveTime(TimeUnit.NANOSECONDS));
    }

    public void testGetExecutorService_corethread_defaults() {
        // given

        // when
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SpiceTestService.class);
        startService(startIntent);

        // then
        assertTrue(getService().getExecutorService() instanceof PriorityThreadPoolExecutor);
        final PriorityThreadPoolExecutor executorService =
                (PriorityThreadPoolExecutor) getService().getExecutorService();

        assertEquals(getService().getCoreThreadCount(), executorService.getCorePoolSize());
        assertEquals(getService().getMaximumThreadCount(), executorService.getMaximumPoolSize());
        assertEquals(getService().getThreadPriority(), executorService.getThreadFactory()
                .newThread(null).getPriority());
        assertEquals(getService().getKeepAliveTime(),
                executorService.getKeepAliveTime(TimeUnit.NANOSECONDS));
    }

    public void testStops_shutsdown_executor_service() {
        // given

        // when
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SpiceTestService.class);
        bindService(startIntent);
        shutdownService();

        // then
        assertTrue(getService().getRequestProcessor().isStopped());
    }
}
