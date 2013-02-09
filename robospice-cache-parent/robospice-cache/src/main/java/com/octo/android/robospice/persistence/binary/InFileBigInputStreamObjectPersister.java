package com.octo.android.robospice.persistence.binary;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import android.app.Application;

import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

/**
 * Stores / retrieves data in file system. This {@link ObjectPersister} is
 * optimized for memory. It will only use file system to save the data, without
 * allocating large memory space to transfert a given binary to the cache. It
 * should be preferred to {@link InFileInputStreamObjectPersister} for low
 * memory devices or memory intensive usages.
 * @author SNI
 */
public final class InFileBigInputStreamObjectPersister extends InFileInputStreamObjectPersister {

    public InFileBigInputStreamObjectPersister(Application application) {
        super(application);
    }

    @Override
    public InputStream saveDataToCacheAndReturnData(InputStream data, Object cacheKey) throws CacheSavingException {
        FileOutputStream output = null;
        // special case for big inputstream object : as it can be read
        // only once and is too big to be locally
        // duplicated,
        // 1) we save it in file
        // 2) we load and return it from the file
        try {
            output = new FileOutputStream(getCacheFile(cacheKey));
            IOUtils.copy(data, output);
            return new FileInputStream(getCacheFile(cacheKey));
        } catch (IOException e) {
            throw new CacheSavingException(e);
        } finally {
            IOUtils.closeQuietly(output);
        }
    }

    @Override
    public void setAsyncSaveEnabled(boolean isAsyncSaveEnabled) {
        if (isAsyncSaveEnabled) {
            throw new IllegalStateException("Asynchronous saving operation not supported.");
        }
    }
}
