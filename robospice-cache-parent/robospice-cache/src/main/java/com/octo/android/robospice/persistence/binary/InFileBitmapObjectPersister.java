package com.octo.android.robospice.persistence.binary;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;

/**
 * Stores and retrieves bitmaps to/from file system. Support custom
 * {@link BitmapFactory.Options} to lower disk usage.
 * @author David Stemmer
 */
public class InFileBitmapObjectPersister extends InFileObjectPersister<Bitmap> {

    private static final int DEFAULT_QUALITY = 100;

    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
    private BitmapFactory.Options decodingOptions = null;
    private int quality = DEFAULT_QUALITY;

    public InFileBitmapObjectPersister(Application application) {
        super(application, Bitmap.class);
    }

    @Override
    protected Bitmap readCacheDataFromFile(File file)
        throws CacheLoadingException {
        Bitmap data = BitmapFactory.decodeFile(file.getAbsolutePath(), decodingOptions);
        if (data == null) {
            throw new CacheLoadingException(String.format("Found the file %s but could not decode bitmap.", file.getAbsolutePath()));
        }
        return data;
    }

    @Override
    public Bitmap saveDataToCacheAndReturnData(Bitmap data, Object cacheKey) throws CacheSavingException {
        BufferedOutputStream out = null;

        try {
            File cacheFile = getCacheFile(cacheKey);
            out = new BufferedOutputStream(new FileOutputStream(cacheFile));

            boolean didCompress = data.compress(compressFormat, quality, out);
            if (!didCompress) {
                throw new CacheSavingException(String.format("Could not compress bitmap for path: %s", getCacheFile(cacheKey).getAbsolutePath()));
            }

            return data;
        } catch (IOException e) {
            throw new CacheSavingException(e);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public BitmapFactory.Options getDecodingOptions() {
        return decodingOptions;
    }

    public void setDecodingOptions(BitmapFactory.Options decodingOptions) {
        this.decodingOptions = decodingOptions;
    }

    public Bitmap.CompressFormat getCompressFormat() {
        return compressFormat;
    }

    public void setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

}
