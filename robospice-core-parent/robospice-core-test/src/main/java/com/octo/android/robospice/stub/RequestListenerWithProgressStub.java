package com.octo.android.robospice.stub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;

public class RequestListenerWithProgressStub<T> extends RequestListenerStub<T> implements RequestProgressListener {

    private boolean isComplete;
    protected Condition requestCompleteCondition = lock.newCondition();

    @Override
    public void onRequestFailure(SpiceException exception) {
        lock.lock();
        try {
            checkIsExectuedInUIThread();
            isSuccessful = false;
            this.exception = exception;
            if (isComplete) {
                requestFinishedCondition.signal();
            }
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
            if (isComplete) {
                requestFinishedCondition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onRequestProgressUpdate(RequestProgress progress) {
        lock.lock();
        try {
            if (progress.getStatus() == RequestStatus.COMPLETE) {
                isComplete = true;
                if (isSuccessful != null) {
                    requestFinishedCondition.signal();
                    requestCompleteCondition.signal();
                }
            }
        } finally {
            lock.unlock();
        }

    }

    public boolean isComplete() {
        return isComplete;
    }

    public void awaitComplete(long millisecond) throws InterruptedException {
        if (isComplete) {
            return;
        }
        lock.lock();
        try {
            requestCompleteCondition.await(millisecond, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

}
