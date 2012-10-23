package com.octo.android.robospice.request.simple;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import roboguice.util.temp.Ln;

import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;
import com.octo.android.robospice.request.SpiceRequest;

/**
 * Downloads small images in size. All data is passed to the listener using memory. This class is meant to help download
 * small images (like thumbnails). If you wish to download bigger documents (or if you don't know the size of your
 * documents), you would be better using {@link BigBinaryRequest}.
 * 
 * @author sni
 * 
 */
public class SmallBinaryRequest extends SpiceRequest< InputStream > {

    protected String url;

    public SmallBinaryRequest( String url ) {
        super( InputStream.class );
        this.url = url;
    }

    @Override
    public final InputStream loadDataFromNetwork() throws Exception {
        try {
            final HttpURLConnection httpURLConnection = (HttpURLConnection) new URL( url ).openConnection();
            InputSupplier< InputStream > supplier = new InputSupplier< InputStream >() {

                public InputStream getInput() throws IOException {
                    return httpURLConnection.getInputStream();
                }
            };
            long total = httpURLConnection.getContentLength();
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ByteStreams.readBytes( supplier, new ProgressByteProcessor( bos, total ) );
            byte[] bytes = bos.toByteArray();
            return new ByteArrayInputStream( bytes );
        } catch ( MalformedURLException e ) {
            Ln.e( e, "Unable to create image URL" );
            return null;
        } catch ( IOException e ) {
            Ln.e( e, "Unable to download image" );
            return null;
        }
    }

    protected final String getUrl() {
        return this.url;
    }

}
