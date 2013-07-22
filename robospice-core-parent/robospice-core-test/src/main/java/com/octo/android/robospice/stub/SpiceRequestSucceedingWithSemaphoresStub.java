package com.octo.android.robospice.stub;

import java.util.concurrent.Semaphore;

import roboguice.util.temp.Ln;

public final class SpiceRequestSucceedingWithSemaphoresStub<T> extends SpiceRequestStub<T> {
    private T returnedData;
    private Semaphore requestStarted = new Semaphore(1);
    private Semaphore allowRequestToFinish = new Semaphore(1);

    public SpiceRequestSucceedingWithSemaphoresStub(Class<T> clazz, T returnedData) throws InterruptedException {
        super(clazz);
        this.returnedData = returnedData;

        // Acquire request started as we haven't yet 
        requestStarted.acquire();

        // Acquire allowRequestToFinish as we want to be told when we can 
        allowRequestToFinish.acquire();
    }

    @Override
    public T loadDataFromNetwork() throws Exception {
        isLoadDataFromNetworkCalled = true;

        // We are now in loadDataFromNetwork status so inform anyone waiting for us
        Ln.d("Request is now in LoadDataFromNetwork");
        requestStarted.release();

        // wait to be told to finish
        Ln.d("Request is waiting to be told to finish");
        allowRequestToFinish.acquire();

        return returnedData;
    }

    public void waitForLoadFromNetwork() throws InterruptedException {
        requestStarted.acquire();
    }

    public void allowRequestToFinish() {
        allowRequestToFinish.release();
    }
}
