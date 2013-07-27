package com.octo.android.robospice.networkstate;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Consider network to be available when one network connection is up, whatever
 * it is. This class will also ensure that both
 * <tt>android.permission.ACCESS_NETWORK_STATE</tt> and
 * <tt>android.permission.ACCESS_NETWORK</tt> are granted.
 * @author sni
 */
public class DefaultNetworkStateChecker implements NetworkStateChecker {

    @Override
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo[] allNetworkInfos = connectivityManager.getAllNetworkInfo();
        for (final NetworkInfo networkInfo : allNetworkInfos) {
            if (networkInfo.getState() == NetworkInfo.State.CONNECTED || networkInfo.getState() == NetworkInfo.State.CONNECTING) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void checkPermissions(final Context context) {
        checkHasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE);
        checkHasPermission(context, Manifest.permission.INTERNET);
    }

    private boolean checkHasPermission(final Context context, final String permissionName) {
        final boolean hasPermission = context.getPackageManager().checkPermission(permissionName, context.getPackageName()) == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) {
            throw new SecurityException("Application doesn\'t declare <uses-permission android:name=\"" + permissionName + "\" />");
        }
        return true;
    }

}
