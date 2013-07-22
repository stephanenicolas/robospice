package com.octo.android.robospice.command;

import java.util.Map;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestStatus;

public class GetActiveRequestsCommand extends SpiceManager.SpiceManagerCommand<Map<CachedSpiceRequest<?>, RequestStatus>> {
    public GetActiveRequestsCommand(SpiceManager spiceManager) {
        super(spiceManager);
    }

    @Override
    protected Map<CachedSpiceRequest<?>, RequestStatus> executeWhenBound(SpiceService spiceService) {
        return spiceService.getActiveRequests();
    }
}
