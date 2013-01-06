package com.octo.android.robospice.exception;

import com.octo.android.robospice.persistence.exception.SpiceException;

/**
 * Exception thrown when there is no available data connection.
 * @author sni
 */
public class NoNetworkException extends SpiceException {

    private static final long serialVersionUID = 5365883691014039322L;

    public NoNetworkException() {
        super("Network is not available");
    }

}
