package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import android.app.Application;
import android.util.Log;

import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.octo.android.robospice.exception.CacheLoadingException;
import com.octo.android.robospice.exception.CacheSavingException;

public final class InFileStringObjectPersister extends InFileObjectPersister< String > {
    private final static String LOG_CAT = InFileStringObjectPersister.class.getSimpleName();

    public InFileStringObjectPersister( Application application ) {
        super( application );
    }

    @Override
    public boolean canHandleClass( Class< ? > clazz ) {
        return clazz.equals( String.class );
    }

    @Override
    public String loadDataFromCache( Object cacheKey, long maxTimeInCacheBeforeExpiry ) throws CacheLoadingException {
        Log.v( LOG_CAT, "Loading String for cacheKey = " + cacheKey );
        File file = getCacheFile( cacheKey );
        if ( file.exists() ) {
            long timeInCache = System.currentTimeMillis() - file.lastModified();
            if ( maxTimeInCacheBeforeExpiry == 0 || timeInCache <= maxTimeInCacheBeforeExpiry ) {
                try {
                    return CharStreams.toString( Files.newReader( file, Charset.forName( "UTF-8" ) ) );
                } catch ( FileNotFoundException e ) {
                    // Should not occur (we test before if file exists)
                    // Do not throw, file is not cached
                    Log.w( LOG_CAT, "file " + file.getAbsolutePath() + " does not exists", e );
                    return null;
                } catch ( Exception e ) {
                    throw new CacheLoadingException( e );
                }
            }
        }
        Log.v( LOG_CAT, "file " + file.getAbsolutePath() + " does not exists" );
        return null;
    }

    @Override
    public String saveDataToCacheAndReturnData( final String data, final Object cacheKey ) throws CacheSavingException {
        Log.v( LOG_CAT, "Saving String " + data + " into cacheKey = " + cacheKey );
        try {
            if ( isAsyncSaveEnabled ) {

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Files.write( data, getCacheFile( cacheKey ), Charset.forName( "UTF-8" ) );
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
                Files.write( data, getCacheFile( cacheKey ), Charset.forName( "UTF-8" ) );
            }
        } catch ( Exception e ) {
            throw new CacheSavingException( e );
        }
        return data;
    }

    /** for testing purpose only. Overriding allows to regive package level visibility. */
    @Override
    protected void awaitForSaveAsyncTermination( long time, TimeUnit timeUnit ) throws InterruptedException {
        super.awaitForSaveAsyncTermination( time, timeUnit );
    }

}
