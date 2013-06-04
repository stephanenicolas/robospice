package com.octo.android.robospice.persistence.keysanitation;

import com.octo.android.robospice.persistence.exception.KeySanitationExcepion;

/**
 * Describes the behavior of an entity responsible for sanitizing keys.
 * Basically, it will take a cache key and encrypt/decrypt to/from a string that
 * can be safely used to create a cache entry.
 * <p/>
 * For instance, it can take a cache key like '/foo%1' and convert it to some
 * key that can be used to create a cache file's name on a file system.
 * <p/>
 * The operation proposed by a {@link KeySanitizer} must be bijective.
 * @author SNI
 */
public interface KeySanitizer {
    /**
     * Will sanitize a given cache key.
     * @param cacheKey
     *            the cache key to sanitize.
     * @return the sanitized cache key.
     */
    Object sanitizeKey(Object cacheKey) throws KeySanitationExcepion;

    /**
     * Will de-sanitize a given sanitized cache key.
     * @param sanitizedCacheKey
     *            the cache key to de-sanitize.
     * @return the de-sanitized cache key.
     */
    Object desanitizeKey(Object sanitizedCacheKey) throws KeySanitationExcepion;
}
