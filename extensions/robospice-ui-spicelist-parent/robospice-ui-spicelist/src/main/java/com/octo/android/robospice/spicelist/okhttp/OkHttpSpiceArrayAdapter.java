package com.octo.android.robospice.spicelist.okhttp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;

import com.octo.android.robospice.spicelist.BaseSpiceArrayAdapter;
import com.octo.android.robospice.spicelist.SpiceListItemView;
import com.octo.android.robospice.spicelist.SpiceListView;

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

}
