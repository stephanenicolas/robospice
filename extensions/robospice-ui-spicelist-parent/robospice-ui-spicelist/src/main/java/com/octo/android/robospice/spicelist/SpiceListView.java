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

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        super.setOnScrollListener(new SpiceListScrollListener(l));
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof BaseSpiceArrayAdapter)) {
            throw new IllegalArgumentException("SpiceLists only support SpiceArrayAdapters.");
        }
        super.setAdapter(adapter);
    }

    // ----------------------------
    // --- PRIVATE API
    // ----------------------------
    private void initialize() {
        super.setOnScrollListener(new SpiceListScrollListener());
    }

    // ----------------------------
    // --- INNER CLASS API
    // ----------------------------
    private final class SpiceListScrollListener implements OnScrollListener {

        private OnScrollListener wrappedListener;

        SpiceListScrollListener() { }

        SpiceListScrollListener(OnScrollListener wrappedListener) {
            this.wrappedListener = wrappedListener;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (wrappedListener != null) {
                wrappedListener.onScrollStateChanged(view, scrollState);
            }
            ListAdapter adapter = getAdapter();
            if (adapter != null) {
                BaseSpiceArrayAdapter<?> spiceArrayAdapter;
                if (adapter instanceof  BaseSpiceArrayAdapter<?>) {
                    spiceArrayAdapter = (BaseSpiceArrayAdapter<?>) adapter;
                } else {
                    spiceArrayAdapter = (BaseSpiceArrayAdapter<?>) ((HeaderViewListAdapter) adapter).getWrappedAdapter();
                }
                spiceArrayAdapter.setNetworkFetchingAllowed(scrollState == SCROLL_STATE_IDLE);
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
