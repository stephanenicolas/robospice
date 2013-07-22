package com.octo.android.robospice.core.test;

import com.octo.android.robospice.request.observer.ObserversNotSupportedException;
import com.octo.android.robospice.request.reporter.DefaultRequestProgressReporterWithObserverSupport;
import com.octo.android.robospice.request.reporter.RequestProgressReporter;

public class SpiceTestServiceWithObserverAndRequestTrackerSupport extends SpiceTestService {
    @Override
    public void onCreate() {
        super.onCreate();

        try {
            enableRequestTracking();
        } catch (ObserversNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected RequestProgressReporter createRequestProgressReporter() {
        // used to set the requestReporterCreated flag
        super.createRequestProgressReporter();

        return new DefaultRequestProgressReporterWithObserverSupport();
    }

    @Override
    public int getThreadCount() {
        return 1;
    }
}
