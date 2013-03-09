package com.octo.android.robospice.stub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Looper;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

public class RequestListenerStub<T> implements RequestListener<T> {

    protected Boolean isSuccessful = null;
    protected boolean isExecutedInUIThread = false;

    protected ReentrantLock lock = new ReentrantLock();
    protected Condition requestFinishedCondition = lock.newCondition();
    protected Exception exception;

    @Override
    public void onRequestFailure(SpiceException exception) {
        lock.lock();
        try {
            checkIsExectuedInUIThread();
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
            checkIsExectuedInUIThread();
            isSuccessful = true;
            requestFinishedCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    protected void checkIsExectuedInUIThread() {
        if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
            isExecutedInUIThread = true;
        }
    }

    public Boolean isSuccessful() {
        return isSuccessful;
    }

    public Exception getReceivedException() {
        return exception;
    }

    public boolean isExecutedInUIThread() {
        return isExecutedInUIThread;
    }

    public void await(long millisecond) throws InterruptedException {
        lock.lock();
        try {
            if (isSuccessful != null) {
                return;
            }
            requestFinishedCondition.await(millisecond, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public void resetSuccess() {
        this.isSuccessful = null;
    }
}
