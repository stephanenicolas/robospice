package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;

/**
 * An {@link ObjectPersister} that saves/loads data in a file.
 * 
 * @author sni
 * 
 * @param <T>
 *            the class of the data to load/save.
 */
public abstract class InFileObjectPersister< T > extends ObjectPersister< T > {

    /* package private */
    static final String CACHE_PREFIX_END = "_";

    public InFileObjectPersister( Application application, Class< T > clazz ) {
        super( application, clazz );
    }

    @Override
    public List< Object > getAllCacheKeys() {
        final String prefix = getCachePrefix();
        String[] cacheFileNameList = getCacheFolder().list( new FilenameFilter() {
            @Override
            public boolean accept( File dir, String filename ) {
                return filename.startsWith( prefix );
            }
        } );
        List< Object > result = new ArrayList< Object >();
        for ( String cacheFileName : cacheFileNameList ) {
            result.add( cacheFileName.substring( prefix.length() ) );
        }
        return result;

    }

    @Override
    public List< T > loadAllDataFromCache() throws CacheLoadingException {
        List< T > result = new ArrayList< T >();
        for ( Object key : getAllCacheKeys() ) {
            result.add( loadDataFromCache( key, DurationInMillis.ALWAYS ) );
        }
        return result;
    }

    @Override
    public boolean removeDataFromCache( Object cacheKey ) {
        return getCacheFile( cacheKey ).delete();
    }

    @Override
    public void removeAllDataFromCache() {
        File cacheFolder = getCacheFolder();
        File[] cacheFileList = cacheFolder.listFiles( new FileFilter() {

            @Override
            public boolean accept( File file ) {
                return file.getName().startsWith( getCachePrefix() );
            }
        } );

        for ( File cacheFile : cacheFileList ) {
            cacheFile.delete();
        }
    }

    protected String getCachePrefix() {
        return getClass().getSimpleName() + CACHE_PREFIX_END;
    }

    protected File getCacheFile( Object cacheKey ) {
        return new File( getCacheFolder(), getCachePrefix() + cacheKey.toString() );
    }

    private File getCacheFolder() {
        return getApplication().getCacheDir();
    }

}