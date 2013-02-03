package com.octo.android.robospice.persistence.retrofit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import retrofit.http.Converter;
import retrofit.io.TypedBytes;
import roboguice.util.temp.Ln;
import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;

public class RetrofitObjectPersister<T> extends InFileObjectPersister<T> {

    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================

    private final Converter converter;

    private String mFactoryPrefix;

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================
    public RetrofitObjectPersister(Application application, Class<T> clazz,
        String factoryPrefix, Converter converter) {
        super(application, clazz);
        this.converter = converter;
        this.mFactoryPrefix = factoryPrefix;
    }

    // ============================================================================================
    // METHODS
    // ============================================================================================

    @Override
    protected String getCachePrefix() {
        return mFactoryPrefix + super.getCachePrefix();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final T loadDataFromCache(Object cacheKey,
        long maxTimeInCacheBeforeExpiry) throws CacheLoadingException {

        File file = getCacheFile(cacheKey);
        if (file.exists()) {
            long timeInCache = System.currentTimeMillis() - file.lastModified();
            if (maxTimeInCacheBeforeExpiry == 0
                || timeInCache <= maxTimeInCacheBeforeExpiry) {
                try {
                    byte[] body = IOUtils.toByteArray(new FileReader(
                        getCacheFile(cacheKey)), "utf-8");
                    return (T) converter.to(body, getHandledClass());
                } catch (FileNotFoundException e) {
                    // Should not occur (we test before if file exists)
                    // Do not throw, file is not cached
                    Ln.w("file " + file.getAbsolutePath() + " does not exists",
                        e);
                    return null;
                } catch (Exception e) {
                    throw new CacheLoadingException(e);
                }
            }
            Ln.v("Cache content is expired since "
                + (maxTimeInCacheBeforeExpiry - timeInCache));
            return null;
        }
        Ln.v("file " + file.getAbsolutePath() + " does not exists");
        return null;
    }

    @Override
    public T saveDataToCacheAndReturnData(final T data, final Object cacheKey)
        throws CacheSavingException {

        try {
            if (isAsyncSaveEnabled()) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            saveData(data, cacheKey);
                        } catch (IOException e) {
                            Ln.e(e, "An error occured on saving request "
                                + cacheKey + " data asynchronously");
                        } catch (CacheSavingException e) {
                            Ln.e(e, "An error occured on saving request "
                                + cacheKey + " data asynchronously");
                        }
                    };
                };
                t.start();
            } else {
                saveData(data, cacheKey);
            }
        } catch (CacheSavingException e) {
            throw e;
        } catch (Exception e) {
            throw new CacheSavingException(e);
        }
        return data;
    }

    private void saveData(T data, Object cacheKey) throws IOException,
        CacheSavingException {
        // transform the content in json to store it in the cache
        TypedBytes typedBytes = converter.from(data);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(getCacheFile(cacheKey));
            typedBytes.writeTo(out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
