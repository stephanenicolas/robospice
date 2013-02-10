package com.octo.android.robospice.spicelist.test;

import android.content.Context;

import com.octo.android.robospice.networkstate.NetworkStateChecker;
import com.octo.android.robospice.spicelist.BitmapSpiceService;

public class TestBigBinarySpiceService extends BitmapSpiceService {

    @Override
    protected NetworkStateChecker getNetworkStateChecker() {
        return new NetworkStateChecker() {

            @Override
            public boolean isNetworkAvailable(Context context) {
                return true;
            }

            @Override
            public void checkPermissions(Context context) {
            }
        };
    }
}
