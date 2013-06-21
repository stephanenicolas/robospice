package com.octo.android.robospice.command;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

public class PutDataInCacheCommand<T> extends SpiceManager.SpiceManagerCommand<T> {
    private Object cacheKey;
    private T data;

    public PutDataInCacheCommand(SpiceManager spiceManager, T data, Object cacheKey) {
        super(spiceManager);
        this.data = data;
        this.cacheKey = cacheKey;
    }

    @Override
    protected T executeWhenBound(SpiceService spiceService) throws CacheSavingException, CacheCreationException {
        return spiceService.putDataInCache(cacheKey, data);
    }
}
