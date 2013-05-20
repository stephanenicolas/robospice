package com.octo.android.robospice.priority;

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
public final class PriorityThreadPoolExecutor {

    private PriorityThreadPoolExecutor() {
        // utility class constructor
    }

    public static PausableThreadPoolExecutor getPriorityExecutor(int nThreads) {
        return new PausableThreadPoolExecutor(nThreads) {

            @Override
            protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
                // makes findbugs happy, there must be some @Nullable
                // annotation in JDK..
                if (runnable == null) {
                    return null;
                }
                RunnableFuture<T> runnableFuture = super.newTaskFor(runnable, value);
                return new PriorityFuture<T>(runnableFuture, ((PriorityRunnable) runnable).getPriority());
            }
        };
    }
}
