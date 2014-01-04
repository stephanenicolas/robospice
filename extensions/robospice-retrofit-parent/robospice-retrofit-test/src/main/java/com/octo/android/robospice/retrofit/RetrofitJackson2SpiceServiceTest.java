package com.octo.android.robospice.retrofit;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.retrofit.test.RetrofitJackson2SpiceTestService;
import com.octo.android.robospice.retrofit.test.model.WeatherResult;
import com.octo.android.robospice.retrofit.test.stub.RequestListenerStub;
import com.octo.android.robospice.retrofit.test.stub.RetrofitSpiceRequestStub;

@SmallTest
public class RetrofitJackson2SpiceServiceTest extends ServiceTestCase<RetrofitJackson2SpiceTestService> {

    private static final int REQUEST_COMPLETION_TIMEOUT = 1000;
    private static final long SMALL_THREAD_SLEEP = 50;
    private SpiceManager spiceManager;

    public RetrofitJackson2SpiceServiceTest() {
        super(RetrofitJackson2SpiceTestService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Thread.sleep(SMALL_THREAD_SLEEP);
        spiceManager = new SpiceManager(RetrofitJackson2SpiceTestService.class);
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
        startIntent.setClass(getContext(), RetrofitJackson2SpiceTestService.class);
        startService(startIntent);
        assertNotNull(getService().createRestAdapterBuilder());
    }

    public void test_addRequest_injects_request_factory() throws InterruptedException {
        // given
        spiceManager.start(getContext());
        RetrofitSpiceRequestStub retrofitSpiceRequestStub = new RetrofitSpiceRequestStub(WeatherResult.class);

        // when
        spiceManager.execute(retrofitSpiceRequestStub, new RequestListenerStub<WeatherResult>());
        retrofitSpiceRequestStub.await(REQUEST_COMPLETION_TIMEOUT);

        // test
        assertNotNull(retrofitSpiceRequestStub.getService());
    }
}
