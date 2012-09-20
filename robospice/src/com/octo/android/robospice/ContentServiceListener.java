package com.octo.android.robospice;

import com.octo.android.robospice.request.CachedContentRequest;

public interface ContentServiceListener {
	public void onRequestProcessed(CachedContentRequest<?> cachedContentRequest);
}
