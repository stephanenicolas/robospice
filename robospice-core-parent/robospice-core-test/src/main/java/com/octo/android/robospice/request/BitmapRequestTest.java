package com.octo.android.robospice.request;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;

import android.graphics.Bitmap;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.octo.android.robospice.core.test.R;
import com.octo.android.robospice.request.simple.BitmapRequest;

/**
 * This test is a good example of how easy it is to test RoboSpice requests.
 * Test is synchronous.
 * @author sni
 */
@LargeTest
public class BitmapRequestTest extends InstrumentationTestCase {

    private static final int TEST_BITMAP_HEIGHT = 36;
    private static final int TEST_BITMAP_WIDTH = 36;
    private static final int TEST_BITMAP_REDUCED_WIDTH = 18;
    private static final int TEST_BITMAP_REDUCED_HEIGHT = 18;
    private MockWebServer mockWebServer;
    private File cacheFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // http://stackoverflow.com/q/6516441/693752
        getInstrumentation().waitForIdleSync();
        mockWebServer = new MockWebServer();
        cacheFile = new File(getInstrumentation().getContext().getCacheDir(), "test");
        cacheFile.delete();
    }

    @Override
    protected void tearDown() throws Exception {
        mockWebServer.shutdown();
        cacheFile.delete();
        super.tearDown();
    }

    public void test_loadDataFromNetwork_returns_a_bitmap() throws Exception {
        // given;
        byte[] data = IOUtils.toByteArray(getInstrumentation().getContext().getResources().openRawResource(R.raw.binary));
        mockWebServer.enqueue(new MockResponse().setBody(data));
        mockWebServer.play();

        BitmapRequest binaryRequest = new BitmapRequest(mockWebServer.getUrl("/").toString(), null, cacheFile);
        Bitmap bitmapReturned = binaryRequest.loadDataFromNetwork();
        InputStream cacheInputStream = new FileInputStream(cacheFile);

        // then
        assertTrue(IOUtils.contentEquals(cacheInputStream, getInstrumentation().getContext().getResources().openRawResource(R.raw.binary)));
        assertEquals(TEST_BITMAP_WIDTH, bitmapReturned.getWidth());
        assertEquals(TEST_BITMAP_HEIGHT, bitmapReturned.getHeight());
    }

    public void test_loadDataFromNetwork_returns_a_bitmap_with_right_size() throws Exception {
        // given;
        byte[] data = IOUtils.toByteArray(getInstrumentation().getContext().getResources().openRawResource(R.raw.binary));
        mockWebServer.enqueue(new MockResponse().setBody(data));
        mockWebServer.play();

        BitmapRequest binaryRequest = new BitmapRequest(mockWebServer.getUrl("/").toString(), TEST_BITMAP_REDUCED_WIDTH, TEST_BITMAP_REDUCED_HEIGHT, cacheFile);
        Bitmap bitmapReturned = binaryRequest.loadDataFromNetwork();
        InputStream cacheInputStream = new FileInputStream(cacheFile);

        // then
        assertTrue(IOUtils.contentEquals(cacheInputStream, getInstrumentation().getContext().getResources().openRawResource(R.raw.binary)));
        assertEquals(TEST_BITMAP_REDUCED_WIDTH, bitmapReturned.getWidth());
        assertEquals(TEST_BITMAP_REDUCED_HEIGHT, bitmapReturned.getHeight());
    }

    public void test_loadDataFromNetwork_throws_exception() throws Exception {
        // given;
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.SC_NOT_FOUND));
        mockWebServer.play();

        // when
        BitmapRequest binaryRequest = new BitmapRequest(mockWebServer.getUrl("/").toString(), null, cacheFile);

        try {
            Bitmap bitmapReturned = binaryRequest.loadDataFromNetwork();

            // expected exception
            fail();
        } catch (FileNotFoundException e) {
            // success
            return;
        }
    }
}
