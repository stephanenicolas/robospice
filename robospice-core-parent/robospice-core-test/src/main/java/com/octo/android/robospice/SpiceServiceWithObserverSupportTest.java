package com.octo.android.robospice;

import java.util.Map;

import org.easymock.EasyMock;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.core.test.SpiceTestService;
import com.octo.android.robospice.core.test.SpiceTestServiceWithObserverSupport;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestStatus;
import com.octo.android.robospice.request.observer.ObserverManager;
import com.octo.android.robospice.request.observer.ObserverTestHelper.MyObserverFactory;
import com.octo.android.robospice.request.observer.ObserversNotSupportedException;
import com.octo.android.robospice.request.tracker.RequestTrackerService;

//Thanks to http://stackoverflow.com/questions/2300029/servicetestcaset-getservice
@SmallTest
public class SpiceServiceWithObserverSupportTest extends
        ServiceTestCase<SpiceTestServiceWithObserverSupport> {

    public SpiceServiceWithObserverSupportTest() {
        super(SpiceTestServiceWithObserverSupport.class);
    }

    public void testRegisterObserverShouldBeOkIfReporterSupportsObservers()
        throws ObserversNotSupportedException {

        // when
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(),
                SpiceTestServiceWithObserverSupport.class);
        startService(startIntent);

        getService().registerObserver(
                new MyObserverFactory(null, 0, null, null, null, null));

        // passed
    }

    public void testObserverManagerShouldBeCreatedIfReporterSupportsObservers() {
        // when
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(),
                SpiceTestServiceWithObserverSupport.class);
        startService(startIntent);

        assertTrue(getService().isRequestReporterCreated());
        assertTrue(getService().isObserverManagerCreated());
    }

    public void testEnableRequestTrackingShouldSucceedIfReporterSupportsObservers() throws ObserversNotSupportedException {
        // when
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SpiceTestService.class);
        startService(startIntent);

        getService().enableRequestTracking();
    }

    public void testEnableRequestTrackingShouldCallRegisterObserverIfReporterSupportsObservers() throws ObserversNotSupportedException {
        // when
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SpiceTestService.class);
        startService(startIntent);

        ObserverManager observerManager = getService().getObserverManager();
        observerManager.registerObserver(RequestTrackerService.getRequestTrackerFactory());

        EasyMock.replay(observerManager);

        getService().enableRequestTracking();

        EasyMock.verify(observerManager);
    }

    public void testGetActiveRequestsShouldReturnEmptyMapIfReporterSupportsObserversAndWeEnabledRequestTracking() throws ObserversNotSupportedException {
        // when
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), SpiceTestService.class);
        startService(startIntent);

        getService().enableRequestTracking();

        Map<CachedSpiceRequest<?>, RequestStatus> activeRequests = getService().getActiveRequests();

        assertEquals(0, activeRequests.size());
    }
}
