package com.octo.android.robospice.persistence.string;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;

import roboguice.util.temp.Ln;
import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;

public class InFileStringObjectPersister extends InFileObjectPersister<String> {

    public InFileStringObjectPersister(Application application) {
        super(application, String.class);
    }

    @Override
    public boolean canHandleClass(Class<?> clazz) {
        return clazz.equals(String.class);
    }

    @Override
    protected String readCacheDataFromFile(File file)
        throws CacheLoadingException {
        try {
            return FileUtils.readFileToString(file, CharEncoding.UTF_8);
        } catch (FileNotFoundException e) {
            // Should not occur (we test before if
            // file exists)
            // Do not throw, file is not cached
            Ln.w("file " + file.getAbsolutePath() + " does not exists", e);
            return null;
        } catch (Exception e) {
            throw new CacheLoadingException(e);
        }
    }

    @Override
    public String saveDataToCacheAndReturnData(final String data, final Object cacheKey) throws CacheSavingException {
        Ln.v("Saving String " + data + " into cacheKey = " + cacheKey);
        try {
            if (isAsyncSaveEnabled()) {

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            FileUtils.writeStringToFile(getCacheFile(cacheKey), data, CharEncoding.UTF_8);
                        } catch (IOException e) {
                            Ln.e(e, "An error occured on saving request " + cacheKey + " data asynchronously");
                        }
                    };
                };
                t.start();
            } else {
                FileUtils.writeStringToFile(getCacheFile(cacheKey), data, CharEncoding.UTF_8);
            }
        } catch (Exception e) {
            throw new CacheSavingException(e);
        }
        return data;
    }
}
