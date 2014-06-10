package com.octo.android.robospice.priority;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class is used internally. It makes testing easier.
 * @author SNI
 */
public class PausableThreadPoolExecutor extends ThreadPoolExecutor {

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------
    /**
     * Creates a thread pool executor with a
     * {@link PausablePriorityBlockingQueue}.
     * @param corePoolSize
     *            the size of the pool of threads.
     * @param maxPoolSize
     *            the size of the pool of threads.
     * @param threadPriority
     *            the priority of threads created as defined by
     *            {@link Thread#setPriority(int)}.
     */
    public PausableThreadPoolExecutor(int corePoolSize, int maxPoolSize, int threadPriority) {
        super(corePoolSize, maxPoolSize, 0, TimeUnit.NANOSECONDS, new PausablePriorityBlockingQueue<Runnable>(), new CustomizablePriorityThreadFactory(threadPriority));
    }
    
    /**
     * Creates a thread pool executor with a
     * {@link PausablePriorityBlockingQueue}.
     * @param poolSize
     *            the size of the pool of threads.
     * @param threadPriority
     *            the priority of threads created as defined by
     *            {@link Thread#setPriority(int)}.
     */
    public PausableThreadPoolExecutor(int poolSize, int threadPriority) {
        super(poolSize, poolSize, 0, TimeUnit.NANOSECONDS, new PausablePriorityBlockingQueue<Runnable>(), new CustomizablePriorityThreadFactory(threadPriority));
    }

    /**
     * Creates a thread pool executor with a
     * {@link PausablePriorityBlockingQueue} and low priority threads.
     * @param poolSize
     *            the size of the pool of threads.
     */
    public PausableThreadPoolExecutor(int poolSize) {
        super(poolSize, poolSize, 0, TimeUnit.NANOSECONDS, new PausablePriorityBlockingQueue<Runnable>(), new CustomizablePriorityThreadFactory());
    }

    // ----------------------------------
    // API
    // ----------------------------------
    public void pause() {
        ((PausablePriorityBlockingQueue<Runnable>) getQueue()).pause();
    }

    public void resume() {
        ((PausablePriorityBlockingQueue<Runnable>) getQueue()).resume();
    }
}
