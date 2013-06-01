package com.octo.android.robospice.googlehttpclient.test.stub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.octo.android.robospice.googlehttpclient.test.model.Weather;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

public class GoogleHttpClientSpiceRequestStub extends GoogleHttpClientSpiceRequest<Weather> {
    private ReentrantLock reentrantLock = new ReentrantLock();
    private Condition loadDataFromNetworkHasBeenExecuted = reentrantLock.newCondition();

    public GoogleHttpClientSpiceRequestStub(Class<Weather> clazz) {
        super(clazz);
    }

    @Override
    public Weather loadDataFromNetwork() throws Exception {
        try {
            reentrantLock.lock();
            loadDataFromNetworkHasBeenExecuted.signal();
        } finally {
            reentrantLock.unlock();
        }
        return new Weather();
    }

    public void await(long timeout) throws InterruptedException {
        try {
            reentrantLock.lock();
            loadDataFromNetworkHasBeenExecuted.await(timeout, TimeUnit.MILLISECONDS);
        } finally {
            reentrantLock.unlock();
        }
    }

}
