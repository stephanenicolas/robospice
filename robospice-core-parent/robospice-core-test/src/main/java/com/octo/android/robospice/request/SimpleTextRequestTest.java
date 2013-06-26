package com.octo.android.robospice.request;

import java.io.FileNotFoundException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.octo.android.robospice.core.test.R;
import com.octo.android.robospice.request.simple.SimpleTextRequest;

/**
 * This test is a good example of how easy it is to test RoboSpice requests.
 * Test is synchronous.
 * @author sni
 */
@LargeTest
public class SimpleTextRequestTest extends InstrumentationTestCase {

    private MockWebServer mockWebServer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // http://stackoverflow.com/q/6516441/693752
        getInstrumentation().waitForIdleSync();
        mockWebServer = new MockWebServer();

    }

    @Override
    protected void tearDown() throws Exception {
        mockWebServer.shutdown();
        super.tearDown();
    }

    public void test_loadDataFromNetwork_returns_a_simple_string() throws Exception {
        // given;
        String loremIpsum = IOUtils.toString(getInstrumentation().getContext().getResources().openRawResource(R.raw.lorem_ipsum));
        mockWebServer.enqueue(new MockResponse().setBody(loremIpsum));
        mockWebServer.play();

        // when
        SimpleTextRequest loremIpsumTextRequest = new SimpleTextRequest(mockWebServer.getUrl("/").toString());
        String stringReturned = loremIpsumTextRequest.loadDataFromNetwork();

        // then
        assertTrue(stringReturned.startsWith("Lorem ipsum"));
    }

    public void test_loadDataFromNetwork_throws_exception() throws Exception {
        // given;
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.SC_NOT_FOUND));
        mockWebServer.play();

        // when
        SimpleTextRequest loremIpsumTextRequest = new SimpleTextRequest(mockWebServer.getUrl("/").toString());

        try {
            String stringReturned = loremIpsumTextRequest.loadDataFromNetwork();

            // expected exception
            fail();
        } catch (FileNotFoundException e) {
            // success
            return;
        }
    }
}
