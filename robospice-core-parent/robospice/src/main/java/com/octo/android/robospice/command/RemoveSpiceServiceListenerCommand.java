package com.octo.android.robospice.command;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.listener.SpiceServiceListener;

public class RemoveSpiceServiceListenerCommand extends SpiceManager.SpiceManagerCommand<Void> {
    private SpiceServiceListener spiceServiceListener;

    public RemoveSpiceServiceListenerCommand(SpiceManager spiceManager, SpiceServiceListener spiceServiceListener) {
        super(spiceManager);
        this.spiceServiceListener = spiceServiceListener;
    }

    @Override
    protected Void executeWhenBound(SpiceService spiceService) {
        spiceService.removeSpiceServiceListener(spiceServiceListener);
        return null;
    }
}
