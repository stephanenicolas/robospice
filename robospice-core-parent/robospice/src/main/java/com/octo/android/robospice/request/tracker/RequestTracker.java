package com.octo.android.robospice.request.tracker;

import java.util.Map;

import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestStatus;

/**
 * Request Tracker provides access to the currently active requests
 * @author Andrew.Clark
 *
 */
public interface RequestTracker {

    Map<CachedSpiceRequest<?>, RequestStatus> getActiveRequests();

}
