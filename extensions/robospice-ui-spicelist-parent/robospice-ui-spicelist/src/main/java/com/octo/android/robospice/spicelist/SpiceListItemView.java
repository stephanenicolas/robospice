package com.octo.android.robospice.spicelist;

import android.widget.ImageView;

/**
 * Describes the behavior of a "List Item View" that contains an ImageView that
 * is used to display a given piece of data. The image will be download from the
 * network and will be managed <i>in an optimal way</i> via RoboSpice.
 * @param <T>
 *            the type of the data displayed in this {@link SpiceListItemView}.
 *            This class has to respect the general contrat of
 *            {@link Object#equals(Object)} and {@link Object#hashCode()}.
 * @author sni
 */
public interface SpiceListItemView<T> {

    /**
     * @return the object that is displayed by this list item.
     */
    T getData();

    /**
     * @return the imageView that is displaying the drawable associated to this
     *         view's data. This ImageView will be update by the SpiceManager.
     */
    ImageView getImageView();

    /**
     * Updates the view with given data. Overrides of this method should not
     * deal with images. Only update other fields here.
     * @param data
     */
    void update(T data);
}
