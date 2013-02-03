package com.octo.android.robospice.persistence.exception;

/**
 * Exception thrown when a problem occurs while loading data from cache. Those
 * exceptions are not thrown by default in the framework.
 * @author sni
 */
public class CacheLoadingException extends SpiceException {

    private static final long serialVersionUID = -1821941621446511524L;

    public CacheLoadingException(String detailMessage) {
        super(detailMessage);
    }

    public CacheLoadingException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CacheLoadingException(Throwable throwable) {
        super(throwable);
    }

}
