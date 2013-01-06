package com.octo.android.robospice.persistence;

/**
 * Utility interface to write duration, used to ease maximum time a value is
 * considered value in the cache before expiring.
 * @author sni
 */
public interface DurationInMillis {

    long NEVER = -1;
    long ALWAYS = 0;
    long ONE_SECOND = 1000;
    long ONE_MINUTE = 60 * ONE_SECOND;
    long ONE_HOUR = 60 * ONE_MINUTE;
    long ONE_DAY = 24 * ONE_HOUR;
    long ONE_WEEK = 7 * ONE_DAY;

}
