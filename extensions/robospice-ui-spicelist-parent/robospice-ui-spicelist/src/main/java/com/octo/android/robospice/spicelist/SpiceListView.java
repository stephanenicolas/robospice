package com.octo.android.robospice.spicelist;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * This {@link ListView} is optimized to display some content that contains
 * image loaded from the network via RoboSpice. It uses a
 * {@link SpiceArrayAdapter} to hold data and create/update views. It can be
 * instanciated programmatically or via XML. Basically, it will load images only
 * when scrolling is stopped.
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

    @Deprecated
    @Override
    public void setOnScrollListener(OnScrollListener l) {
        throw new RuntimeException(
            "OnScrollListener is already used internally by a SpliceListView.");
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof SpiceArrayAdapter)) {
            throw new IllegalArgumentException(
                "SpiceLists only support SpiceArrayAdapters.");
        }
        super.setAdapter(adapter);

    }

    @Override
    public SpiceArrayAdapter<?> getAdapter() {
        return (SpiceArrayAdapter<?>) super.getAdapter();
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
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (getAdapter() != null) {
                getAdapter().setNetworkFetchingAllowed(
                    scrollState == SCROLL_STATE_IDLE);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        }
    }

}
