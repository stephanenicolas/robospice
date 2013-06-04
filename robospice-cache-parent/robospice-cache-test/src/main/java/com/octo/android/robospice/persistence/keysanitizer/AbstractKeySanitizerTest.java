package com.octo.android.robospice.persistence.keysanitizer;

import android.test.AndroidTestCase;

import com.octo.android.robospice.persistence.exception.KeySanitationExcepion;
import com.octo.android.robospice.persistence.keysanitation.KeySanitizer;

public abstract class AbstractKeySanitizerTest extends AndroidTestCase {

    protected KeySanitizer keySanitizer;

    protected void setUp(KeySanitizer keySanitizer) throws Exception {
        super.setUp();
        this.keySanitizer = keySanitizer;
    }

    public void testSanitizeKey(Object expectedSanitizedKey, Object cacheKeyToSanitize) throws KeySanitationExcepion {
        // given

        // when
        Object sanitized = keySanitizer.sanitizeKey(cacheKeyToSanitize);

        // then
        assertEquals(expectedSanitizedKey, sanitized);
    }

    public void testDesanitizeKey(Object expectedDesanitizedKey, Object cacheKeyToDesanitize) throws KeySanitationExcepion {
        // given

        // when
        Object sanitized = keySanitizer.desanitizeKey(cacheKeyToDesanitize);

        // then
        assertEquals(expectedDesanitizedKey, sanitized);
    }

}
