package com.octo.android.robospice.stub;

import roboguice.util.temp.Ln;

public final class SpiceRequestFailingStub<T> extends SpiceRequestStub<T> {

    private long sleepTimeBeforeAnswering = 0;

    public SpiceRequestFailingStub(Class<T> clazz) {
        super(clazz);
    }

    public SpiceRequestFailingStub(Class<T> clazz, long sleepTimeBeforeAnswering) {
        super(clazz);
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
        throw new Exception();
    }

}
