package com.octo.android.robospice.command;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

public class IsDataInCacheCommand extends SpiceManager.SpiceManagerCommand<Boolean> {
    private Class<?> clazz;
    private Object cacheKey;
    private long cacheExpiryDuration;

    public IsDataInCacheCommand(SpiceManager spiceManager, Class<?> clazz, Object cacheKey, long cacheExpiryDuration) {
        super(spiceManager);
        this.clazz = clazz;
        this.cacheExpiryDuration = cacheExpiryDuration;
        this.cacheKey = cacheKey;
    }

    @Override
    protected Boolean executeWhenBound(SpiceService spiceService) throws CacheSavingException, CacheCreationException {
        return spiceService.isDataInCache(clazz, cacheKey, cacheExpiryDuration);
    }
}
