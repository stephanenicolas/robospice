package com.octo.android.robospice.request.simple;

import java.io.File;

import android.graphics.Bitmap;

import com.octo.android.robospice.request.SpiceRequest;

/**
 * Describes the common behavior of all {@link SpiceRequest} that allow get
 * fetch Bitmaps.
 * @author SNI
 */
public interface IBitmapRequest {

    Bitmap loadDataFromNetwork() throws Exception;

    File getCacheFile();

}
