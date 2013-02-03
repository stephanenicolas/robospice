package com.octo.android.robospice.retrofit.test.stub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;
import com.octo.android.robospice.retrofit.test.model.WeatherResult;

public class RetrofitSpiceRequestStub extends RetrofitSpiceRequest< WeatherResult > {
    private ReentrantLock reentrantLock = new ReentrantLock();
    private Condition loadDataFromNetworkHasBeenExecuted = reentrantLock.newCondition();

    public RetrofitSpiceRequestStub( Class< WeatherResult > clazz ) {
        super( clazz );
    }

    @Override
    public WeatherResult loadDataFromNetwork() throws Exception {
        try {
            reentrantLock.lock();
            loadDataFromNetworkHasBeenExecuted.signal();
        } finally {
            reentrantLock.unlock();
        }
        return new WeatherResult();
    }

    public void await( long timeout ) throws InterruptedException {
        try {
            reentrantLock.lock();
            loadDataFromNetworkHasBeenExecuted.await( timeout, TimeUnit.MILLISECONDS );
        } finally {
            reentrantLock.unlock();
        }
    }

}
