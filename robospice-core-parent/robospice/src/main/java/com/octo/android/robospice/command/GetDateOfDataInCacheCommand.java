package com.octo.android.robospice.command;

import java.util.Date;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;

public class GetDateOfDataInCacheCommand extends SpiceManager.SpiceManagerCommand<Date> {
    private Class<?> clazz;
    private Object cacheKey;

    public GetDateOfDataInCacheCommand(SpiceManager spiceManager, Class<?> clazz, Object cacheKey) {
        super(spiceManager);
        this.clazz = clazz;
        this.cacheKey = cacheKey;
    }

    @Override
    protected Date executeWhenBound(SpiceService spiceService) throws CacheCreationException {
        try {
            return spiceService.getDateOfDataInCache(clazz, cacheKey);
        } catch (CacheLoadingException ex) {
            return null;
        }
    }
}
