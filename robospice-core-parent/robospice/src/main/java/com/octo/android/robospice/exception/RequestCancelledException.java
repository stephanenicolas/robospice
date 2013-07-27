package com.octo.android.robospice.exception;

import com.octo.android.robospice.persistence.exception.SpiceException;

/**
 * Exception thrown when a problem occurs while retrieving data from network.
 * @author sni
 */
public class RequestCancelledException extends SpiceException {

    private static final long serialVersionUID = 5790006264835400721L;

    public RequestCancelledException(final String detailMessage) {
        super(detailMessage);
    }

    public RequestCancelledException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RequestCancelledException(final Throwable throwable) {
        super(throwable);
    }

}
