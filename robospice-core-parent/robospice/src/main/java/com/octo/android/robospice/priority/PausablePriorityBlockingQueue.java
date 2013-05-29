package com.octo.android.robospice.priority;

import java.io.ObjectInputStream;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used internally. It makes testing easier. The queue will not
 * allow to {@link #poll()} or {@link #take()} an element when paused.
 * @author SNI
 */
public class PausablePriorityBlockingQueue<T> extends PriorityBlockingQueue<T> {
    private static final long serialVersionUID = 3726077277740650698L;
    private boolean isPaused;
    private transient ReentrantLock pauseLock = new ReentrantLock();
    private transient Condition unpaused = pauseLock.newCondition();

    public PausablePriorityBlockingQueue() {
        super();
    }

    @Override
    public T poll() {
        pauseLock.lock();
        try {
            while (isPaused) {
                unpaused.await();
            }
        } catch (InterruptedException ie) {
            throw new RuntimeException("Interrupted while paused.");
        } finally {
            pauseLock.unlock();
        }
        return super.poll();
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        pauseLock.lock();
        try {
            while (isPaused) {
                unpaused.await();
            }
        } catch (InterruptedException ie) {
            throw new RuntimeException("Interrupted while paused.");
        } finally {
            pauseLock.unlock();
        }
        return super.poll(timeout, unit);
    }

    @Override
    public T take() throws InterruptedException {
        pauseLock.lock();
        try {
            while (isPaused) {
                unpaused.await();
            }
        } catch (InterruptedException ie) {
            throw new RuntimeException("Interrupted while paused.");
        } finally {
            pauseLock.unlock();
        }
        return super.take();
    }

    public void pause() {
        pauseLock.lock();
        try {
            isPaused = true;
        } finally {
            pauseLock.unlock();
        }
    }

    public void resume() {
        pauseLock.lock();
        try {
            isPaused = false;
            unpaused.signalAll();
        } finally {
            pauseLock.unlock();
        }
    }

    // makes findbugs happy, but unused
    private void readObject(ObjectInputStream ois) {
        try {
            ois.defaultReadObject();
            pauseLock = new ReentrantLock();
            pauseLock.newCondition();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
