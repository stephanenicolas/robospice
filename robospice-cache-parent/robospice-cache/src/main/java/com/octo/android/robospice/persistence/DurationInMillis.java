package com.octo.android.robospice.persistence;

/**
 * Utility interface to write durations. Its values can be used as helpers for
 * the SpiceManager.execute method.
 * @author sni
 */
public interface DurationInMillis {

    /**
     * Data in cache will never be returned, a network call will always be
     * performed.
     */
    @Deprecated
    long NEVER = -1;

    /**
     * Data in cache will never be returned, a network call will always be
     * performed.
     */
    long ALWAYS_EXPIRED = -1;

    /**
     * Data in cache will always be returned. Thus a network call will only be
     * performed once.
     */
    @Deprecated
    long ALWAYS = 0;

    /**
     * Data in cache will always be returned. Thus a network call will only be
     * performed once.
     */
    long ALWAYS_RETURNED = 0;

    long ONE_SECOND = 1000;
    long ONE_MINUTE = 60 * ONE_SECOND;
    long ONE_HOUR = 60 * ONE_MINUTE;
    long ONE_DAY = 24 * ONE_HOUR;
    long ONE_WEEK = 7 * ONE_DAY;

}
