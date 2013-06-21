package com.octo.android.robospice.command;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;

public class RemoveAllDataFromCacheCommand extends SpiceManager.SpiceManagerCommand<Void> {

    public RemoveAllDataFromCacheCommand(SpiceManager spiceManager) {
        super(spiceManager);
    }

    @Override
    protected Void executeWhenBound(SpiceService spiceService) {
        spiceService.removeAllDataFromCache();
        return null;
    }
}
