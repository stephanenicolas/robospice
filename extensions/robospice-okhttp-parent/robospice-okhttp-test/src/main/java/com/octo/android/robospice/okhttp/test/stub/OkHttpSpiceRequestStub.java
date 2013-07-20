package com.octo.android.robospice.okhttp.test.stub;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.octo.android.robospice.request.okhttp.OkHttpSpiceRequest;

public class OkHttpSpiceRequestStub extends OkHttpSpiceRequest<String> {
    private ReentrantLock reentrantLock = new ReentrantLock();
    private Condition loadDataFromNetworkHasBeenExecuted = reentrantLock.newCondition();

    public OkHttpSpiceRequestStub() {
        super(String.class);
    }

    @Override
    public String loadDataFromNetwork() throws Exception {
        try {
            reentrantLock.lock();
            loadDataFromNetworkHasBeenExecuted.signal();
        } finally {
            reentrantLock.unlock();
        }
        return "";
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
