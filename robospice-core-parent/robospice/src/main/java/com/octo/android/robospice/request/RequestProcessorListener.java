package com.octo.android.robospice.request;

public interface RequestProcessorListener {
    void requestsInProgress();

    void allRequestComplete();
}
