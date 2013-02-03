package com.octo.android.robospice.persistence.exception;

/**
 * Super class of all exceptions in RoboSpice.
 * @author sni
 */
public class SpiceException extends Exception {

    private static final long serialVersionUID = 4494147890739338461L;

    public SpiceException(String detailMessage) {
        super(detailMessage);
    }

    public SpiceException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SpiceException(Throwable throwable) {
        super(throwable);
    }

}
