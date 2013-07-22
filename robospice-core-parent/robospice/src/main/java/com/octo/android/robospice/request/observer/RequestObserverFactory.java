package com.octo.android.robospice.request.observer;

import com.octo.android.robospice.request.CachedSpiceRequest;

/**
 * Request Observer Factory that is registered with the Observer Manager and is informed when a new request has been added
 * Request Observer Factories can decide which requests they want to observe
 * 
 * @author Andrew.Clark
 *
 */
public interface RequestObserverFactory {

    /**
     * When a request has been created the Observer Factory is asked whether it wants to observe this request
     * 
     * @param request
     * @return RequestObserver or null if no observation is required for this Observer Factory
     */
    RequestObserver create(CachedSpiceRequest<?> request);

}
