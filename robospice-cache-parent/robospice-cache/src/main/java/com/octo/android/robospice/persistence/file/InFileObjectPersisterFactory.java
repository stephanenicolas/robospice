package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import roboguice.util.temp.Ln;
import android.app.Application;

import com.octo.android.robospice.persistence.CacheCleaner;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.ObjectPersisterFactory;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.keysanitation.KeySanitizer;

/**
 * A factory that will create {@link ObjectPersister} instances that save/load
 * data in a file.
 * @author sni
 */
public abstract class InFileObjectPersisterFactory extends ObjectPersisterFactory implements CacheCleaner {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private File cacheFolder;
    private String cachePrefix;
    private KeySanitizer keySanitizer;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public InFileObjectPersisterFactory(Application application) throws CacheCreationException {
        this(application, null, null);
    }

    public InFileObjectPersisterFactory(Application application, File cacheFolder) throws CacheCreationException {
        this(application, null, cacheFolder);
    }

    public InFileObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses) throws CacheCreationException {
        this(application, listHandledClasses, null);
    }

    public InFileObjectPersisterFactory(Application application, List<Class<?>> listHandledClasses, File cacheFolder) throws CacheCreationException {
        super(application, listHandledClasses);
        setCacheFolder(cacheFolder);
        setCachePrefix(getClass().getSimpleName() + InFileObjectPersister.CACHE_PREFIX_END);
    }

    // ----------------------------------
    // API
    // ----------------------------------

    /**
     * Sets the folder used by object persisters of this factory.
     * @param cacheFolder
     *            the new cache folder of this factory (and persisters it will
     *            create). Ca be null, it will then default to the sub folder
     *            {@link InFileObjectPersister#DEFAULT_ROOT_CACHE_DIR} in side
     *            the application cache folder. Will be created if doesn't exist
     *            yet.
     * @throws CacheCreationException
     */
    public void setCacheFolder(File cacheFolder) throws CacheCreationException {
        if (cacheFolder == null) {
            cacheFolder = new File(getApplication().getCacheDir(), InFileObjectPersister.DEFAULT_ROOT_CACHE_DIR);
        }

        this.cacheFolder = cacheFolder;
        if (!cacheFolder.exists() && !cacheFolder.mkdirs()) {
            throw new CacheCreationException("The cache folder " + cacheFolder.getAbsolutePath() + " could not be created.");
        }
    }

    /**
     * Sets the cachePrefix used by object persisters of this factory.
     * @param cachePrefix
     *            the new cache cachePrefix of this factory (and persisters it
     *            will create). Defaults to "className".
     */
    public void setCachePrefix(String cachePrefix) {
        this.cachePrefix = cachePrefix;
    }

    public File getCacheFolder() {
        return cacheFolder;
    }

    public String getCachePrefix() {
        return cachePrefix;
    }

    public KeySanitizer getKeySanitizer() {
        return keySanitizer;
    }

    /**
     * @param keySanitizer
     *            the new key sanitizer to be used by this
     *            {@link InFileObjectPersisterFactory} and persisters. May be
     *            null, in that case no key sanitation will be used. This is the
     *            default.
     */
    public void setKeySanitizer(KeySanitizer keySanitizer) {
        this.keySanitizer = keySanitizer;
    }

    @Override
    public final <T> InFileObjectPersister<T> createObjectPersister(Class<T> clazz) {

        InFileObjectPersister<T> inFileObjectPersister;
        try {
            inFileObjectPersister = createInFileObjectPersister(clazz, cacheFolder);
            inFileObjectPersister.setFactoryCachePrefix(cachePrefix);
            inFileObjectPersister.setKeySanitizer(keySanitizer);
            return inFileObjectPersister;
        } catch (CacheCreationException e) {
            throw new RuntimeException("Could not create cache folder of factory.", e);
        }
    }

    public abstract <T> InFileObjectPersister<T> createInFileObjectPersister(Class<T> clazz, File cacheFolder) throws CacheCreationException;

    @Override
    public void removeAllDataFromCache() {
        File cacheFolder = getCacheFolder();
        File[] cacheFileList = cacheFolder.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.getName().startsWith(getCachePrefix());
            }
        });

        boolean allDeleted = true;
        if (cacheFileList == null || cacheFileList.length == 0) {
            return;
        }
        for (File cacheFile : cacheFileList) {
            allDeleted = cacheFile.delete() && allDeleted;
        }
        if (allDeleted || cacheFileList.length == 0) {
            Ln.d("Some file could not be deleted from cache.");
        }
    }

}
