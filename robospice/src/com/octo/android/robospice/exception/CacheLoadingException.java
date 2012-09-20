package com.octo.android.robospice.exception;

import com.octo.android.robospice.ContentManager;
import com.octo.android.robospice.ContentService;

/**
 * Exception thrown when a problem occurs while loading data from cache. Those exceptions are not thrown by default in
 * the framework.
 * 
 * @see ContentManager#setFailOnCacheError(boolean)
 * @see ContentService#setFailOnCacheError(boolean)
 * @author sni
 * 
 */
public class CacheLoadingException extends ContentManagerException {

    private static final long serialVersionUID = -1821941621446511524L;

    public CacheLoadingException( String detailMessage ) {
        super( detailMessage );
    }

    public CacheLoadingException( String detailMessage, Throwable throwable ) {
        super( detailMessage, throwable );
    }

    public CacheLoadingException( Throwable throwable ) {
        super( throwable );
    }

}
