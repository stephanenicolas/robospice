package com.octo.android.robospice;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.googlehttpclient.test.GoogleHttpClientSpiceTestService;
import com.octo.android.robospice.googlehttpclient.test.model.Weather;
import com.octo.android.robospice.googlehttpclient.test.stub.GoogleHttpClientSpiceRequestStub;
import com.octo.android.robospice.googlehttpclient.test.stub.RequestListenerStub;

//Thanks to http://stackoverflow.com/questions/2300029/servicetestcaset-getservice
@SmallTest
public class GoogleHttpClientSpiceServiceTest extends
    ServiceTestCase<GoogleHttpClientSpiceTestService> {

    private static final int REQUEST_COMPLETION_TIMEOUT = 1000;
    private SpiceManager spiceManager;

    public GoogleHttpClientSpiceServiceTest() {
        super(GoogleHttpClientSpiceTestService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        spiceManager = new SpiceManager(GoogleHttpClientSpiceTestService.class);
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
        startIntent.setClass(getContext(),
            GoogleHttpClientSpiceTestService.class);
        startService(startIntent);
        assertNotNull(getService().createRequestFactory());
    }

    public void test_addRequest_injects_request_factory()
        throws InterruptedException {
        // given
        spiceManager.start(getContext());
        GoogleHttpClientSpiceRequestStub googleHttpClientSpiceRequest = new GoogleHttpClientSpiceRequestStub(
            Weather.class);

        // when
        spiceManager.execute(googleHttpClientSpiceRequest,
            new RequestListenerStub<Weather>());
        googleHttpClientSpiceRequest.await(REQUEST_COMPLETION_TIMEOUT);

        // test
        assertNotNull(googleHttpClientSpiceRequest.getHttpRequestFactory());
    }
}
