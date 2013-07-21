package com.octo.android.robospice.spicelist.simple;

import com.octo.android.robospice.SpiceManager;

public class BitmapSpiceManager extends SpiceManager {

    public BitmapSpiceManager() {
        super(BitmapSpiceService.class);
    }

    /**
     * For testing only.
     * @param bitmapSpiceServiceClass
     *            the spice service to bind to.
     */
    protected BitmapSpiceManager(Class<? extends BitmapSpiceService> bitmapSpiceServiceClass) {
        super(bitmapSpiceServiceClass);
    }

}
