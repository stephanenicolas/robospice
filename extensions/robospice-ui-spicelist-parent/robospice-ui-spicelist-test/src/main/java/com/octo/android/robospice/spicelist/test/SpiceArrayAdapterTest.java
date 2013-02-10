package com.octo.android.robospice.spicelist.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.google.mockwebserver.RecordedRequest;
import com.octo.android.robospice.request.simple.BitmapRequest;
import com.octo.android.robospice.spicelist.BitmapSpiceManager;
import com.octo.android.robospice.spicelist.SpiceArrayAdapter;
import com.octo.android.robospice.spicelist.SpiceListItemView;

public class SpiceArrayAdapterTest extends InstrumentationTestCase {

    private static final int ADAPTER_UPDATE_TIME_OUT = 3000;

    private File cacheFile;

    private MockWebServer mockWebServer;

    private SpiceArrayAdapterUnderTest adapter;
    private BitmapSpiceManager spiceManager;
    private DataUnderTest data1;
    private DataUnderTest data2;
    private ArrayList<DataUnderTest> data;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cacheFile = new File(getInstrumentation().getTargetContext().getCacheDir(), "Foo");
        cacheFile.delete();
        mockWebServer = new MockWebServer();

        spiceManager = new BitmapSpiceManagerUnderTest();
        spiceManager.start(getInstrumentation().getTargetContext());

        data = new ArrayList<DataUnderTest>();

        data1 = new DataUnderTest();
        data1.setFoo("data1");
        data1.setImageUrl("data1.png");

        data2 = new DataUnderTest();
        data2.setFoo("data2");
        data2.setImageUrl("data2.png");

        data.add(data1);
        data.add(data2);
        adapter = new SpiceArrayAdapterUnderTest(getInstrumentation().getTargetContext(), spiceManager, data);
    }

    @Override
    protected void tearDown() throws Exception {
        mockWebServer.shutdown();
        super.tearDown();
    }

    public void testGetItem_at_position_0() {
        assertEquals("data1 was expected.", data1.getFoo(), adapter.getItem(0).getFoo());
    }

    public void testGetItemId_at_position_0() {
        assertEquals("Wrong ID.", 0, adapter.getItemId(0));
    }

    public void testGetCount() {
        assertEquals("Contacts amount incorrect.", data.size(), adapter.getCount());
    }

    // I have 3 views on my adapter, name, number and photo
    public void testGetView_fills_list_item_view_with_data_and_executes_request() throws IOException, InterruptedException {
        // given;
        byte[] data = IOUtils.toByteArray(getInstrumentation().getContext().getResources().openRawResource(R.raw.binary));
        mockWebServer.enqueue(new MockResponse().setBody(data));
        mockWebServer.play();

        // when
        View view = adapter.getView(0, null, null);
        adapter.await(ADAPTER_UPDATE_TIME_OUT);
        assertTrue(adapter.isLoadBitmapHasBeenCalled());

        // then
        TextView nameView = (TextView) view.findViewById(R.id.user_name_textview);
        ImageView photoView = (ImageView) view.findViewById(R.id.thumbnail_imageview);

        assertNotNull("View is null. ", view);
        assertNotNull("Name TextView is null. ", nameView);
        assertNotNull("Photo ImageView is null. ", photoView);

        assertEquals("Names doesn't match.", data1.getFoo(), nameView.getText());

        // could we get notified of this request ?
        assertEquals(1, mockWebServer.getRequestCount());
        RecordedRequest first = mockWebServer.takeRequest();
        assertEquals("GET /" + data1.getImageUrl() + " HTTP/1.1", first.getRequestLine());

        InputStream cacheInputStream = new FileInputStream(cacheFile);
        assertTrue(IOUtils.contentEquals(cacheInputStream, getInstrumentation().getContext().getResources().openRawResource(R.raw.binary)));

    }

    private class DataUnderTest {
        private String foo;
        private String imageUrl;

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

    }

    private class SpiceArrayAdapterUnderTest extends SpiceArrayAdapter<DataUnderTest> {

        private ReentrantLock reentrantLock = new ReentrantLock();
        private Condition loadBitmapHasBeenCalledCondition = reentrantLock.newCondition();
        private boolean loadBitmapHasBeenCalled = false;

        public SpiceArrayAdapterUnderTest(Context context, BitmapSpiceManager spiceManagerBinary, List<DataUnderTest> data) {
            super(context, spiceManagerBinary, data);
        }

        @Override
        public BitmapRequest createRequest(DataUnderTest data, int reqWidth, int reqHeight) {
            return new BitmapRequest(mockWebServer.getUrl("/" + data.getImageUrl()).toString(), reqWidth, reqHeight, cacheFile);
        }

        // ----------------------------------------------------
        // ----- Block Test thread until drawable is refreshed.
        // ----------------------------------------------------

        @Override
        protected void loadBitmapAsynchronously(DataUnderTest octo, ImageView thumbImageView, String tempThumbnailImageFileName) {
            super.loadBitmapAsynchronously(octo, thumbImageView, tempThumbnailImageFileName);
            reentrantLock.lock();
            try {
                loadBitmapHasBeenCalled = true;
                loadBitmapHasBeenCalledCondition.signal();
            } finally {
                reentrantLock.unlock();
            }
        }

        public void await(long millisecond) throws InterruptedException {
            reentrantLock.lock();
            try {
                loadBitmapHasBeenCalledCondition.await(millisecond, TimeUnit.MILLISECONDS);
            } finally {
                reentrantLock.unlock();
            }
        }

        public boolean isLoadBitmapHasBeenCalled() {
            return loadBitmapHasBeenCalled;
        }

        @Override
        public SpiceListItemView<DataUnderTest> createView(Context context) {
            return new ListItemViewStub(getContext());
        }
    }

    private class ListItemViewStub extends RelativeLayout implements SpiceListItemView<DataUnderTest> {

        private DataUnderTest dataUnderTest;
        private TextView userNameTextView;
        private ImageView thumbImageView;

        public ListItemViewStub(Context context) {
            super(context);
            LayoutInflater.from(context).inflate(R.layout.view_cell_tweet, this);

            this.userNameTextView = (TextView) this.findViewById(R.id.user_name_textview);
            this.thumbImageView = (ImageView) this.findViewById(R.id.thumbnail_imageview);
        }

        @Override
        public void update(DataUnderTest data) {
            this.dataUnderTest = data;
            userNameTextView.setText(dataUnderTest.getFoo());
        }

        @Override
        public DataUnderTest getData() {
            return dataUnderTest;
        }

        @Override
        public ImageView getImageView() {
            return thumbImageView;
        }

    }

    /**
     * Used for testing only so that we can add a custom Service that works
     * offline for testing.
     */
    private class BitmapSpiceManagerUnderTest extends BitmapSpiceManager {

        public BitmapSpiceManagerUnderTest() {
            super(TestBigBinarySpiceService.class);
        }
    }
}
