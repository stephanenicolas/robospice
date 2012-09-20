package com.octo.android.robospice.stub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Looper;

import com.octo.android.robospice.exception.ContentManagerException;
import com.octo.android.robospice.request.RequestListener;

public class RequestListenerStub< T > implements RequestListener< T > {

    private Boolean isSuccessful = null;
    private boolean isExecutedInUIThread = false;

    private ReentrantLock lock = new ReentrantLock();
    private Condition requestFinishedCondition = lock.newCondition();
    private Exception exception;

    @Override
    public void onRequestFailure( ContentManagerException exception ) {
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
    public void onRequestSuccess( T arg0 ) {
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
        if ( Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper() ) {
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

    public void await( long millisecond ) throws InterruptedException {
        lock.lock();
        try {
            requestFinishedCondition.await( millisecond, TimeUnit.MILLISECONDS );
        } finally {
            lock.unlock();
        }
    }
}