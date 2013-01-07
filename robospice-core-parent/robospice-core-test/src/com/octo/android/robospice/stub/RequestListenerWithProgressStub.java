package com.octo.android.robospice.stub;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;

public class RequestListenerWithProgressStub<T> extends RequestListenerStub<T>
    implements RequestProgressListener {

    private boolean isComplete;

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
                }
            }
        } finally {
            lock.unlock();
        }

    }

    public boolean isComplete() {
        return isComplete;
    }

}
