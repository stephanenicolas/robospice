package com.octo.android.robospice.stub;

import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;
import com.octo.android.robospice.request.listener.RequestStatus;

public class RequestListenerWithProgressStub< T > extends RequestListenerStub< T > implements RequestProgressListener {

    private boolean isComplete;

    @Override
    public void onRequestProgressUpdate( RequestProgress progress ) {
        if ( progress.getStatus() == RequestStatus.COMPLETE ) {
            isComplete = true;
        }
    }

    public boolean isComplete() {
        return isComplete;
    }

}