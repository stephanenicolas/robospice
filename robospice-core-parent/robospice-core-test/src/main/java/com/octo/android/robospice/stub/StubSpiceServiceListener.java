package com.octo.android.robospice.stub;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import roboguice.util.temp.Ln;

import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.SpiceServiceServiceListener;

public class StubSpiceServiceListener implements SpiceServiceServiceListener {

    protected ReentrantLock lock = new ReentrantLock();
    protected Condition requestFinishedCondition = lock.newCondition();

    @Override
    public void onRequestProcessed(CachedSpiceRequest<?> cachedSpiceRequest) {
        Ln.d(cachedSpiceRequest + " processed");
    }

}
