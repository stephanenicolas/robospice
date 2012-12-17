package com.octo.android.robospice.networkstate;

import android.content.Context;

import com.octo.android.robospice.request.RequestProcessor;

/**
 * Defines the responsability of an entity that checks the network state. It can be passed to the
 * {@link RequestProcessor} to determine if network is available or not and to determine if requests should be processed
 * or not.
 * 
 * @author sni
 * 
 */
public interface NetworkStateChecker {
    /**
     * Determine whether network is available or not.
     * 
     * @param context
     *            the context from which network state is accessed.
     * @return a boolean indicating if network is considered to be available or not.
     */
    public boolean isNetworkAvailable( Context context );
}
