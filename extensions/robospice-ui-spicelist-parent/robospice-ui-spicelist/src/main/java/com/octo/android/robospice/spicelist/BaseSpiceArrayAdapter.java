package com.octo.android.robospice.spicelist;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import roboguice.util.temp.Ln;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

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
public abstract class BaseSpiceArrayAdapter<T> extends ArrayAdapter<T> {

    protected int imageWidth = 0;
    protected int imageHeight = 0;

    /**
     * Indicates wether to use the network to update data. This is set by the {@link SpiceListView}.
     */
    protected boolean isNetworkFetchingAllowed = true;
    /**
     * A {@link SpiceManager} that will be used to fetch binaries. It's lifecycle has to be managed
     * at the context level (usually fragment or activity).
     */
    protected SpiceManager spiceManagerBinary;
    /**
     * List of event listeners to get notified of network fetching allowed changes.
     */
    private List<NetworkFetchingAuthorizationStateChangeAdapter> networkFetchingAuthorizationStateChangeListenerList = Collections
            .synchronizedList(new ArrayList<NetworkFetchingAuthorizationStateChangeAdapter>());
    /**
     * Contains all images that have been added recently to the list. They will be animated when
     * first displayed.
     */
    private Set<Object> freshDrawableSet = new HashSet<Object>();
    /** The default drawable to display during image loading from the network. */
    protected Drawable defaultDrawable;
    private Animation animation;

    // ----------------------------
    // --- CONSTRUCTOR
    // ----------------------------

    public BaseSpiceArrayAdapter(Context context, SpiceManager spiceManagerBinary) {
        this(context, spiceManagerBinary, new ArrayList<T>());
    }

    public BaseSpiceArrayAdapter(Context context, SpiceManager spiceManagerBinary, T[] objects) {
        this(context, spiceManagerBinary, Arrays.asList(objects));
    }

    /**
     * Used for testing only.
     */
    protected BaseSpiceArrayAdapter(Context context, SpiceManager spiceManagerBinary, List<T> objects) {
        super(context, 0, objects);
        initialize(context, spiceManagerBinary);
    }

    // ----------------------------
    // --- PUBLIC API
    // ----------------------------

    public void setDefaultUserDrawable(Drawable defaultUserDrawable) {
        this.defaultDrawable = defaultUserDrawable;
    }

    /* package-private */void setNetworkFetchingAllowed(boolean isNetworkFetchingAllowed) {
        boolean changed = isNetworkFetchingAllowed != this.isNetworkFetchingAllowed;
        this.isNetworkFetchingAllowed = isNetworkFetchingAllowed;
        if (isNetworkFetchingAllowed && changed) {
            fireOnNetworkFetchingAllowedChange();
            Ln.d("calling state change listeners");
        }
    }

