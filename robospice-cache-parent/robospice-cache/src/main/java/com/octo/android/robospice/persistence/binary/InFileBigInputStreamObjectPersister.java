package com.octo.android.robospice.persistence.binary;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheSavingException;

public final class InFileBigInputStreamObjectPersister extends
    InFileInputStreamObjectPersister {

    public InFileBigInputStreamObjectPersister(Application application) {
        super(application);
    }

    @Override
    public InputStream saveDataToCacheAndReturnData(InputStream data,
        Object cacheKey) throws CacheSavingException {
        // special case for big inputstream object : as it can be read
        // only once and is too big to be locally
        // duplicated,
        // 1) we save it in file
        // 2) we load and return it from the file
        try {
            FileOutputStream output = new FileOutputStream(
                getCacheFile(cacheKey));
            IOUtils.copy(data, output);
            IOUtils.closeQuietly(output);
            return new FileInputStream(getCacheFile(cacheKey));
        } catch (IOException e) {
            throw new CacheSavingException(e);
        }
    }

    @Override
    public void setAsyncSaveEnabled(boolean isAsyncSaveEnabled) {
        if (isAsyncSaveEnabled) {
            throw new IllegalStateException(
                "Asynchronous saving operation not supported.");
        }
    }
}
