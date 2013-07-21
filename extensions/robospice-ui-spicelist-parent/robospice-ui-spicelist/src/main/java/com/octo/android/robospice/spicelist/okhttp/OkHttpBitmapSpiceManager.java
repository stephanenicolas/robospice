package com.octo.android.robospice.spicelist.okhttp;

import com.octo.android.robospice.SpiceManager;

public class OkHttpBitmapSpiceManager extends SpiceManager {

    public OkHttpBitmapSpiceManager() {
        super(OkHttpBitmapSpiceService.class);
    }

    /**
     * For testing only.
     * @param bitmapSpiceServiceClass
     *            the spice service to bind to.
     */
    protected OkHttpBitmapSpiceManager(Class<? extends OkHttpBitmapSpiceService> bitmapSpiceServiceClass) {
        super(bitmapSpiceServiceClass);
    }

}
