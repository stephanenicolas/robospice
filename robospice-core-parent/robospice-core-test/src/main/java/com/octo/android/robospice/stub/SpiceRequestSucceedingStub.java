package com.octo.android.robospice.stub;

import roboguice.util.temp.Ln;

public final class SpiceRequestSucceedingStub<T> extends SpiceRequestStub<T> {
    private T returnedData;
    private long sleepTimeBeforeAnswering = 0;

    public SpiceRequestSucceedingStub(Class<T> clazz, T returnedData) {
        this(clazz, returnedData, 0);
    }

    public SpiceRequestSucceedingStub(Class<T> clazz, T returnedData, long sleepTimeBeforeAnswering) {
        super(clazz);
        this.returnedData = returnedData;
        this.sleepTimeBeforeAnswering = sleepTimeBeforeAnswering;
    }

    @Override
    public T loadDataFromNetwork() throws Exception {
        isLoadDataFromNetworkCalled = true;
        signalStopWaiting();
        if (sleepTimeBeforeAnswering != 0) {
            try {
                Thread.sleep(sleepTimeBeforeAnswering);
            } catch (InterruptedException e) {
                Ln.d(e, "Interrupted while sleeping.");
            }
        }
        return returnedData;
    }
}
