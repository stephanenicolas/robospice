package com.octo.android.robospice.networkstate;

import android.content.Context;

import com.octo.android.robospice.request.RequestProcessor;

/**
 * Defines the responsability of an entity that checks the network state. It can
 * be passed to the {@link RequestProcessor} to determine if network is
 * available or not and to determine if requests should be processed or not.
 * {@link NetworkStateChecker} are also in charge of checking required
 * permission to determine the network state and network usage.
 * @author sni
 */
public interface NetworkStateChecker {
    /**
     * Determine whether network is available or not.
     * @param context
     *            the context from which network state is accessed.
     * @return a boolean indicating if network is considered to be available or
     *         not.
     */
    boolean isNetworkAvailable(Context context);

    /**
     * Check if all permissions necessary to determine network state and use
     * network are granted to a given context.
     * @param context
     *            the context that will be checked to see if it has all required
     *            permissions.
     */
    void checkPermissions(Context context);
}
