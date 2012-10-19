package com.octo.android.robospice.request.simple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;
import com.octo.android.robospice.request.SpiceRequest;

/**
 * Downloads big images in size. All data is passed to the listener using file system. This class is meant to help
 * download big images. If you wish to download smaller documents, you would be better using {@link SmallBinaryRequest}.
 * 
 * @author sni
 * 
 */
public class BigBinaryRequest extends SpiceRequest< InputStream > {

    protected String url;
    protected File cacheFile;

    public BigBinaryRequest( String url, File cacheFile ) {
        super( InputStream.class );
        this.url = url;
        this.cacheFile = cacheFile;
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
            // touch
            cacheFile.setLastModified( System.currentTimeMillis() );
            OutputStream fileOutputStream = new FileOutputStream( cacheFile );
            ByteStreams.readBytes( supplier, new ProgressByteProcessor( fileOutputStream, total ) );
            return new FileInputStream( cacheFile );
        } catch ( MalformedURLException e ) {
            Log.e( getClass().getName(), "Unable to create image URL", e );
            return null;
        } catch ( IOException e ) {
            Log.e( getClass().getName(), "Unable to download image", e );
            return null;
        }
    }

    protected final String getUrl() {
        return this.url;
    }

}
