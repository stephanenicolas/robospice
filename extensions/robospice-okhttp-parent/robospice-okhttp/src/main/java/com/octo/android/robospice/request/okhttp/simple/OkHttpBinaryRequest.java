package com.octo.android.robospice.request.okhttp.simple;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.commons.io.IOUtils;

import roboguice.util.temp.Ln;

import com.octo.android.robospice.request.ProgressByteProcessor;
import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;

/**
 * Abstract class used to download binaries. See {@link OkHttpSmallBinaryRequest} and
 * {@link OkHttpBigBinaryRequest}
 * @author jva
 */
public abstract class OkHttpBinaryRequest extends OkHttpSpiceRequest<InputStream> {

    private static final int BUF_SIZE = 4096;
    protected String url;

    public OkHttpBinaryRequest(final String url) {
        super(InputStream.class);
        this.url = url;
    }

    @Override
    public final InputStream loadDataFromNetwork() throws Exception {
        try {
            Request request = new Request.Builder().url(url).build();
            Response response = getOkHttpClient().newCall(request).execute();
            return processStream(response.body().contentLength(), response.body().byteStream());
        } catch (final MalformedURLException e) {
            Ln.e(e, "Unable to create URL");
            throw e;
        } catch (final IOException e) {
            Ln.e(e, "Unable to download binary");
            throw e;
        }
    }

    protected final String getUrl() {
        return this.url;
    }

    /**
     * Override this method to process the stream downloaded
     * @param contentLength
     *            size of the download
     * @param inputStream
     *            stream of the download
     * @return an inputstream containing the download
     */
    public abstract InputStream processStream(long contentLength, InputStream inputStream) throws IOException;

    /**
     * Inspired from Guava com.google.common.io.ByteStreams
     */
    protected void readBytes(final InputStream in, final ProgressByteProcessor processor) throws IOException {
        final byte[] buf = new byte[BUF_SIZE];
        try {
            int amt;
            do {
                amt = in.read(buf);
                if (amt == -1) {
                    break;
                }
            } while (processor.processBytes(buf, 0, amt));
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

}
