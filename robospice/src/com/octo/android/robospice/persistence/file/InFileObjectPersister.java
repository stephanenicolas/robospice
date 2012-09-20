package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.io.FileFilter;

import android.app.Application;

import com.octo.android.robospice.persistence.ObjectPersister;

/**
 * An {@link ObjectPersister} that saves/loads data in a file.
 * 
 * @author sni
 * 
 * @param <DATA>
 *            the class of the data to load/save.
 */
public abstract class InFileObjectPersister< DATA > extends ObjectPersister< DATA > {

    /* package private */
    static final String CACHE_PREFIX_END = "_";

    public InFileObjectPersister( Application application ) {
        super( application );
    }

    @Override
    public boolean removeDataFromCache( Object cacheKey ) {
        return getCacheFile( cacheKey ).delete();
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
        return getClass().getSimpleName() + CACHE_PREFIX_END;
    }

    protected File getCacheFile( Object cacheKey ) {
        return new File( getCacheFolder(), getCachePrefix() + cacheKey.toString() );
    }

    private File getCacheFolder() {
        return getApplication().getCacheDir();
    }

}