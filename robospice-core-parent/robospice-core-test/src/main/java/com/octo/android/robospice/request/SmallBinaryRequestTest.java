package com.octo.android.robospice.request;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.octo.android.robospice.core.test.R;
import com.octo.android.robospice.request.simple.SmallBinaryRequest;

/**
 * This test is a good example of how easy it is to test RoboSpice requests.
 * Test is synchronous.
 * @author sni
 */
@LargeTest
public class SmallBinaryRequestTest extends AndroidTestCase {

    private MockWebServer mockWebServer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockWebServer = new MockWebServer();
    }

    @Override
    protected void tearDown() throws Exception {
        mockWebServer.shutdown();
        super.tearDown();
    }

    public void test_loadDataFromNetwork_returns_a_small_binary() throws Exception {
        // given;
        byte[] data = IOUtils.toByteArray(getContext().getResources().openRawResource(R.raw.binary));
        mockWebServer.enqueue(new MockResponse().setBody(data));
        mockWebServer.play();

        // when
        SmallBinaryRequest binaryRequest = new SmallBinaryRequest(mockWebServer.getUrl("/").toString());
        InputStream binaryReturned = binaryRequest.loadDataFromNetwork();

        // then
        assertTrue(IOUtils.contentEquals(binaryReturned, getContext().getResources().openRawResource(R.raw.binary)));
    }

    public void test_loadDataFromNetwork_throws_exception() throws Exception {
        // given;
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.SC_NOT_FOUND));
        mockWebServer.play();

        // when
        SmallBinaryRequest binaryRequest = new SmallBinaryRequest(mockWebServer.getUrl("/").toString());

        try {
            binaryRequest.loadDataFromNetwork();

            // expected exception
            fail();
        } catch (FileNotFoundException e) {
            // success
            return;
        }
    }
}
