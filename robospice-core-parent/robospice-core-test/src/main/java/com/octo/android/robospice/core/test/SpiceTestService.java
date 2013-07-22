package com.octo.android.robospice.core.test;

import android.app.Application;
import android.content.Context;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.request.observer.ObserverManager;
import com.octo.android.robospice.request.reporter.RequestProgressReporter;
import com.octo.android.robospice.stub.DoubleInMemoryPersisterStub;
import com.octo.android.robospice.stub.IntegerPersisterStub;
import com.octo.android.robospice.stub.StringPersisterStub;

/**
 * Only used to test RoboSpice. Will not rely on network state. Multi-threaded
 * spice service.
 * 
 * @author sni
 */
public class SpiceTestService extends SpiceService {

    private static final int TEST_THREAD_COUNT = 3;
    private static final int TEST_THREAD_PRIORITY = Thread.NORM_PRIORITY;
    private boolean observerManagerCreated = false;
    private boolean requestReporterCreated = false;

    @Override
    public CacheManager createCacheManager(Application application) {
        CacheManager cacheManager = new CacheManager();
        StringPersisterStub stringPersisterStub = new StringPersisterStub(application);
        IntegerPersisterStub integerPersisterStub = new IntegerPersisterStub(application);
        DoubleInMemoryPersisterStub doubleInMemoryPersisterStub = new DoubleInMemoryPersisterStub(application);
        cacheManager.addPersister(stringPersisterStub);
        cacheManager.addPersister(integerPersisterStub);
        cacheManager.addPersister(doubleInMemoryPersisterStub);
        return cacheManager;
    }

    @Override
    protected NetworkStateChecker getNetworkStateChecker() {
        return new NetworkStateChecker() {

            @Override
            public boolean isNetworkAvailable(Context context) {
                return true;
            }

            @Override
            public void checkPermissions(Context context) {
                // do nothing
            }
        };
    }

    @Override
    protected ObserverManager createObserverManager() {
        observerManagerCreated = true;

        return super.createObserverManager();
    }

    @Override
    protected RequestProgressReporter createRequestProgressReporter() {
        requestReporterCreated = true;

        return super.createRequestProgressReporter();
    }

    @Override
    public int getThreadCount() {
        return TEST_THREAD_COUNT;
    }

    @Override
    public int getThreadPriority() {
        return TEST_THREAD_PRIORITY;
    }

    @Override
    public boolean isFailOnCacheError() {
        return true;
    }

    public boolean isObserverManagerCreated() {
        return observerManagerCreated;
    }

    public boolean isRequestReporterCreated() {
        return requestReporterCreated;
    }
}
