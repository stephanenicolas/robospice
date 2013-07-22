package com.octo.android.robospice.core.test;

import org.easymock.EasyMock;

import com.octo.android.robospice.request.observer.ObserverManager;
import com.octo.android.robospice.request.reporter.DefaultRequestProgressReporterWithObserverSupport;
import com.octo.android.robospice.request.reporter.RequestProgressReporter;

/**
 * Only used to test RoboSpice. Will not rely on network state. Multi-threaded
 * spice service.
 * 
 * @author Andrew Clark
 */
public class SpiceTestServiceWithObserverSupport extends SpiceTestService {

    private ObserverManager mockObserverManager;

    @Override
    protected RequestProgressReporter createRequestProgressReporter() {
        // used to set the requestReporterCreated flag
        super.createRequestProgressReporter();

        return new DefaultRequestProgressReporterWithObserverSupport();
    }

    @Override
    protected ObserverManager createObserverManager() {
        // used to set the observerManagerCreated flag
        super.createObserverManager();

        mockObserverManager = EasyMock.createNiceMock(ObserverManager.class);
        return mockObserverManager;
    }

    public ObserverManager getObserverManager() {
        return mockObserverManager;
    }
}
