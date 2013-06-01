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
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
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

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private KeySanitizer keySanitizer;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public InFileObjectPersister(Application application, Class<T> clazz) {
        super(application, clazz);
    }

    // ----------------------------------
    // PUBLIC API
    // ----------------------------------

    @Override
    public long getCreationDateInCache(Object cacheKey) throws CacheLoadingException {
        try {
            return getCacheFile(cacheKey).lastModified();
        } catch (Exception e) {
            throw new CacheLoadingException("Data could not be found in cache for cacheKey=" + cacheKey);
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

    /**
     * @return Wether or not this {@link InFileObjectPersister} uses a {@link KeySanitizer}.
     */
    public boolean isUsingKeySanitizer() {
        return keySanitizer != null;
    }

    /**
     * @param keySanitize
     *            the new key sanitizer to be used by this {@link InFileObjectPersister}. May be
     *            null, in that case no key sanitation will be used default).
     */
    public void setKeySanitizer(KeySanitizer keySanitizer) {
        this.keySanitizer = keySanitizer;
    }

    /**
     * @return the key sanitizer used by this {@link InFileObjectPersister}. May be null, in that
     *         case no key sanitation will be used default).
     */
    public KeySanitizer getKeySanitizer() {
        return keySanitizer;
    }

    // ----------------------------------
    // PROTECTED METHODS
    // ----------------------------------

    /**
     * Get a key that may be sanitized if a {@link KeySanitizer} is used.
     * @param cacheKey
     *            a non-sanitized cacheKey.
     * @return a key that will be sanitized if a {@link KeySanitizer} is used.
     */
    protected final String toKey(String cacheKey) {
        if (isUsingKeySanitizer()) {
            return (String) keySanitizer.sanitizeKey(cacheKey);
        } else {
            return cacheKey;
        }
    }

    /**
     * Get a cache key that may be de-sanitized if a {@link KeySanitizer} is used.
     * @param cacheKey
     *            a possibly sanitized cacheKey.
     * @return a key that will be de-sanitized if a {@link KeySanitizer} is used.
     */
    protected final String fromKey(String cacheKey) {
        if (isUsingKeySanitizer()) {
            return (String) keySanitizer.desanitizeKey(cacheKey);
        } else {
            return cacheKey;
        }
    }

    protected abstract T readCacheDataFromFile(File file) throws CacheLoadingException;

    protected String getCachePrefix() {
        return getClass().getSimpleName() + CACHE_PREFIX_END;
    }

    public File getCacheFile(Object cacheKey) {
        return new File(getCacheFolder(), getCachePrefix() + toKey(cacheKey.toString()));
    }

    private File getCacheFolder() {
        return getApplication().getCacheDir();
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
