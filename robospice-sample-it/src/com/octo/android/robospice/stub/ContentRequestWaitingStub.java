package com.octo.android.robospice.stub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ContentRequestWaitingStub< T > extends ContentRequestStub< T > {
    protected boolean isLoadDataFromNetworkCalled = false;

    private ReentrantLock lock = new ReentrantLock();
    private Condition requestFinishedCondition = lock.newCondition();

    private long waitBeforeExecution;

    public ContentRequestWaitingStub( Class< T > clazz ) {
        this( clazz, 0 );
    }

    public ContentRequestWaitingStub( Class< T > clazz, long waitBeforeExecution ) {
        super( clazz );
        this.waitBeforeExecution = waitBeforeExecution;
    }

    @Override
    public boolean isLoadDataFromNetworkCalled() {
        lock.lock();
        try {
            requestFinishedCondition.signal();
        } finally {
            lock.unlock();
        }
        return isLoadDataFromNetworkCalled;
    }

    @Override
    public T loadDataFromNetwork() throws Exception {
        lock.lock();
        isLoadDataFromNetworkCalled = true;
        try {
            requestFinishedCondition.signal();
        } finally {
            lock.unlock();
        }
        if ( waitBeforeExecution != 0 ) {
            Thread.sleep( waitBeforeExecution );
        }
        return null;
    }

    @Override
    public void await( long millisecond ) throws InterruptedException {
        lock.lock();
        try {
            requestFinishedCondition.await( millisecond, TimeUnit.MILLISECONDS );
        } finally {
            lock.unlock();
        }
    }
}