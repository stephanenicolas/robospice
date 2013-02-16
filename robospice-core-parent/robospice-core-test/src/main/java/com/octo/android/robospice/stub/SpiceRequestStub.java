package com.octo.android.robospice.stub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.octo.android.robospice.request.SpiceRequest;

/**
 * A {@link SpiceRequest} that is stubbed to provide testable state. Typically,
 * when testing parts of RoboSpice, we will create a request and send it to the
 * engine. As the engine is asynchronous the request is processed in a different
 * thread than the testing thread. This stub allows to wait for request to be
 * completed to make assertions in the test. It will also provide testable
 * states (knowing if methods have been called or not).
 * @author sni
 * @param <T>
 *            the type of the request's result.
 */
public abstract class SpiceRequestStub<T> extends SpiceRequest<T> {
    /** Whether {@link #loadDataFromNetwork()} has been called or not. */
    protected boolean isLoadDataFromNetworkCalled = false;
    /** Synchronizes access to {@link #requestFinishedCondition}. */
    private ReentrantLock lock = new ReentrantLock();
    /** Indicates whether or not request can be considered as finished. */
    private Condition requestFinishedCondition = lock.newCondition();

    public SpiceRequestStub(Class<T> clazz) {
        super(clazz);
    }

    protected void signalStopWaiting() {
        lock.lock();
        try {
            requestFinishedCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return a boolean to indicate whether or not the
     *         {@link #loadDataFromNetwork()} method has been called.
     */
    public boolean isLoadDataFromNetworkCalled() {
        signalStopWaiting();
        return isLoadDataFromNetworkCalled;
    }

    public void awaitForLoadDataFromNetworkIsCalled(long millisecond) throws InterruptedException {
        if (isLoadDataFromNetworkCalled) {
            return;
        }
        lock.lock();
        try {
            requestFinishedCondition.await(millisecond, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }
}
