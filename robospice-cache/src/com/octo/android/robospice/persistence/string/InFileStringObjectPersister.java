package com.octo.android.robospice.persistence.string;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import roboguice.util.temp.Ln;
import android.app.Application;

import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;

public final class InFileStringObjectPersister extends InFileObjectPersister< String > {

    public InFileStringObjectPersister( Application application ) {
        super( application, String.class );
    }

    @Override
    public boolean canHandleClass( Class< ? > clazz ) {
        return clazz.equals( String.class );
    }

    @Override
    public String loadDataFromCache( Object cacheKey, long maxTimeInCacheBeforeExpiry ) throws CacheLoadingException {
        Ln.v( "Loading String for cacheKey = " + cacheKey );
        File file = getCacheFile( cacheKey );
        if ( file.exists() ) {
            long timeInCache = System.currentTimeMillis() - file.lastModified();
            if ( maxTimeInCacheBeforeExpiry == 0 || timeInCache <= maxTimeInCacheBeforeExpiry ) {
                try {
                    return CharStreams.toString( Files.newReader( file, Charset.forName( "UTF-8" ) ) );
                } catch ( FileNotFoundException e ) {
                    // Should not occur (we test before if file exists)
                    // Do not throw, file is not cached
                    Ln.w( "file " + file.getAbsolutePath() + " does not exists", e );
                    return null;
                } catch ( Exception e ) {
                    throw new CacheLoadingException( e );
                }
            }
        }
        Ln.v( "file " + file.getAbsolutePath() + " does not exists" );
        return null;
    }

    @Override
    public String saveDataToCacheAndReturnData( final String data, final Object cacheKey ) throws CacheSavingException {
        Ln.v( "Saving String " + data + " into cacheKey = " + cacheKey );
        try {
            if ( isAsyncSaveEnabled ) {

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Files.write( data, getCacheFile( cacheKey ), Charset.forName( "UTF-8" ) );
                        } catch ( IOException e ) {
                            Ln.e( e, "An error occured on saving request " + cacheKey + " data asynchronously" );
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

    /* Overriden to regive permission to package. Just for testing. */
    @Override
    protected File getCacheFile( Object cacheKey ) {
        return super.getCacheFile( cacheKey );
    }

}
