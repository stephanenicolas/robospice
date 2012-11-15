package com.octo.android.robospice.networkstate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Consider network to be available when one network connection is up, whatever it is.
 * 
 * @author sni
 * 
 */
public class DefaultNetworkStateChecker implements NetworkStateChecker {

    public boolean isNetworkAvailable( Context context ) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo[] allNetworkInfos = connectivityManager.getAllNetworkInfo();
        for ( NetworkInfo networkInfo : allNetworkInfos ) {
            if ( networkInfo.getState() == NetworkInfo.State.CONNECTED || networkInfo.getState() == NetworkInfo.State.CONNECTING ) {
                return true;
            }
        }
        return false;
    }

}
