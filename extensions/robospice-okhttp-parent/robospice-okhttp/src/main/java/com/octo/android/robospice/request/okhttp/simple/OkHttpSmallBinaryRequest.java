package com.octo.android.robospice.request.okhttp.simple;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.octo.android.robospice.request.ProgressByteProcessor;

/**
 * Downloads small images in size. All data is passed to the listener using
 * memory. This class is meant to help download small images (like thumbnails).
 * If you wish to download bigger documents (or if you don't know the size of
 * your documents), you would be better using {@link OkHttpBigBinaryRequest}.
 * @author sni & jva
 */
public class OkHttpSmallBinaryRequest extends OkHttpBinaryRequest {

    public OkHttpSmallBinaryRequest(final String url) {
        super(url);
    }

    @Override
    public InputStream processStream(final int contentLength,
        final InputStream inputStream) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        readBytes(inputStream, new ProgressByteProcessor(this, bos,
            contentLength));

        final byte[] bytes = bos.toByteArray();
        return new ByteArrayInputStream(bytes);
    }

}
