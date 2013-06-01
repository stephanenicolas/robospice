package com.octo.android.robospice.persistence.keysanitation;

import java.nio.charset.Charset;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Base64;

/**
 * Uses base 64 to sanitize/de-sanitize keys. Only applies to cache keys that
 * are strings. Keys sanitized by this class can be safely used to create file
 * names (and urls).
 * @author SNI
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class DefaultKeySanitizer implements KeySanitizer {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------
    private static final int BASE64_FLAGS = Base64.URL_SAFE + Base64.NO_WRAP + Base64.NO_PADDING;
    private static final Charset UTF8_CHARSET = Charset.forName("utf-8");

    // ----------------------------------
    // API
    // ----------------------------------

    @Override
    public Object sanitizeKey(Object cacheKey) {
        if (!(cacheKey instanceof String)) {
            throw new RuntimeException(DefaultKeySanitizer.class.getSimpleName() + " can only be used with Strings cache keys.");
        }
        return Base64.encodeToString(((String) cacheKey).getBytes(UTF8_CHARSET), BASE64_FLAGS);
    }

    @Override
    public Object desanitizeKey(Object sanitzedCacheKey) {
        if (!(sanitzedCacheKey instanceof String)) {
            throw new RuntimeException(DefaultKeySanitizer.class.getSimpleName() + " can only be used with Strings cache keys.");
        }
        return new String(Base64.decode((String) sanitzedCacheKey, BASE64_FLAGS), UTF8_CHARSET);
    }
}
