package com.octo.android.robospice.persistence.binary;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;

import java.io.*;

/**
 * @author David Stemmer
 */
public class InFileBitmapObjectPersister extends InFileObjectPersister<Bitmap> {

    private static final String ERROR_CACHE_MISS_EXPIRED = "Found file in cache but the data was stale: %s";
    private static final String ERROR_CACHE_MISS_NOT_FOUND = "No cached file for path: %s";
    private static final String ERROR_COULD_NOT_COMPRESS_BITMAP = "Could not compress bitmap for path: %s";
    private static final String ERROR_COULD_NOT_DECODE_BITMAP = "Found the file but could not decode bitmap for path: %s";

    public InFileBitmapObjectPersister(Application application) {
        super(application, Bitmap.class);
    }

    @Override
    public Bitmap loadDataFromCache(Object cacheKey, long maxTimeInCache)
        throws CacheLoadingException {

        File cachedFile = getCacheFile(cacheKey);

        boolean dataIsMissing = !cachedFile.exists();
        if (dataIsMissing) {
            String errorMsg = String.format(ERROR_CACHE_MISS_NOT_FOUND,
                cachedFile.getAbsolutePath());
            throw new CacheLoadingException(errorMsg);
        }

        boolean dataDoesExpire = maxTimeInCache != DurationInMillis.ALWAYS;
        boolean dataIsStale = System.currentTimeMillis() - cachedFile.lastModified() > maxTimeInCache;
        if (dataDoesExpire && dataIsStale) {
            String errorMsg = String.format(ERROR_CACHE_MISS_EXPIRED,
                cachedFile.getAbsolutePath());
            throw new CacheLoadingException(errorMsg);
        }

        Bitmap data = BitmapFactory.decodeFile(cachedFile.getAbsolutePath());

        boolean bitmapNotLoaded = data == null;
        if (bitmapNotLoaded) {
            String errorMsg = String.format(ERROR_COULD_NOT_DECODE_BITMAP,
                cachedFile.getAbsolutePath());
            throw new CacheLoadingException(errorMsg);
        }

        return data;
    }

    @Override
    public Bitmap saveDataToCacheAndReturnData(Bitmap data, Object cacheKey)
        throws CacheSavingException {
        try {
            String errorMsg = null;
            File cacheFile = getCacheFile(cacheKey);
            BufferedOutputStream out = new BufferedOutputStream(
                new FileOutputStream(cacheFile));

            boolean didCompress = data.compress(Bitmap.CompressFormat.PNG, 100,
                out);
            if (!didCompress) {
                /*
                 * we don't throw the error immediately so the stream has an
                 * opportunity to close
                 */
                errorMsg = String.format(ERROR_COULD_NOT_COMPRESS_BITMAP,
                    getCacheFile(cacheKey).getAbsolutePath());
            }

            out.flush();
            out.close();

            if (errorMsg != null) {
                throw new CacheSavingException(errorMsg);
            }

            return data;
        } catch (FileNotFoundException e) {
            throw new CacheSavingException(e);
        } catch (IOException e) {
            throw new CacheSavingException(e);
        }
    }
}
