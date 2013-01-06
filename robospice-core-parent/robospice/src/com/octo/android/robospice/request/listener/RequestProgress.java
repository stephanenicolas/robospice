package com.octo.android.robospice.request.listener;

public class RequestProgress {

    private RequestStatus status;
    private float progress;

    public RequestProgress(final RequestStatus status) {
        this(status, 0);
    }

    public RequestProgress(final RequestStatus status, final float progress) {
        this.status = status;
        this.progress = progress;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(final RequestStatus status) {
        this.status = status;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(final float progress) {
        this.progress = progress;
    }

}
