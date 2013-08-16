package com.octo.android.robospice.request.okhttp.simple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import roboguice.util.temp.Ln;

import com.octo.android.robospice.request.ProgressByteProcessor;

/**
 * Downloads big images in size. All data is passed to the listener using file
 * system. This class is meant to help download big images. If you wish to
 * download smaller documents, you would be better using
 * {@link OkHttpSmallBinaryRequest}.
 * @author sni 
 */
public class OkHttpBigBinaryRequest extends OkHttpBinaryRequest {

    protected File cacheFile;

    /**
     * Creates a OkHttpBigBinaryRequest using its own cache file to prevent any downloaded data to be stored in memory.
     * All file received from the network (via a simple http GET request) will be stored directly in the cache file to prevent memory loss.
     * @param url the url to get the image data from.
     * @param cacheFile a file used to store image data. Developers will have to handle the cache file erasure by themselves.
     * <b>This cache file is not handled by RS caching mechanism in the "normal way". </b>
     */
    public OkHttpBigBinaryRequest(final String url, final File cacheFile) {
        super(url);
        this.cacheFile = cacheFile;
    }

    @Override
    public InputStream processStream(final int contentLength,
        final InputStream inputStream) throws IOException {
        OutputStream fileOutputStream = null;
        try {
            // touch
            boolean isTouchedNow = cacheFile.setLastModified(System
                .currentTimeMillis());
            if (!isTouchedNow) {
                Ln.d(
                    "Modification time of file %s could not be changed normally ",
                    cacheFile.getAbsolutePath());
            }
            fileOutputStream = new FileOutputStream(cacheFile);
            readBytes(inputStream, new ProgressByteProcessor(this,
                fileOutputStream, contentLength));
            return new FileInputStream(cacheFile);
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    public File getCacheFile() {
        return cacheFile;
    }
}
