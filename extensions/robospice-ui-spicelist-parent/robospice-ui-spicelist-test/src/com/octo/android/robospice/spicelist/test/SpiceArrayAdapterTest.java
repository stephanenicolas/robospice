package com.octo.android.robospice.spicelist.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.test.InstrumentationTestCase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.google.mockwebserver.RecordedRequest;
import com.octo.android.robospice.request.simple.BigBinaryRequest;
import com.octo.android.robospice.spicelist.BigBinarySpiceManager;
import com.octo.android.robospice.spicelist.SpiceArrayAdapter;
import com.octo.android.robospice.spicelist.SpiceListItemView;

public class SpiceArrayAdapterTest extends InstrumentationTestCase {

    private File cacheFile;

    private MockWebServer mockWebServer;

    private SpiceArrayAdapterUnderTest adapter;
    private BigBinarySpiceManager spiceManager;
    private DataUnderTest data1;
    private DataUnderTest data2;
    private ArrayList< DataUnderTest > data;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cacheFile = new File( getInstrumentation().getTargetContext().getCacheDir(), "Foo" );
        cacheFile.delete();
        mockWebServer = new MockWebServer();

        spiceManager = new BigBinarySpiceManager();
        spiceManager.start( getInstrumentation().getTargetContext() );

        data = new ArrayList< DataUnderTest >();

        data1 = new DataUnderTest();
        data1.foo = "data1";
        data1.imageUrl = "data1.png";

        data2 = new DataUnderTest();
        data2.foo = "data2";
        data2.imageUrl = "data2.png";

        data.add( data1 );
        data.add( data2 );
        adapter = new SpiceArrayAdapterUnderTest( getInstrumentation().getTargetContext(), spiceManager, data );
    }

    @Override
    protected void tearDown() throws Exception {
        mockWebServer.shutdown();
        super.tearDown();
    }

    public void testGetItem_at_position_0() {
        assertEquals( "data1 was expected.", data1.foo, adapter.getItem( 0 ).foo );
    }

    public void testGetItemId_at_position_0() {
        assertEquals( "Wrong ID.", 0, adapter.getItemId( 0 ) );
    }

    public void testGetCount() {
        assertEquals( "Contacts amount incorrect.", data.size(), adapter.getCount() );
    }

    // I have 3 views on my adapter, name, number and photo
    public void testGetView() throws NotFoundException, IOException, InterruptedException {
        // given;
        byte[] data = IOUtils.toByteArray( getInstrumentation().getContext().getResources().openRawResource( R.raw.binary ) );
        mockWebServer.enqueue( new MockResponse().setBody( data ) );
        mockWebServer.play();

        View view = adapter.getView( 0, null, null );

        TextView nameView = (TextView) view.findViewById( R.id.user_name_textview );
        ImageView photoView = (ImageView) view.findViewById( R.id.thumbnail_imageview );

        // On this part you will have to test it with your own views/data
        // then
        assertNotNull( "View is null. ", view );
        assertNotNull( "Name TextView is null. ", nameView );
        assertNotNull( "Photo ImageView is null. ", photoView );

        assertEquals( "Names doesn't match.", data1.foo, nameView.getText() );

        // could we get notified of this request ?
        Thread.sleep( 5000 );
        assertEquals( 1, mockWebServer.getRequestCount() );
        RecordedRequest first = mockWebServer.takeRequest();
        assertEquals( "GET /" + data1.imageUrl + " HTTP/1.1", first.getRequestLine() );

        InputStream cacheInputStream = new FileInputStream( cacheFile );
        assertTrue( IOUtils.contentEquals( cacheInputStream, getInstrumentation().getContext().getResources().openRawResource( R.raw.binary ) ) );

    }

    private class DataUnderTest {
        public String foo;
        public String imageUrl;

    }

    private class SpiceArrayAdapterUnderTest extends SpiceArrayAdapter< DataUnderTest > {

        private List< DataUnderTest > data;

        public SpiceArrayAdapterUnderTest( Context context, BigBinarySpiceManager spiceManagerBinary, List< DataUnderTest > data ) {
            super( context, spiceManagerBinary );
            this.data = data;
        }

        @Override
        public BigBinaryRequest createRequest( DataUnderTest data ) {
            return new BigBinaryRequest( mockWebServer.getUrl( "/" + data.imageUrl ).toString(), cacheFile );
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public DataUnderTest getItem( int position ) {
            return data.get( position );
        }

        @SuppressWarnings("unchecked")
        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {
            View view;

            DataUnderTest currentData = data.get( position );
            if ( convertView != null ) {
                view = convertView;
            } else {
                view = new StubbedView( getContext() );
            }
            ( (StubbedView) view ).setDataUnderTest( currentData );
            // this is the most important line. It will update views automatically
            // ----------------------------------------
            update( currentData, (SpiceListItemView< DataUnderTest >) view );
            // ----------------------------------------
            return view;
        }
    }

    private class StubbedView extends RelativeLayout implements SpiceListItemView< DataUnderTest > {

        private DataUnderTest dataUnderTest;
        private TextView userNameTextView;
        private ImageView thumbImageView;

        public StubbedView( Context context ) {
            super( context );
            LayoutInflater.from( context ).inflate( R.layout.view_cell_tweet, this );

            this.userNameTextView = (TextView) this.findViewById( R.id.user_name_textview );
            this.thumbImageView = (ImageView) this.findViewById( R.id.thumbnail_imageview );
        }

        public void setDataUnderTest( DataUnderTest dataUnderTest ) {
            this.dataUnderTest = dataUnderTest;
            userNameTextView.setText( dataUnderTest.foo );
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

}
