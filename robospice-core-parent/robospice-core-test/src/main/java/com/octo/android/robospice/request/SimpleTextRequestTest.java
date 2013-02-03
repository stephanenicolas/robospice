package com.octo.android.robospice.request;

import org.apache.commons.io.IOUtils;

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
        mockWebServer = new MockWebServer();

    }

    @Override
    protected void tearDown() throws Exception {
        mockWebServer.shutdown();
        super.tearDown();
    }

    public void test_loadDataFromNetwork_returns_a_simple_string()
        throws Exception {
        // given;
        String loremIpsum = IOUtils.toString(getInstrumentation().getContext()
            .getResources().openRawResource(R.raw.lorem_ipsum));
        mockWebServer.enqueue(new MockResponse().setBody(loremIpsum));
        mockWebServer.play();

        // when
        SimpleTextRequest loremIpsumTextRequest = new SimpleTextRequest(
            mockWebServer.getUrl("/").toString());
        String stringReturned = loremIpsumTextRequest.loadDataFromNetwork();

        // then
        assertTrue(stringReturned.startsWith("Lorem ipsum"));
    }
}
