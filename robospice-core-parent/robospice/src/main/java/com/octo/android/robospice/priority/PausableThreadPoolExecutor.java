package com.octo.android.robospice.priority;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class is used internally. It makes testing easier.
 * @author SNI
 */
public class PausableThreadPoolExecutor extends ThreadPoolExecutor {

    public PausableThreadPoolExecutor(int poolSize) {
        super(poolSize, poolSize, 0, TimeUnit.NANOSECONDS, new PausablePriorityBlockingQueue<Runnable>());
    }

    public void pause() {
        ((PausablePriorityBlockingQueue<Runnable>) getQueue()).pause();
    }

    public void resume() {
        ((PausablePriorityBlockingQueue<Runnable>) getQueue()).resume();
    }
}
