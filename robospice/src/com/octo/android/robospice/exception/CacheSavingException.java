package com.octo.android.robospice.exception;

import com.octo.android.robospice.ContentManager;
import com.octo.android.robospice.ContentService;

/**
 * Exception thrown when a problem occurs while saving data to cache. Those exceptions are not thrown by default in the
 * framework.
 * 
 * @see ContentManager#setFailOnCacheError(boolean)
 * @see ContentService#setFailOnCacheError(boolean)
 * @author sni
 * 
 */
public class CacheSavingException extends ContentManagerException {

    private static final long serialVersionUID = -633402253089445891L;

    public CacheSavingException( String detailMessage ) {
        super( detailMessage );
    }

    public CacheSavingException( String detailMessage, Throwable throwable ) {
        super( detailMessage, throwable );
    }

    public CacheSavingException( Throwable throwable ) {
        super( throwable );
    }

}
