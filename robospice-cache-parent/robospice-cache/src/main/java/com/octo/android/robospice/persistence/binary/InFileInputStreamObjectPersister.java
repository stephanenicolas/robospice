package com.octo.android.robospice.persistence.binary;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import roboguice.util.temp.Ln;
import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;

public class InFileInputStreamObjectPersister extends InFileObjectPersister<InputStream> {

    public InFileInputStreamObjectPersister(Application application) {
        super(application, InputStream.class);
    }

    @Override
    protected InputStream readCacheDataFromFile(File file)
        throws CacheLoadingException {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // Should not occur (we test before if
            // file exists)
            // Do not throw, file is not cached
            Ln.w("file " + file.getAbsolutePath() + " does not exists", e);
            return null;
        }
    }

    @Override
    public InputStream saveDataToCacheAndReturnData(InputStream data, final Object cacheKey) throws CacheSavingException {
        // special case for inputstream object : as it can be read only
        // once,
        // 0) we extract the content of the input stream as a byte[]
        // 1) we save it in file asynchronously if enabled
        // 2) the result will be a new InputStream on the byte[]
        final byte[] byteArray;
        try {
            byteArray = IOUtils.toByteArray(data);

            if (isAsyncSaveEnabled()) {
                Thread t = new Thread() {

                    @Override
                    public void run() {
                        try {
                            FileUtils.writeByteArrayToFile(getCacheFile(cacheKey), byteArray);
                        } catch (IOException e) {
                            Ln.e(e, "An error occured on saving request " + cacheKey + " data asynchronously");
                        }
                    };
                };
                t.start();
            } else {
                FileUtils.writeByteArrayToFile(getCacheFile(cacheKey), byteArray);
            }

            return new ByteArrayInputStream(byteArray);
        } catch (IOException e) {
            throw new CacheSavingException(e);
        }
    }

    @Override
    public boolean canHandleClass(Class<?> clazz) {
        try {
            clazz.asSubclass(InputStream.class);
            return true;
        } catch (ClassCastException ex) {
            return false;
        }
    }
}
