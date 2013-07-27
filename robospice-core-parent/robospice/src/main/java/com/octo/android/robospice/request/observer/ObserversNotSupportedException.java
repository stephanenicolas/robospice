package com.octo.android.robospice.request.observer;

/**
 * Exception thrown when observer support is required but has not been
 * implemented
 * @author Andrew.Clark
 */
public class ObserversNotSupportedException extends Exception {
    private static final long serialVersionUID = -3208517238791526626L;

    public ObserversNotSupportedException() {
        super("You've tried registering an Observer but you also need to override createRequestProgressReporter and return a reporter like DefaultRequestProgressReporterWithObserverSupport");
    }
}
