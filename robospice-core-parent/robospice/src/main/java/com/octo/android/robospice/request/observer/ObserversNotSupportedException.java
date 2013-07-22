package com.octo.android.robospice.request.observer;

/**
 * Exception thrown when observer support is required but has not been implemented
 * @author Andrew.Clark
 *
 */
public class ObserversNotSupportedException extends Exception {
    public ObserversNotSupportedException() {
        super("You've tried registering an Observer but you also need to override createRequestProgressReporter and return a reporter like DefaultRequestProgressReporterWithObserverSupport");
    }
}
