package com.octo.android.robospice.persistence.keysanitizer;

import com.octo.android.robospice.persistence.keysanitation.DefaultKeySanitizer;

public class DefaultKeySanitizerTest extends AbstractKeySanitizerTest {

    private static final String TEST_UNSANITIZED_KEY = "foo and bar";
    private static final String TEST_SANITIZED_KEY = "Zm9vIGFuZCBiYXI";
    private static final Object TEST_BAD_KEY = new Object();

    @Override
    protected void setUp() throws Exception {
        super.setUp(new DefaultKeySanitizer());
    }

    public void testSanitizeKey() {
        super.testSanitizeKey(TEST_SANITIZED_KEY, TEST_UNSANITIZED_KEY);
    }

    public void testDesanitizeKey() {
        super.testDesanitizeKey(TEST_UNSANITIZED_KEY, TEST_SANITIZED_KEY);
    }

    public void testSanitizeKey_should_throw_exception_when_cacheKey_is_not_a_string() {
        // given

        // when
        try {
            keySanitizer.sanitizeKey(TEST_BAD_KEY);
            fail();
        } catch (RuntimeException ex) {
            // check style happy
            assertTrue(true);
        }

        // then
    }

    public void testDesanitizeKey_should_throw_exception_when_cacheKey_is_not_a_string() {
        // given

        // when
        try {
            keySanitizer.desanitizeKey(TEST_BAD_KEY);
            fail();
        } catch (RuntimeException ex) {
            // check style happy
            assertTrue(true);
        }

        // then
    }

}
