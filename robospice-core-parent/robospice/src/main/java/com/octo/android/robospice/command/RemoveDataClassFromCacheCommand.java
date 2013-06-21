package com.octo.android.robospice.command;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;

public class RemoveDataClassFromCacheCommand extends SpiceManager.SpiceManagerCommand<Void> {
    private final Class<?> clazz;

    public <T> RemoveDataClassFromCacheCommand(SpiceManager spiceManager, Class<T> clazz) {
        super(spiceManager);
        this.clazz = clazz;
    }

    @Override
    protected Void executeWhenBound(SpiceService spiceService) {
        spiceService.removeAllDataFromCache(clazz);
        return null;
    }
}
