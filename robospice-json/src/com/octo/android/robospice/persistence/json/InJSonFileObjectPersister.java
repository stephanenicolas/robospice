package com.octo.android.robospice.persistence.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.Application;
import android.util.Log;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.octo.android.robospice.exception.CacheLoadingException;
import com.octo.android.robospice.exception.CacheSavingException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;

public final class InJSonFileObjectPersister< T > extends InFileObjectPersister< T > {

    private final static String LOG_CAT = InJSonFileObjectPersister.class.getSimpleName();

    // ============================================================================================
    // ATTRIBUTES
    // ============================================================================================

    private final ObjectMapper mJsonMapper;

    private Class< T > clazz;

    private String mFactoryPrefix;

    // ============================================================================================
    // CONSTRUCTOR
    // ============================================================================================
    public InJSonFileObjectPersister( Application application, Class< T > clazz, String factoryPrefix ) {
        super( application );
        this.clazz = clazz;
        this.mJsonMapper = new ObjectMapper();
        this.mFactoryPrefix = factoryPrefix;
    }

    // ============================================================================================
    // METHODS
    // ============================================================================================

    @Override
    protected String getCachePrefix() {
        return mFactoryPrefix + super.getCachePrefix();
    }

    @Override
    public final T loadDataFromCache( Object cacheKey, long maxTimeInCacheBeforeExpiry ) throws CacheLoadingException {
        T result = null;
        String resultJson = null;

        File file = getCacheFile( cacheKey );
        if ( file.exists() ) {
            long timeInCache = System.currentTimeMillis() - file.lastModified();
            if ( maxTimeInCacheBeforeExpiry == 0 || timeInCache <= maxTimeInCacheBeforeExpiry ) {
                try {
                    resultJson = CharStreams.toString( Files.newReader( file, Charset.forName( "UTF-8" ) ) );

                    // finally transform json in object
                    if ( !Strings.isNullOrEmpty( resultJson ) ) {
                        result = mJsonMapper.readValue( resultJson, clazz );
                        return result;
                    }
                    throw new CacheLoadingException( "Unable to restore cache content : cache file is empty" );
                } catch ( FileNotFoundException e ) {
                    // Should not occur (we test before if file exists)
                    // Do not throw, file is not cached
                    Log.w( LOG_CAT, "file " + file.getAbsolutePath() + " does not exists", e );
                    return null;
                } catch ( CacheLoadingException e ) {
                    throw e;
                } catch ( Exception e ) {
                    throw new CacheLoadingException( e );
                }
            }
            Log.v( LOG_CAT, "Cache content is expired since " + ( maxTimeInCacheBeforeExpiry - timeInCache ) );
            return null;
        }
        Log.v( LOG_CAT, "file " + file.getAbsolutePath() + " does not exists" );
        return null;
    }

    @Override
    public T saveDataToCacheAndReturnData( final T data, final Object cacheKey ) throws CacheSavingException {

        try {
            if ( isAsyncSaveEnabled ) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            saveData( data, cacheKey );
                        } catch ( IOException e ) {
                            Log.e( LOG_CAT, "An error occured on saving request " + cacheKey + " data asynchronously", e );
                        } catch ( CacheSavingException e ) {
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
                saveData( data, cacheKey );
            }
        } catch ( CacheSavingException e ) {
            throw e;
        } catch ( Exception e ) {
            throw new CacheSavingException( e );
        }
        return data;
    }

    private void saveData( T data, Object cacheKey ) throws IOException, JsonGenerationException, JsonMappingException, CacheSavingException {
        String resultJson;
        // transform the content in json to store it in the cache
        resultJson = mJsonMapper.writeValueAsString( data );

        // finally store the json in the cache
        if ( !Strings.isNullOrEmpty( resultJson ) ) {
            Files.write( resultJson, getCacheFile( cacheKey ), Charset.forName( "UTF-8" ) );
        } else {
            throw new CacheSavingException( "Data was null and could not be serialized in json" );
        }
    }

    @Override
    public boolean canHandleClass( Class< ? > clazz ) {
        return true;
    }

    /** for testing purpose only. Overriding allows to regive package level visibility. */
    @Override
    protected void awaitForSaveAsyncTermination( long time, TimeUnit timeUnit ) throws InterruptedException {
        super.awaitForSaveAsyncTermination( time, timeUnit );
    }

    /** for testing purpose only. Overriding allows to regive package level visibility. */
    @Override
    protected File getCacheFile( Object cacheKey ) {
        // TODO Auto-generated method stub
        return super.getCacheFile( cacheKey );
    }

}
