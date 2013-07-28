package com.octo.android.robospice.command;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.listener.SpiceServiceListener;

public class AddSpiceServiceListenerCommand extends SpiceManager.SpiceManagerCommand<Void> {
    private SpiceServiceListener spiceServiceListener;

    public AddSpiceServiceListenerCommand(SpiceManager spiceManager, SpiceServiceListener spiceServiceListener) {
        super(spiceManager);
        this.spiceServiceListener = spiceServiceListener;
    }

    @Override
    protected Void executeWhenBound(SpiceService spiceService) {
        spiceService.addSpiceServiceListener(spiceServiceListener);
        return null;
    }
}
