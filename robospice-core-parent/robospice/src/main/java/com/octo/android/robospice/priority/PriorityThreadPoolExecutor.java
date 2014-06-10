package com.octo.android.robospice.priority;

import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;

/**
 * A ThreadPoolExecutor with priority management, from <a href=
 * "http://stackoverflow.com/q/807223/693752">SOF</a> <br/>
 * There are some inherent limitations to this mechanism. For instance, the
 * first runnable/callable passed to the executor doesn't go in the queue. Thus
 * the priority mechanism will only apply when tasks are queued, and this
 * happens when the number of current runners is exceeds the number of max
 * thread in the pool size.
 * @author SNI
 */
public final class PriorityThreadPoolExecutor extends PausableThreadPoolExecutor {

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------
    public PriorityThreadPoolExecutor(int corePoolSize, int maxPoolSize,
        int threadPriority) {
        super(corePoolSize, maxPoolSize, threadPriority);
    }

    public PriorityThreadPoolExecutor(int poolSize, int threadPriority) {
        super(poolSize, threadPriority);
    }

    public PriorityThreadPoolExecutor(int poolSize) {
        super(poolSize);
    }

    // ----------------------------------
    // API
    // ----------------------------------
    public static PriorityThreadPoolExecutor getPriorityExecutor(
        int nCoreThreads, int maxPoolSize, int threadPriority) {
        return new PriorityThreadPoolExecutor(nCoreThreads, maxPoolSize,
            threadPriority);
    }

    public static PriorityThreadPoolExecutor getPriorityExecutor(int nThreads, int threadPriority) {
        return new PriorityThreadPoolExecutor(nThreads, threadPriority);
    }

    public static PriorityThreadPoolExecutor getPriorityExecutor(int nThreads) {
        return new PriorityThreadPoolExecutor(nThreads);
    }

    // ----------------------------------
    // OVVERRIDEN METHODS
    // ----------------------------------
    @Override
    // thanks to
    // http://binkley.blogspot.fr/2009/04/jumping-work-queue-in-executor.html
    // for backward compat to SDK 8 (JDK 5)
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        // makes findbugs happy, there must be some @Nullable
        // annotation in JDK..
        if (runnable == null) {
            return null;
        }
        return new PriorityFuture<T>(runnable, ((PriorityRunnable) runnable).getPriority(), value);
    }

    // form JDK 1.6, to ensure backward compatibility
    @Override
    public Future<?> submit(Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }
        RunnableFuture<Object> ftask = newTaskFor(task, null);
        execute(ftask);
        return ftask;
    }
}
