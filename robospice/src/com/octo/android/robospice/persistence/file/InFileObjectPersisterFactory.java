package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.io.FileFilter;

import android.app.Application;

import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.ObjectPersisterFactory;

/**
 * A factory that will create {@link ObjectPersister} instances will that saves/loads data in a file.
 * 
 * @author sni
 * 
 * @param <DATA>
 *            the class of the data to load/save.
 */
public abstract class InFileObjectPersisterFactory extends ObjectPersisterFactory {

    public InFileObjectPersisterFactory( Application application ) {
        super( application );
    }

    @Override
    public abstract < DATA > InFileObjectPersister< DATA > createClassCacheManager( Class< DATA > clazz );

    public boolean removeDataFromCache( Class< ? > clazz, Object cacheKey ) {
        return createClassCacheManager( clazz ).removeDataFromCache( cacheKey );
    }

    public void removeAllDataFromCache( Class< ? > clazz ) {
        createClassCacheManager( clazz ).removeAllDataFromCache();
    }

    @Override
    public void removeAllDataFromCache() {
        File cacheFolder = getCacheFolder();
        File[] cacheFileList = cacheFolder.listFiles( new FileFilter() {

            public boolean accept( File pathname ) {
                String path = pathname.getAbsolutePath();
                String fileName = path.substring( path.lastIndexOf( File.separator ) + 1 );
                return fileName.startsWith( getCachePrefix() );
            }
        } );

        for ( File cacheFile : cacheFileList ) {
            cacheFile.delete();
        }
    }

    protected String getCachePrefix() {
        return getClass().getSimpleName() + InFileObjectPersister.CACHE_PREFIX_END;
    }

    private File getCacheFolder() {
        return getApplication().getCacheDir();
    }
}