    /**
     * Updates a {@link SpiceListItemView} containing some data. The method
     * {@link #createRequest(Object)} will be applied to data to know which bitmapRequest to execute
     * to get data from network if needed. This method must be called during
     * {@link #getView(int, android.view.View, android.view.ViewGroup)}.
     * @param data
     *            the data to update the {@link SpiceListItemView} with.
     * @param spiceListItemView
     *            the {@link SpiceListItemView} that displays an image to represent data.
     */
    protected void updateListItemViewAsynchronously(T data, SpiceListItemView<T> spiceListItemView) {
        if (!registered(spiceListItemView)) {
            addSpiceListItemView(spiceListItemView);
        }
        for (int imageIndex = 0; imageIndex < spiceListItemView.getImageViewCount(); imageIndex++) {
            imageWidth = Math.max(imageWidth, spiceListItemView.getImageView(imageIndex).getWidth());
            imageHeight = Math.max(imageHeight, spiceListItemView.getImageView(imageIndex).getHeight());
            performBitmapRequestAsync(spiceListItemView, data, imageIndex);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SpiceListItemView<T> spiceListItemView;

        T currentItem = getItem(position);
        if (convertView != null) {
            spiceListItemView = (SpiceListItemView<T>) convertView;
        } else {
            spiceListItemView = createView(getContext(), parent);
        }
        spiceListItemView.update(currentItem);

        // this is the most important line. It will update views automatically
        // ----------------------------------------
        updateListItemViewAsynchronously(currentItem, spiceListItemView);
        // ----------------------------------------

        return (View) spiceListItemView;
    }

    public abstract SpiceListItemView<T> createView(Context context, ViewGroup parent);

    // ----------------------------
    // --- PRIVATE API
    // ----------------------------

    private void addSpiceListItemView(SpiceListItemView<T> spiceListItemView) {
        this.networkFetchingAuthorizationStateChangeListenerList.add(new NetworkFetchingAuthorizationStateChangeAdapter(
                spiceListItemView));
    }

    private boolean registered(SpiceListItemView<T> view) {
        for (NetworkFetchingAuthorizationStateChangeAdapter listener : networkFetchingAuthorizationStateChangeListenerList) {
            if (listener.getView() == view) {
                return true;
            }
        }
        return false;
    }

    private void fireOnNetworkFetchingAllowedChange() {
        synchronized (networkFetchingAuthorizationStateChangeListenerList) {
            for (NetworkFetchingAuthorizationStateChangeAdapter networkFetchingAuthorizationStateChangeListener : networkFetchingAuthorizationStateChangeListenerList) {
                Ln.d("calling state change listener");
                networkFetchingAuthorizationStateChangeListener.onNetworkFetchingAllowedChange(isNetworkFetchingAllowed);
            }
        }
    }

    private void initialize(Context context, SpiceManager spiceManagerBinary) {
        this.spiceManagerBinary = spiceManagerBinary;
        defaultDrawable = context.getResources().getDrawable(android.R.drawable.picture_frame);
        animation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        animation.setDuration(getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    // ----------------------------
    // --- INNER CLASSES
    // ----------------------------

    protected class ImageRequestListener implements RequestListener<Bitmap> {

        private SpiceListItemView<T> spiceListItemView;
        private T data;
        private ImageView thumbImageView;
        private String imageFileName;

        public ImageRequestListener(T data, SpiceListItemView<T> spiceListItemView, int imageIndex, String imageFileName) {
            this.data = data;
            this.spiceListItemView = spiceListItemView;
            this.thumbImageView = spiceListItemView.getImageView(imageIndex);
            this.imageFileName = imageFileName;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            Ln.e(SpiceListItemView.class.getName(), "Unable to retrive image", spiceException);
            thumbImageView.setImageDrawable(defaultDrawable);
        }

        @Override
        public void onRequestSuccess(Bitmap bitmap) {
            freshDrawableSet.add(data);
            if (this.data.equals(spiceListItemView.getData())) {
                loadBitmapAsynchronously(data, thumbImageView, imageFileName);
            }
        }
    }

    protected void loadBitmapAsynchronously(T octo, ImageView thumbImageView, String tempThumbnailImageFileName) {
        if (thumbImageView.getTag() != null && thumbImageView.getTag().equals(tempThumbnailImageFileName)) {
            return;
        }

        if (cancelPotentialWork(tempThumbnailImageFileName, thumbImageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(thumbImageView, octo);
            task.fileName = tempThumbnailImageFileName;
            final AsyncDrawable asyncDrawable = new AsyncDrawable(getContext().getResources(), task);
            thumbImageView.setImageDrawable(asyncDrawable);
            thumbImageView.setTag(tempThumbnailImageFileName);
            task.execute(tempThumbnailImageFileName);
        }
    }

    private boolean cancelPotentialWork(String fileName, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapFileName = bitmapWorkerTask.fileName;
            if (bitmapFileName == null || !bitmapFileName.equals(fileName)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was
        // cancelled
        return true;
    }

    protected BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof BaseSpiceArrayAdapter.AsyncDrawable) {
                @SuppressWarnings("unchecked")
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public abstract void performBitmapRequestAsync(SpiceListItemView<T> spiceListItemView, T data, int imageIndex);

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        String fileName = "";
        private T data;
        private Animation animation;

        public BitmapWorkerTask(ImageView imageView, T data) {
            // Use a WeakReference to ensure the ImageView can be garbage
            // collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.data = data;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            animation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
            animation.setDuration(getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime));
            fileName = params[0];
            return BitmapFactory.decodeFile(fileName, null);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                if (data.toString().equals("JFA")) {
                    Ln.d(data.toString() + " : cancel decoding bitmap");
                }
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                if (data.toString().equals("JFA")) {
                    Ln.d(data.toString() + " : bitmapworkertask");
                }
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    if (freshDrawableSet.contains(data)) {
                        freshDrawableSet.remove(data);
                        imageView.startAnimation(animation);
                    }
                    imageView.setImageBitmap(bitmap);
                    // no used anymore.
                    // imageView.setTag( null );
                }
            }
        }
    }

    private class NetworkFetchingAuthorizationStateChangeAdapter {

        private WeakReference<SpiceListItemView<T>> weakReferenceSpiceListItemView;

        public NetworkFetchingAuthorizationStateChangeAdapter(SpiceListItemView<T> spiceListItemView) {
            this.weakReferenceSpiceListItemView = new WeakReference<SpiceListItemView<T>>(spiceListItemView);
        }

        public void onNetworkFetchingAllowedChange(boolean networkFetchingAllowed) {
            SpiceListItemView<T> spiceListItemView = weakReferenceSpiceListItemView.get();
            if (spiceListItemView != null) {
                T data = spiceListItemView.getData();
                for (int imageIndex = 0; imageIndex < spiceListItemView.getImageViewCount(); imageIndex++) {
                    performBitmapRequestAsync(spiceListItemView, data, imageIndex);
                }
            }
        }

        public SpiceListItemView<T> getView() {
            return weakReferenceSpiceListItemView.get();
        }
    }

    private class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, BitmapWorkerTask bitmapWorkerTask) {
            super(res);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

}
