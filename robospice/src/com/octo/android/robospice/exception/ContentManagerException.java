package com.octo.android.robospice.exception;

/**
 * Super class of all exceptions in the framework.
 * 
 * @author sni
 * 
 */
public class ContentManagerException extends Exception {

    private static final long serialVersionUID = 4494147890739338461L;

    public ContentManagerException( String detailMessage ) {
        super( detailMessage );
    }

    public ContentManagerException( String detailMessage, Throwable throwable ) {
        super( detailMessage, throwable );
    }

    public ContentManagerException( Throwable throwable ) {
        super( throwable );
    }

}
