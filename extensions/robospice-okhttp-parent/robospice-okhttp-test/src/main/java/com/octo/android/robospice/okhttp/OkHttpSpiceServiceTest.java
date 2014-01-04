package com.octo.android.robospice.okhttp;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.okhttp.test.OkHttpSpiceTestService;
import com.octo.android.robospice.okhttp.test.stub.OkHttpSpiceRequestStub;
import com.octo.android.robospice.okhttp.test.stub.RequestListenerStub;

//Thanks to http://stackoverflow.com/questions/2300029/servicetestcaset-getservice
@SmallTest
public class OkHttpSpiceServiceTest extends ServiceTestCase<OkHttpSpiceTestService> {

    private static final int REQUEST_COMPLETION_TIMEOUT = 1000;
    private static final long SMALL_THREAD_SLEEP = 50;
    private SpiceManager spiceManager;

    public OkHttpSpiceServiceTest() {
        super(OkHttpSpiceTestService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Thread.sleep(SMALL_THREAD_SLEEP);
        spiceManager = new SpiceManager(OkHttpSpiceTestService.class);
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
        startIntent.setClass(getContext(), OkHttpSpiceTestService.class);
        startService(startIntent);
        assertNotNull(getService().createOkHttpClient());
    }

    public void test_addRequest_injects_request_factory() throws InterruptedException {
        // given
        spiceManager.start(getContext());
        OkHttpSpiceRequestStub okHttpSpiceRequestStub = new OkHttpSpiceRequestStub();

        // when
        spiceManager.execute(okHttpSpiceRequestStub, new RequestListenerStub<String>());
        okHttpSpiceRequestStub.await(REQUEST_COMPLETION_TIMEOUT);

        // test
        assertNotNull(okHttpSpiceRequestStub.getOkHttpClient());
    }
}
