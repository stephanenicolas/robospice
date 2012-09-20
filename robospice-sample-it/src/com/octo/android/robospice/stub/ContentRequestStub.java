package com.octo.android.robospice.stub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.octo.android.robospice.request.ContentRequest;

public abstract class ContentRequestStub< T > extends ContentRequest< T > {
    protected boolean isLoadDataFromNetworkCalled = false;

    private ReentrantLock lock = new ReentrantLock();
    private Condition requestFinishedCondition = lock.newCondition();

    public ContentRequestStub( Class< T > clazz ) {
        super( clazz );
    }

    public boolean isLoadDataFromNetworkCalled() {
        lock.lock();
        try {
            requestFinishedCondition.signal();
        } finally {
            lock.unlock();
        }
        return isLoadDataFromNetworkCalled;
    }

    public void await( long millisecond ) throws InterruptedException {
        lock.lock();
        try {
            requestFinishedCondition.await( millisecond, TimeUnit.MILLISECONDS );
        } finally {
            lock.unlock();
        }
    }
}