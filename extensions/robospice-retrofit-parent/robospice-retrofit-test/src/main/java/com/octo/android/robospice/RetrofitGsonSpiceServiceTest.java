package com.octo.android.robospice;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.retrofit.test.RetrofitSpiceTestService;
import com.octo.android.robospice.retrofit.test.model.WeatherResult;
import com.octo.android.robospice.retrofit.test.stub.RequestListenerStub;
import com.octo.android.robospice.retrofit.test.stub.RetrofitSpiceRequestStub;

//Thanks to http://stackoverflow.com/questions/2300029/servicetestcaset-getservice
@SmallTest
public class RetrofitGsonSpiceServiceTest extends ServiceTestCase< RetrofitSpiceTestService > {

    private static final int REQUEST_COMPLETION_TIMEOUT = 1000;
    private SpiceManager spiceManager;

    public RetrofitGsonSpiceServiceTest() {
        super( RetrofitSpiceTestService.class );
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        spiceManager = new SpiceManager( RetrofitSpiceTestService.class );
    }

    @Override
    protected void tearDown() throws Exception {
        shutdownService();
        if ( spiceManager.isStarted() ) {
            spiceManager.shouldStopAndJoin( REQUEST_COMPLETION_TIMEOUT );
        }
        super.tearDown();
    }

    public void test_createRequestFactory_returns_default_factory() {
        Intent startIntent = new Intent();
        startIntent.setClass( getContext(), RetrofitSpiceTestService.class );
        startService( startIntent );
        assertNotNull( getService().createRestAdapterBuilder() );
    }

    public void test_addRequest_injects_request_factory() throws InterruptedException {
        // given
        spiceManager.start( getContext() );
        RetrofitSpiceRequestStub googleHttpClientSpiceRequest = new RetrofitSpiceRequestStub( WeatherResult.class );

        // when
        spiceManager.execute( googleHttpClientSpiceRequest, new RequestListenerStub< WeatherResult >() );
        googleHttpClientSpiceRequest.await( REQUEST_COMPLETION_TIMEOUT );

        // test
        assertNotNull( googleHttpClientSpiceRequest.getRestAdapterBuilder() );
    }
}
