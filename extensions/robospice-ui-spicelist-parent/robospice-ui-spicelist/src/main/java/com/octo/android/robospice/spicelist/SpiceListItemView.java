package com.octo.android.robospice.spicelist;

import android.widget.ImageView;

/**
 * Describes the behavior of a "List Item View" that contains an ImageView that
 * is used to display a data. The image will be download from the network via
 * RoboSpice.
 * @param <T>
 *            the type of the data displayed in this {@link SpiceListItemView}.
 * @author sni
 */
public interface SpiceListItemView<T> {

    /**
     * @return the object that is displayed by this list item.
     */
    T getData();

    /**
     * @return the imageView that is displaying the drawable associated to this
     *         view's data.
     */
    ImageView getImageView();
}
