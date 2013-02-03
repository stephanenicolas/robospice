package com.octo.android.robospice.exception;

import com.octo.android.robospice.persistence.exception.SpiceException;

/**
 * Exception thrown when a problem occurs while retrieving data from network.
 * @author sni
 */
public class NetworkException extends SpiceException {

    private static final long serialVersionUID = 5751706264835400721L;

    public NetworkException(final String detailMessage) {
        super(detailMessage);
    }

    public NetworkException(final String detailMessage,
        final Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NetworkException(final Throwable throwable) {
        super(throwable);
    }

}
