package com.octo.android.robospice.command;

import java.util.List;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;

public class GetAllCacheKeysCommand extends SpiceManager.SpiceManagerCommand<List<Object>> {
    private Class<?> clazz;

    public GetAllCacheKeysCommand(SpiceManager spiceManager, Class<?> clazz) {
        super(spiceManager);
        this.clazz = clazz;
    }

    @Override
    protected List<Object> executeWhenBound(SpiceService spiceService) throws CacheLoadingException, CacheCreationException {
        return spiceService.getAllCacheKeys(clazz);
    }
}
