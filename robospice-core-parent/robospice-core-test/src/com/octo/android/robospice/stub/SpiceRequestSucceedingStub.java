package com.octo.android.robospice.stub;

public final class SpiceRequestSucceedingStub<T> extends SpiceRequestStub<T> {
    private T returnedData;
    private long sleepTimeBeforeAnswering = 0;

    public SpiceRequestSucceedingStub(Class<T> clazz, T returnedData) {
        this(clazz, returnedData, 0);
    }

    public SpiceRequestSucceedingStub(Class<T> clazz, T returnedData,
        long sleepTimeBeforeAnswering) {
        super(clazz);
        this.returnedData = returnedData;
        this.sleepTimeBeforeAnswering = sleepTimeBeforeAnswering;
    }

    @Override
    public T loadDataFromNetwork() throws Exception {
        isLoadDataFromNetworkCalled = true;
        signalStopWaiting();
        if (sleepTimeBeforeAnswering != 0) {
            Thread.sleep(sleepTimeBeforeAnswering);
        }
        return returnedData;
    }
}
