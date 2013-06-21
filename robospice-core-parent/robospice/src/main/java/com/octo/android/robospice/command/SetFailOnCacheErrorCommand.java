package com.octo.android.robospice.command;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceManager.SpiceManagerCommand;
import com.octo.android.robospice.SpiceService;

public class SetFailOnCacheErrorCommand extends SpiceManagerCommand<Void> {
    private final boolean failOnCacheError;

    public SetFailOnCacheErrorCommand(SpiceManager spiceManager, boolean failOnCacheError) {
        super(spiceManager);
        this.failOnCacheError = failOnCacheError;
    }

    @Override
    protected Void executeWhenBound(SpiceService spiceService) {
        spiceService.setFailOnCacheError(failOnCacheError);
        return null;
    }
}
