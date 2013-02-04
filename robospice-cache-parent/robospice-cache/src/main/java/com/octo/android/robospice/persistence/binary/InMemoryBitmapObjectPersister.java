package com.octo.android.robospice.persistence.binary;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class InMemoryBitmapObjectPersister
    extends InMemoryLRUCacheObjectPersister<Bitmap>
{

    private final int cacheSize;

    public InMemoryBitmapObjectPersister (Application application)
    {
        this(application, -1);
    }

    public InMemoryBitmapObjectPersister( Application application,
                                          int cacheSize )
    {
        super( application, Bitmap.class );

        // base Android memory class is 16 MB per process
        // the cache should take up no more than 1/4 of the available app memory
        if (cacheSize > 0)
        {
            this.cacheSize = cacheSize;
        }
        else
        {
            int memClass = ( (ActivityManager) application.getSystemService(
                Context.ACTIVITY_SERVICE ) ).getMemoryClass();
            this.cacheSize = 1024 * 1024 * 4 * (memClass / 16);
        }
    }

    @Override
    protected LruCache<String, CacheItem<Bitmap>> instantiateLRUCache()
    {
        return new LruCache<String, CacheItem<Bitmap>>(cacheSize)
        {

            @Override
            protected int sizeOf( String key,
                                  CacheItem<Bitmap> value )
            {
                return value.data.getRowBytes() *
                       value.data.getHeight();
            }

            @Override
            protected void entryRemoved( boolean evicted,
                                         String key,
                                         CacheItem<Bitmap> oldValue,
                                         CacheItem<Bitmap> newValue )
            {
                super.entryRemoved( evicted, key, oldValue, newValue );
            }
        };
    }


}
