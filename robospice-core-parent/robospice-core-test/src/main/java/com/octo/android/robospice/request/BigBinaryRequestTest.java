package com.octo.android.robospice.request;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.octo.android.robospice.core.test.R;
import com.octo.android.robospice.request.simple.BigBinaryRequest;

/**
 * This test is a good example of how easy it is to test RoboSpice requests.
 * Test is synchronous.
 * @author sni
 */
@LargeTest
public class BigBinaryRequestTest extends AndroidTestCase {

    private MockWebServer mockWebServer;
    private File cacheFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockWebServer = new MockWebServer();
        cacheFile = new File(getContext().getCacheDir(), "test");
        cacheFile.delete();
    }

    @Override
    protected void tearDown() throws Exception {
        mockWebServer.shutdown();
        cacheFile.delete();
        super.tearDown();
    }

    public void test_loadDataFromNetwork_returns_a_binary() throws Exception {
        // given;
        byte[] data = IOUtils.toByteArray(getContext().getResources().openRawResource(R.raw.binary));
        mockWebServer.enqueue(new MockResponse().setBody(data));
        mockWebServer.play();

        BigBinaryRequest binaryRequest = new BigBinaryRequest(mockWebServer.getUrl("/").toString(), cacheFile);
        InputStream binaryReturned = binaryRequest.loadDataFromNetwork();
        InputStream cacheInputStream = new FileInputStream(cacheFile);

        // then
        assertTrue(IOUtils.contentEquals(binaryReturned, getContext().getResources().openRawResource(R.raw.binary)));
        assertTrue(IOUtils.contentEquals(cacheInputStream, getContext().getResources().openRawResource(R.raw.binary)));
    }
}
