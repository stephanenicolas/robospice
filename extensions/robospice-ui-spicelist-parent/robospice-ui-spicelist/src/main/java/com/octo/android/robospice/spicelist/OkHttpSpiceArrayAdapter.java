package com.octo.android.robospice.spicelist;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roboguice.util.temp.Ln;
import android.content.Context;
import android.os.AsyncTask;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.okhttp.simple.OkHttpBitmapRequest;

/**
 * An adapter that is optimized for {@link SpiceListView} instances. It offers to update ImageViews
 * contained in {@link SpiceListItemView} instances with images loaded from the network. All you
 * have to do is to Override {@link #createRequest(Object)} to define a bitmapRequest for each
 * object in the list that is associated an image to display. Also please note that in your
 * {@link #getView(int, android.view.View, android.view.ViewGroup)} method, you must call
 * {@link #updateListItemViewAsynchronously(Object, SpiceListItemView)} in order for your
 * {@link SpiceListItemView} to be updated automagically.
 * @author sni
 * @param <T>
 *            the type of data displayed by the list.
 */
public abstract class OkHttpSpiceArrayAdapter<T> extends BaseSpiceArrayAdapter<T> {

    // ----------------------------
    // --- CONSTRUCTOR
    // ----------------------------

    public OkHttpSpiceArrayAdapter(Context context, OkHttpBitmapSpiceManager spiceManagerBinary) {
        this(context, spiceManagerBinary, new ArrayList<T>());
    }

    public OkHttpSpiceArrayAdapter(Context context, OkHttpBitmapSpiceManager spiceManagerBinary, T[] objects) {
        this(context, spiceManagerBinary, Arrays.asList(objects));
    }

    /**
     * Used for testing only.
     */
    protected OkHttpSpiceArrayAdapter(Context context, OkHttpBitmapSpiceManager spiceManagerBinary, List<T> objects) {
        super(context, spiceManagerBinary, objects);
    }

    // ----------------------------
    // --- PUBLIC API
    // ----------------------------

    @Override
    public void performBitmapRequestAsync(SpiceListItemView<T> spiceListItemView, T data, int imageIndex) {
        new ThumbnailAsynTask(createRequest(data, imageIndex, imageWidth, imageHeight)).execute(data, spiceListItemView,
                imageIndex);
    }

    public abstract OkHttpBitmapRequest createRequest(T data, int imageIndex, int requestImageWidth, int requestImageHeight);

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    protected class ThumbnailAsynTask extends AsyncTask<Object, Void, Boolean> {

        private T data;
        private SpiceListItemView<T> spiceListItemView;
        private String tempThumbnailImageFileName = "";
        private OkHttpBitmapRequest bitmapRequest;
        private int imageIndex;

        public ThumbnailAsynTask(OkHttpBitmapRequest request) {
            this.bitmapRequest = request;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Boolean doInBackground(Object... params) {
            data = (T) params[0];
            spiceListItemView = (SpiceListItemView<T>) params[1];
            imageIndex = (Integer) params[2];

            if (bitmapRequest != null) {

                File tempThumbnailImageFile = bitmapRequest.getCacheFile();
                tempThumbnailImageFileName = tempThumbnailImageFile.getAbsolutePath();
                Ln.d("Filename : " + tempThumbnailImageFileName);

                if (!tempThumbnailImageFile.exists()) {
                    if (isNetworkFetchingAllowed) {
                        ImageRequestListener imageRequestListener = new ImageRequestListener(data, spiceListItemView, imageIndex,
                                tempThumbnailImageFileName);
                        spiceManagerBinary.execute(bitmapRequest, "THUMB_IMAGE_" + data.hashCode(),
                                DurationInMillis.ALWAYS_EXPIRED, imageRequestListener);
                    }
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isImageAvailableInCache) {
            if (isImageAvailableInCache) {
                loadBitmapAsynchronously(data, spiceListItemView.getImageView(imageIndex), tempThumbnailImageFileName);
            } else {
                spiceListItemView.getImageView(imageIndex).setImageDrawable(defaultDrawable);
            }
        }
    }

}
