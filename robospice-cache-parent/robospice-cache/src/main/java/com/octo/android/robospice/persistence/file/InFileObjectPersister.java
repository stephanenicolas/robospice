package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import roboguice.util.temp.Ln;
import android.app.Application;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.KeySanitationExcepion;
import com.octo.android.robospice.persistence.keysanitation.KeySanitizer;

/**
 * An {@link ObjectPersister} that saves/loads data in a file.
 * @author sni
 * @param <T>
 *            the class of the data to load/save.
 */
public abstract class InFileObjectPersister<T> extends ObjectPersister<T> {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    /* package private */
    static final String CACHE_PREFIX_END = "_";

    /* package private */
    static final String DEFAULT_ROOT_CACHE_DIR = "robospice-cache";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private KeySanitizer keySanitizer;

    private File cacheFolder;

    private String factoryCachePrefix = "";

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public InFileObjectPersister(Application application, Class<T> clazz) throws CacheCreationException {
        super(application, clazz);
        setCacheFolder(null);
    }

    public InFileObjectPersister(Application application, Class<T> clazz, File cacheFolder) throws CacheCreationException {
        super(application, clazz);
        setCacheFolder(cacheFolder);
    }

    // ----------------------------------
    // PUBLIC API
    // ----------------------------------

    /**
     * Set the cacheFolder to use.
     * @param cacheFolder
     *            the new cache folder to use. Can be null, will then default to
     *            {@link #DEFAULT_ROOT_CACHE_DIR} sub folder in the application
     *            cache dir.
     * @throws CacheCreationException
     *             if the cache folder doesn't exist or can't be created.
     */
    public void setCacheFolder(File cacheFolder) throws CacheCreationException {
        if (cacheFolder == null) {
            cacheFolder = new File(getApplication().getCacheDir(), DEFAULT_ROOT_CACHE_DIR);
        }
        synchronized (cacheFolder.getAbsolutePath().intern()) {
            if (!cacheFolder.exists() && !cacheFolder.mkdirs()) {
                throw new CacheCreationException("The cache folder " + cacheFolder.getAbsolutePath() + " could not be created.");
            }
        }
        this.cacheFolder = cacheFolder;
    }

    public final File getCacheFolder() {
        return cacheFolder;
    }

    @Override
    public long getCreationDateInCache(Object cacheKey) throws CacheLoadingException {
        File cacheFile = getCacheFile(cacheKey);
        if (cacheFile.exists()) {
            return cacheFile.lastModified();
        } else {
            throw new CacheLoadingException(
                "Data could not be found in cache for cacheKey=" + cacheKey);
        }
    }

