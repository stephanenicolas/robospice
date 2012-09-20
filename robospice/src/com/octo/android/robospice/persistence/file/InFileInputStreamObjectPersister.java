package com.octo.android.robospice.persistence.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import android.app.Application;
import android.util.Log;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.octo.android.robospice.exception.CacheLoadingException;
import com.octo.android.robospice.exception.CacheSavingException;

public class InFileInputStreamObjectPersister extends InFileObjectPersister< InputStream > {

    private final static String LOG_CAT = InFileInputStreamObjectPersister.class.getSimpleName();

    public InFileInputStreamObjectPersister( Application application ) {
        super( application );
    }

    @Override
    public InputStream loadDataFromCache( Object cacheKey, long maxTimeInCacheBeforeExpiry ) throws CacheLoadingException {
        File file = getCacheFile( cacheKey );
        if ( file.exists() ) {
            long timeInCache = System.currentTimeMillis() - file.lastModified();
            if ( maxTimeInCacheBeforeExpiry == 0 || timeInCache <= maxTimeInCacheBeforeExpiry ) {
                try {
                    return new FileInputStream( file );
                } catch ( FileNotFoundException e ) {
                    // Should not occur (we test before if file exists)
                    // Do not throw, file is not cached
                    Log.w( LOG_CAT, "file " + file.getAbsolutePath() + " does not exists", e );
                    return null;
                }
            }
        }
        Log.v( LOG_CAT, "file " + file.getAbsolutePath() + " does not exists" );
        return null;
    }

    @Override
    public InputStream saveDataToCacheAndReturnData( InputStream data, final Object cacheKey ) throws CacheSavingException {
        // special case for inputstream object : as it can be read only once,
        // 0) we extract the content of the input stream as a byte[]
        // 1) we save it in file asynchronously if enabled
        // 2) the result will be a new InputStream on the byte[]
        final byte[] byteArray;
        try {
            byteArray = ByteStreams.toByteArray( data );

            if ( isAsyncSaveEnabled ) {
                new Thread() {

                    @Override
                    public void run() {
                        try {
                            ByteStreams.write( byteArray, Files.newOutputStreamSupplier( getCacheFile( cacheKey ) ) );
                        } catch ( IOException e ) {
                            Log.e( LOG_CAT, "An error occured on saving request " + cacheKey + " data asynchronously", e );
                        } finally {
                            // notify that saving is finished for test purpose
                            lock.lock();
                            condition.signal();
                            lock.unlock();
                        }
                    };
                }.start();
            } else {
                ByteStreams.write( byteArray, Files.newOutputStreamSupplier( getCacheFile( cacheKey ) ) );
            }

            return new ByteArrayInputStream( byteArray );
        } catch ( IOException e ) {
            throw new CacheSavingException( e );
        }
    }

    @Override
    public boolean canHandleClass( Class< ? > clazz ) {
        try {
            clazz.asSubclass( InputStream.class );
            return true;
        } catch ( ClassCastException ex ) {
            return false;
        }
    }

    /** for testing purpose only. Overriding allows to regive package level visibility. */
    @Override
    protected void awaitForSaveAsyncTermination( long time, TimeUnit timeUnit ) throws InterruptedException {
        super.awaitForSaveAsyncTermination( time, timeUnit );
    }
}
