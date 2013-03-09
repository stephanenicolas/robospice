package com.octo.android.robospice.stub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;

public class RequestListenerWithProgressStub<T> extends RequestListenerStub<T> implements RequestProgressListener {

    private boolean isComplete;
    protected ReentrantLock lockComplete = new ReentrantLock();
    protected Condition requestCompleteCondition = lockComplete.newCondition();

    @Override
    public void onRequestProgressUpdate(RequestProgress progress) {
        lockComplete.lock();
        try {
            if (progress.getStatus() == RequestStatus.COMPLETE) {
                isComplete = true;
                requestCompleteCondition.signal();
            }
        } finally {
            lockComplete.unlock();
        }
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void awaitComplete(long millisecond) throws InterruptedException {
        lockComplete.lock();
        try {
            if (isComplete) {
                return;
            }
            requestCompleteCondition.await(millisecond, TimeUnit.MILLISECONDS);
        } finally {
            lockComplete.unlock();
        }
    }

}
