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
 * @author sni & jva
 */
public class OkHttpBigBinaryRequest extends OkHttpBinaryRequest {

    protected File cacheFile;

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
