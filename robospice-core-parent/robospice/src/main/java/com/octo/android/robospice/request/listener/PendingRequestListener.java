package com.octo.android.robospice.request.listener;

/**
 * Listens to a SpiceRequest that may be pending, or not. It will be notified of
 * request's result if such a request is pending, otherwise it will notified
 * that such a request is not currently pending.
 */

public interface PendingRequestListener<RESULT> extends RequestListener<RESULT> {
    void onRequestNotFound();
}
