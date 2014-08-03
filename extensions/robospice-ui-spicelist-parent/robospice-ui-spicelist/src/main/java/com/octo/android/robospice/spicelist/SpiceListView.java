package com.octo.android.robospice.spicelist;

import android.widget.HeaderViewListAdapter;
import com.octo.android.robospice.spicelist.simple.SpiceArrayAdapter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * This {@link ListView} is optimized to display some content that contains image loaded from the
 * network via RoboSpice. It uses a {@link SpiceArrayAdapter} to hold data and create/update views.
 * It can be instanciated programmatically or via XML. Basically, it will load images only when
 * scrolling is stopped.
 * @author sni
 */
public class SpiceListView extends ListView {

    /*package-private for tests*/ final SpiceListScrollListener onScrollListener = new SpiceListScrollListener();

    // ----------------------------
    // --- CONSTRUCTORS
    // ----------------------------

    public SpiceListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public SpiceListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public SpiceListView(Context context) {
        super(context);
        initialize();
    }

    // ----------------------------
    // --- PUBLIC API
    // ----------------------------

    /**
     * {@inheritDoc}
     *
     * @param l The listener to register with the ListView, or {@code null} to unregister an existing one.
     */
    @Override
    public void setOnScrollListener(OnScrollListener l) {
        onScrollListener.setWrappedListener(l);
        super.setOnScrollListener(onScrollListener);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof BaseSpiceArrayAdapter)) {
            throw new IllegalArgumentException("SpiceLists only support SpiceArrayAdapters.");
        }
        super.setAdapter(adapter);
    }

    @Override
    public BaseSpiceArrayAdapter<?> getAdapter() {
        ListAdapter adapter = super.getAdapter();

        if (adapter == null) {
            return null;
        } else if (adapter instanceof  BaseSpiceArrayAdapter<?>) {
            return (BaseSpiceArrayAdapter<?>) adapter;
        } else {
            return (BaseSpiceArrayAdapter<?>) ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
    }

    // ----------------------------
    // --- PRIVATE API
    // ----------------------------
    private void initialize() {
        super.setOnScrollListener(onScrollListener);
    }

    // ----------------------------
    // --- INNER CLASS API
    // ----------------------------
    /*package-private for tests*/ final class SpiceListScrollListener implements OnScrollListener {

        private OnScrollListener wrappedListener;

        private void setWrappedListener(OnScrollListener l) {
            this.wrappedListener = l;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (getAdapter() != null) {
                getAdapter().setNetworkFetchingAllowed(scrollState == SCROLL_STATE_IDLE);
            }
            if (wrappedListener != null) {
                wrappedListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (wrappedListener != null) {
                wrappedListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    }

}
