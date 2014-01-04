package com.octo.android.robospice;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.springandroid.test.SpringAndroidTestService;
import com.octo.android.robospice.springandroid.test.model.json.Weather;
import com.octo.android.robospice.springandroid.test.stub.RequestListenerStub;
import com.octo.android.robospice.springandroid.test.stub.SpringAndroidSpiceRequestStub;

//Thanks to http://stackoverflow.com/questions/2300029/servicetestcaset-getservice
@SmallTest
public class SpringAndroidSpiceServiceTest extends ServiceTestCase<SpringAndroidTestService> {

    private static final int REQUEST_COMPLETION_TIMEOUT = 1000;
    private static final long SMALL_THREAD_SLEEP = 50;
    private SpiceManager spiceManager;

    public SpringAndroidSpiceServiceTest() {
        super(SpringAndroidTestService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Thread.sleep(SMALL_THREAD_SLEEP);
        spiceManager = new SpiceManager(SpringAndroidTestService.class);
    }

    @Override
    protected void tearDown() throws Exception {
        shutdownService();
        if (spiceManager.isStarted()) {
            spiceManager.shouldStopAndJoin(REQUEST_COMPLETION_TIMEOUT);
        }
        super.tearDown();
    }

    public void test_createRequestFactory_returns_default_factory() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SpringAndroidTestService.class);
        startService(startIntent);
        assertNotNull(getService().createRestTemplate());
    }

    public void test_addRequest_injects_request_factory() throws InterruptedException {
        // given
        spiceManager.start(getContext());
        SpringAndroidSpiceRequestStub springAndroidSpiceRequest = new SpringAndroidSpiceRequestStub(Weather.class);

        // when
        spiceManager.execute(springAndroidSpiceRequest, new RequestListenerStub<Weather>());
        springAndroidSpiceRequest.await(REQUEST_COMPLETION_TIMEOUT);

        // test
        assertNotNull(springAndroidSpiceRequest.getRestTemplate());
    }
}
