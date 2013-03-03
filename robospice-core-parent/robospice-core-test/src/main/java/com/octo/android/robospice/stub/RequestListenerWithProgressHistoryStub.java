package com.octo.android.robospice.stub;

import java.util.ArrayList;
import java.util.List;

import com.octo.android.robospice.request.listener.RequestProgress;

public class RequestListenerWithProgressHistoryStub<T> extends RequestListenerWithProgressStub<T> {

    private List<RequestProgress> requestProgressesHistory = new ArrayList<RequestProgress>();

    @Override
    public void onRequestProgressUpdate(RequestProgress progress) {
        super.onRequestProgressUpdate(progress);
        requestProgressesHistory.add(progress);
    }

    public List<RequestProgress> getRequestProgressesHistory() {
        return requestProgressesHistory;
    }
}
