package com.octo.android.robospice.core.test;

import android.app.Application;
import android.content.Context;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.stub.IntegerPersisterStub;
import com.octo.android.robospice.stub.StringPersisterStub;

/**
 * Only used to test RoboSpice. Will not rely on network state. Multi-threaded
 * spice service.
 * @author sni
 */
public class SpiceTestService extends SpiceService {

    private static final int TEST_THREAD_COUNT = 3;

    @Override
    public CacheManager createCacheManager(Application application) {
        CacheManager cacheManager = new CacheManager();
        StringPersisterStub stringPersisterStub = new StringPersisterStub(application);
        IntegerPersisterStub integerPersisterStub = new IntegerPersisterStub(application);
        cacheManager.addPersister(stringPersisterStub);
        cacheManager.addPersister(integerPersisterStub);
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
    public int getThreadCount() {
        return TEST_THREAD_COUNT;
    }

    @Override
    public boolean isFailOnCacheError() {
        return true;
    }

}
