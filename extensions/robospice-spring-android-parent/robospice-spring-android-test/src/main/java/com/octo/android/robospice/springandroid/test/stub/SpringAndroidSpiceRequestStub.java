package com.octo.android.robospice.springandroid.test.stub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;
import com.octo.android.robospice.springandroid.test.model.json.Weather;

public class SpringAndroidSpiceRequestStub extends
    SpringAndroidSpiceRequest<Weather> {
    private ReentrantLock reentrantLock = new ReentrantLock();
    private Condition loadDataFromNetworkHasBeenExecuted = reentrantLock
        .newCondition();

    public SpringAndroidSpiceRequestStub(Class<Weather> clazz) {
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
            loadDataFromNetworkHasBeenExecuted.await(timeout,
                TimeUnit.MILLISECONDS);
        } finally {
            reentrantLock.unlock();
        }
    }

}
