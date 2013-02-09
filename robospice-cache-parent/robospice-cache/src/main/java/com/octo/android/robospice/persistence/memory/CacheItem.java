package com.octo.android.robospice.persistence.memory;

/**
 * The CacheItem class represents a cached object, consisting of a piece of data
 * and a time stamp marking when the data was added to the cache.
 * @param <T>
 *            the type of object that will be stored in the cache.
 */
public class CacheItem<T> {
    private final long creationDate;
    private final T data;

    public CacheItem(T data) {
        this.creationDate = System.currentTimeMillis();
        this.data = data;
    }

    public CacheItem(long creationDate, T data) {
        this.creationDate = creationDate;
        this.data = data;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public T getData() {
        return data;
    }
}
