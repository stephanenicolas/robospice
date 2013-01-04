package com.octo.android.robospice.persistence;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Application;

import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

/**
 * Super class of all entities responsible for loading/saving objects of a given
 * class in the cache.
 * @author sni
 * @param <T>
 *                the class of the objects this {@link ObjectPersister} can
 *                persist/unpersist.
 */
public abstract class ObjectPersister<T> implements Persister {

        protected boolean isAsyncSaveEnabled;
        protected ReentrantLock lock = new ReentrantLock();
        protected Condition condition = lock.newCondition();
        private Application application;
        private Class<T> clazz;

        public ObjectPersister(Application application, Class<T> clazz) {
                this.application = application;
                this.clazz = clazz;
        }

        protected Application getApplication() {
                return application;
        }

        protected Class<T> getHandledClass() {
                return clazz;
        }

        @Override
        public boolean canHandleClass(Class<?> clazz) {
                return clazz.equals(this.clazz);
        }

        /**
         * Load data from cache if not expired.
         * @param cacheKey
         *                the cacheKey of the data to load.
         * @param maxTimeInCache
         *                the maximum time the data can have been stored in
         *                cached before being considered expired. 0 means
         *                infinite.
         * @return the data if it could be loaded.
         * @throws FileNotFoundException
         *                 if the data was not in cache.
         * @throws IOException
         *                 if the data in cache can't be read.
         * @throws CacheExpiredException
         *                 if the data in cache is expired.
         */
        public abstract T loadDataFromCache(Object cacheKey, long maxTimeInCache)
                        throws CacheLoadingException;

        public abstract List<T> loadAllDataFromCache()
                        throws CacheLoadingException;

        public abstract List<Object> getAllCacheKeys();

        public abstract T saveDataToCacheAndReturnData(T data, Object cacheKey)
                        throws CacheSavingException;

        public abstract boolean removeDataFromCache(Object cacheKey);

        public abstract void removeAllDataFromCache();

        public boolean isAsyncSaveEnabled() {
                return isAsyncSaveEnabled;
        }

        public void setAsyncSaveEnabled(boolean isAsyncSaveEnabled) {
                this.isAsyncSaveEnabled = isAsyncSaveEnabled;
        }

        protected void awaitForSaveAsyncTermination(long time, TimeUnit timeUnit)
                        throws InterruptedException {
                lock.lock();
                condition.await(time, timeUnit);
                lock.unlock();
        }
}
