package com.octo.android.robospice.request.simple;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import roboguice.util.temp.Ln;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.octo.android.robospice.request.ProgressByteProcessor;
import com.octo.android.robospice.request.SpiceRequest;

/**
 * Downloads big images in size as bitmaps. All data is passed to the listener
 * using file system.
 * @author sni
 */
public class BitmapRequest extends SpiceRequest<Bitmap> implements IBitmapRequest {

    private static final int BUF_SIZE = 4096;

    private String url;
    private BitmapFactory.Options options;
    private File cacheFile;

    private int height = -1;
    private int width = -1;

    /**
     * Creates a BitmapRequest able to fetch a {@link Bitmap} from the network.
     * @param url
     *            the url of the bitmap to fetch.
     * @param cacheFile
     *            a file used to store data during download.
     */
    public BitmapRequest(String url, File cacheFile) {
        this(url, new BitmapFactory.Options(), cacheFile);
    }

    /**
     * Creates a BitmapRequest able to fetch a {@link Bitmap} from the network.
     * @param url
     *            the url of the bitmap to fetch.
     * @param options
     *            used to decode the data received from the network.
     * @param cacheFile
     *            a file used to store data during download.
     */
    public BitmapRequest(String url, BitmapFactory.Options options, File cacheFile) {
        super(Bitmap.class);
        this.url = url;
        this.options = options;
        this.cacheFile = cacheFile;
    }

    /**
     * Creates a BitmapRequest able to fetch a {@link Bitmap} from the network.
     * @param url
     *            the url of the bitmap to fetch.
     * @param width
     *            the requested width of the image.
     * @param height
     *            the requested height of the image.
     * @param cacheFile
     *            a file used to store data during download.
     */
    public BitmapRequest(String url, int width, int height, File cacheFile) {
        super(Bitmap.class);
        this.url = url;
        this.width = width;
        this.height = height;
        this.cacheFile = cacheFile;
    }

    @Override
    public Bitmap loadDataFromNetwork() throws Exception {
        try {
            final HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            processStream(httpURLConnection.getContentLength(), httpURLConnection.getInputStream());

            if (width != -1 && height != -1) {
                this.options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(cacheFile.getAbsolutePath(), options);
                options.inSampleSize = calculateInSampleSize(options, width, height);
                options.inJustDecodeBounds = false;
                options.inPurgeable = true;
                return BitmapFactory.decodeFile(cacheFile.getAbsolutePath(), options);
            } else {
                return BitmapFactory.decodeFile(cacheFile.getAbsolutePath(), options);
            }
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

    /*
     * (non-Javadoc)
     * @see
     * com.octo.android.robospice.request.simple.IBitmapRequest#getCacheFile()
     */
    @Override
    public File getCacheFile() {
        return cacheFile;
    }

    public void processStream(int contentLength, final InputStream inputStream) throws IOException {
        OutputStream fileOutputStream = null;
        try {
            // touch
            boolean isTouchedNow = cacheFile.setLastModified(System.currentTimeMillis());
            if (!isTouchedNow) {
                Ln.d("Modification time of file %s could not be changed normally ", cacheFile.getAbsolutePath());
            }
            fileOutputStream = new FileOutputStream(cacheFile);
            readBytes(inputStream, new ProgressByteProcessor(this, fileOutputStream, contentLength));
        } finally {
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

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

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }
}
