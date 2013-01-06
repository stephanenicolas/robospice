package com.octo.android.robospice.request.simple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Downloads big images in size. All data is passed to the listener using file
 * system. This class is meant to help download big images. If you wish to
 * download smaller documents, you would be better using
 * {@link SmallBinaryRequest}.
 * @author sni & jva
 */
public class BigBinaryRequest extends BinaryRequest {

    protected File cacheFile;

    public BigBinaryRequest(final String url, final File cacheFile) {
        super(url);
        this.cacheFile = cacheFile;
    }

    @Override
    public InputStream processStream(final int contentLength,
        final InputStream inputStream) throws IOException {
        // touch
        cacheFile.setLastModified(System.currentTimeMillis());
        final OutputStream fileOutputStream = new FileOutputStream(cacheFile);
        readBytes(inputStream, new ProgressByteProcessor(fileOutputStream,
            contentLength));
        IOUtils.closeQuietly(fileOutputStream);
        return new FileInputStream(cacheFile);
    }

    public File getCacheFile() {
        return cacheFile;
    }
}
