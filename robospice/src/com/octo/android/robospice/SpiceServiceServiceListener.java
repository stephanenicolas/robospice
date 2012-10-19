package com.octo.android.robospice;

import com.octo.android.robospice.request.CachedSpiceRequest;

public interface SpiceServiceServiceListener {
	public void onRequestProcessed(CachedSpiceRequest<?> cachedContentRequest);
}
