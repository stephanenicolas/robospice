package com.octo.android.robospice.priority;

import java.util.concurrent.ThreadFactory;

/**
 * Creates threads with a low priority.
 * @author SNI
 */
public class CustomizablePriorityThreadFactory implements ThreadFactory {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private int threadPriority = Thread.MIN_PRIORITY;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------
    public CustomizablePriorityThreadFactory(int threadPriority) {
        this.threadPriority = threadPriority;
    }

    public CustomizablePriorityThreadFactory() {
        this(Thread.MIN_PRIORITY);
    }

    // ----------------------------------
    // API
    // ----------------------------------

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setPriority(threadPriority);
        return thread;
    }
}
