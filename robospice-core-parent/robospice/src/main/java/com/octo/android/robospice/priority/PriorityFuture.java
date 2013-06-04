package com.octo.android.robospice.priority;

import java.util.concurrent.FutureTask;

/**
 * A future with priority, from <a href=
 * "http://stackoverflow.com/q/807223/693752">SOF</a>
 * @author SNI
 * @param <T>
 *            the type of the future's return.
 */
public class PriorityFuture<T> extends FutureTask<T> implements Comparable<PriorityFuture<T>> {

    private int priority;

    public PriorityFuture(Runnable other, int priority, final T result) {
        super(other, result);
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
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

        return true;
    }

}
