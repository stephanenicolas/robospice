package com.octo.android.robospice.priority;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A future with priority, from <a href=
 * "http://stackoverflow.com/q/807223/693752">SOF</a>
 * @author SNI
 * @param <T>
 *            the type of the future's return.
 */
public class PriorityFuture<T> implements RunnableFuture<T>, Comparable<PriorityFuture<T>> {

    private RunnableFuture<T> src;
    private int priority;

    public PriorityFuture(RunnableFuture<T> other, int priority) {
        this.src = other;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return src.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return src.isCancelled();
    }

    @Override
    public boolean isDone() {
        return src.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return src.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return src.get(timeout, unit);
    }

    @Override
    public void run() {
        src.run();
    }

    @Override
    public int compareTo(PriorityFuture<T> other) {
        return priority - other.priority;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + priority;
        result = prime * result + (src == null ? 0 : src.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PriorityFuture<?> other = (PriorityFuture<?>) obj;
        if (priority != other.priority) {
            return false;
        }
        if (src == null) {
            if (other.src != null) {
                return false;
            }
        } else if (!src.equals(other.src)) {
            return false;
        }
        return true;
    }

}
