package com.octo.android.robospice.spicelist.test.stub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class RequestListenerStub<T> implements RequestListener<T> {

    protected Boolean isSuccessful = null;

    protected ReentrantLock lock = new ReentrantLock();
    protected Condition requestFinishedCondition = lock.newCondition();
    protected Exception exception;

    @Override
    public void onRequestFailure(SpiceException exception) {
        lock.lock();
        try {
            isSuccessful = false;
            this.exception = exception;
            requestFinishedCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onRequestSuccess(T arg0) {
        lock.lock();
        try {
            isSuccessful = true;
            requestFinishedCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    public Boolean isSuccessful() {
        return isSuccessful;
    }

    public Exception getReceivedException() {
        return exception;
    }

    public void await(long millisecond) throws InterruptedException {
        lock.lock();
        try {
            requestFinishedCondition.await(millisecond, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }
}
