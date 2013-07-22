package com.octo.android.robospice.request.reporter;

import com.octo.android.robospice.request.observer.ObserverManager;
import com.octo.android.robospice.request.observer.RequestObserverFactory;

/**
 * Defines the behaviour of a Request Reporter which supports observers.
 * An instance of a class supporting this interface is required before observers can be used 
 * @author Andrew.Clark
 *
 */
public interface RequestProgressReporterWithObserverSupport extends RequestProgressReporter {

    /**
     * Called by the SpiceService when an Observer Manager has been created
     * @param observerManager
     */
    void setObserverManager(ObserverManager observerManager);

    /**
     * Called by the SpiceService when an Observer Factory is being registered
     * @param observerFactory
     */
    void registerObserver(RequestObserverFactory observerFactory);
}
