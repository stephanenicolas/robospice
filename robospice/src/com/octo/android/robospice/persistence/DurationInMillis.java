package com.octo.android.robospice.persistence;

/**
 * Utility interface to write duration, used to ease maximum time a value is considered value in the cache before
 * expiring.
 * 
 * @author sni
 * 
 */
public interface DurationInMillis {

    public static final long ALWAYS = 0;
    public static final long ONE_SECOND = 1000;
    public static final long ONE_MINUTE = 60 * ONE_SECOND;
    public static final long ONE_HOUR = 60 * ONE_MINUTE;
    public static final long ONE_DAY = 24 * ONE_HOUR;
    public static final long ONE_WEEK = 7 * ONE_DAY;

}