    @Override
    public List<Object> getAllCacheKeys() {
        final String prefix = getCachePrefix();
        int prefixLength = prefix.length();
        String[] cacheFileNameList = getCacheFolder().list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                // patch from florianmski
                return filename.startsWith(prefix);
            }
        });
        List<Object> result = new ArrayList<Object>(cacheFileNameList.length);
        for (String cacheFileName : cacheFileNameList) {
            String cacheKey = cacheFileName.substring(prefixLength);
            result.add(fromKey(cacheKey));
        }
        return result;

    }

    @Override
    public List<T> loadAllDataFromCache() throws CacheLoadingException {
        List<Object> allCacheKeys = getAllCacheKeys();
        List<T> result = new ArrayList<T>(allCacheKeys.size());
        for (Object key : allCacheKeys) {
            result.add(loadDataFromCache(key, DurationInMillis.ALWAYS_RETURNED));
        }
        return result;
    }

    @Override
    public boolean removeDataFromCache(Object cacheKey) {
        return getCacheFile(cacheKey).delete();
    }

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
        for (File cacheFile : cacheFileList) {
            allDeleted = cacheFile.delete() && allDeleted;
        }
        if (allDeleted || cacheFileList.length == 0) {
            Ln.d("Some file could not be deleted from cache.");
        }
    }

    @Override
    public T loadDataFromCache(Object cacheKey, long maxTimeInCache) throws CacheLoadingException {

        File file = getCacheFile(cacheKey);
        if (isCachedAndNotExpired(file, maxTimeInCache)) {
            return readCacheDataFromFile(file);
        }

        return null;
    }

    @Override
    public boolean isDataInCache(Object cacheKey, long maxTimeInCacheBeforeExpiry) {
        File file = getCacheFile(cacheKey);
        return isCachedAndNotExpired(file, maxTimeInCacheBeforeExpiry);
    }

    /**
     * @return Whether or not this {@link InFileObjectPersister} uses a
     *         {@link KeySanitizer}.
     */
    public boolean isUsingKeySanitizer() {
        return keySanitizer != null;
    }

    /**
     * @param keySanitizer
     *            the new key sanitizer to be used by this
     *            {@link InFileObjectPersister}. May be null, in that case no
     *            key sanitation will be used default). If key sanitation fails
     *            on a given cache key (by throwing a
     *            {@link KeySanitationExcepion}, original (unsanitized) cache
     *            keys will be used directly.
     */
    public void setKeySanitizer(KeySanitizer keySanitizer) {
        this.keySanitizer = keySanitizer;
    }

    /**
     * @return the key sanitizer used by this {@link InFileObjectPersister}. May
     *         be null, in that case no key sanitation will be used default).
     */
    public KeySanitizer getKeySanitizer() {
        return keySanitizer;
    }

    public final File getCacheFile(Object cacheKey) {
        return new File(getCacheFolder(), getCachePrefix() + toKey(cacheKey.toString()));
    }

    // ----------------------------------
    // PROTECTED METHODS
    // ----------------------------------
    /* package-private */
    void setFactoryCachePrefix(String factoryCachePrefix) {
        this.factoryCachePrefix = factoryCachePrefix;
    }

    /**
     * Get a key that may be sanitized if a {@link KeySanitizer} is used.
     * @param cacheKey
     *            a non-sanitized cacheKey.
     * @return a key that will be sanitized if a {@link KeySanitizer} is used.
     */
    protected final String toKey(String cacheKey) {
        if (isUsingKeySanitizer()) {
            try {
                return (String) keySanitizer.sanitizeKey(cacheKey);
            } catch (KeySanitationExcepion e) {
                Ln.e(e, "Key could not be sanitized, falling back on original key.");
                return cacheKey;
            }
        } else {
            return cacheKey;
        }
    }

    /**
     * Get a cache key that may be de-sanitized if a {@link KeySanitizer} is
     * used.
     * @param cacheKey
     *            a possibly sanitized cacheKey.
     * @return a key that will be de-sanitized if a {@link KeySanitizer} is
     *         used.
     */
    protected final String fromKey(String cacheKey) {
        if (isUsingKeySanitizer()) {
            try {
                return (String) keySanitizer.desanitizeKey(cacheKey);
            } catch (KeySanitationExcepion e) {
                Ln.e(e, "Key could not be desanitized, falling back on original key.");
                return cacheKey;
            }
        } else {
            return cacheKey;
        }
    }

    protected abstract T readCacheDataFromFile(File file) throws CacheLoadingException;

    protected final String getCachePrefix() {
        return factoryCachePrefix + getClass().getSimpleName() + CACHE_PREFIX_END + getHandledClass().getSimpleName() + CACHE_PREFIX_END;
    }

    protected boolean isCachedAndNotExpired(Object cacheKey, long maxTimeInCacheBeforeExpiry) {
        File cacheFile = getCacheFile(cacheKey);
        return isCachedAndNotExpired(cacheFile, maxTimeInCacheBeforeExpiry);
    }

    protected boolean isCachedAndNotExpired(File cacheFile, long maxTimeInCacheBeforeExpiry) {
        if (cacheFile.exists()) {
            long timeInCache = System.currentTimeMillis() - cacheFile.lastModified();
            if (maxTimeInCacheBeforeExpiry == DurationInMillis.ALWAYS_RETURNED || timeInCache <= maxTimeInCacheBeforeExpiry) {
                return true;
            }
        }
        return false;
    }

}
