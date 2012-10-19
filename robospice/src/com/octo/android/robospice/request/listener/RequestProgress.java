package com.octo.android.robospice.request.listener;

public class RequestProgress {

    private RequestStatus status;
    private float progress;

    public RequestProgress( RequestStatus status ) {
        this( status, 0 );
    }

    public RequestProgress( RequestStatus status, float progress ) {
        this.status = status;
        this.progress = progress;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus( RequestStatus status ) {
        this.status = status;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress( float progress ) {
        this.progress = progress;
    }

}
