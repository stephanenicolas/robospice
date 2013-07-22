package com.octo.android.robospice.request.reporter;

import java.util.Set;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.observer.ObserverManager;
import com.octo.android.robospice.request.observer.RequestObserverFactory;

/**
 * Default implementation of RequestProgressReporterWithObserverSupport. It will notify listeners
 * on the ui thread and notify the Observer Manager of request changes
 * 
 * @author Andrew.Clark
 *
 */
public class DefaultRequestProgressReporterWithObserverSupport extends
        DefaultRequestProgressReporter implements
        RequestProgressReporterWithObserverSupport {

    private ObserverManager observerManager;

    /**
     * Called by the Spice Service when an Observer Manager has been created
     */
    @Override
    public void setObserverManager(ObserverManager observerManager) {
        this.observerManager = observerManager;
    }

    /**
     * Called by the Spice service to when an observer factory is being registered
     */
    @Override
    public void registerObserver(RequestObserverFactory observerFactory) {
        observerManager.registerObserver(observerFactory);
    }

    @Override
    public <T> void notifyListenersOfRequestAdded(
            CachedSpiceRequest<T> request, Set<RequestListener<?>> listeners) {

        observerManager.notifyObserversOfRequestAdded(request);

        super.notifyListenersOfRequestAdded(request, listeners);
    }

    @Override
    public <T> void notifyListenersOfRequestCancellation(
            CachedSpiceRequest<T> request, Set<RequestListener<?>> listeners) {

        observerManager.notifyObserversOfRequestCancellation(request);

        super.notifyListenersOfRequestCancellation(request, listeners);
    }

    @Override
    public <T> void notifyListenersOfRequestFailure(
            CachedSpiceRequest<T> request, SpiceException e,
            Set<RequestListener<?>> listeners) {

        observerManager.notifyObserversOfRequestFailure(request, e);

        super.notifyListenersOfRequestFailure(request, e, listeners);
    }

    @Override
    public <T> void notifyListenersOfRequestProgress(
            CachedSpiceRequest<T> request, Set<RequestListener<?>> listeners,
            RequestProgress progress) {

        observerManager.notifyObserversOfRequestProgress(request, progress);

        super.notifyListenersOfRequestProgress(request, listeners, progress);
    }

    @Override
    public <T> void notifyListenersOfRequestSuccess(
            final CachedSpiceRequest<T> request, final T result,
            final Set<RequestListener<?>> listeners) {

        observerManager.notifyObserversOfRequestSuccess(request, result);

        super.notifyListenersOfRequestSuccess(request, result, listeners);
    }
}
