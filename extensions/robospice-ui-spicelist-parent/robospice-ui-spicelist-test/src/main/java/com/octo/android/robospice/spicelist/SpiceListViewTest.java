package com.octo.android.robospice.spicelist;

import org.easymock.EasyMock;

import android.test.InstrumentationTestCase;
import android.test.UiThreadTest;
import android.widget.AbsListView.OnScrollListener;

public class SpiceListViewTest extends InstrumentationTestCase {

    private static final int SCROLL_Y_AMOUNT_IN_PIXELS = 100;
    private SpiceListView spiceListView;
    private OnScrollListener mockOnScrollListener;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        spiceListView = new SpiceListView(getInstrumentation().getContext());
        mockOnScrollListener = EasyMock.createMock(OnScrollListener.class);
    }

    @UiThreadTest
    public void testOnScroll_triggers_wrapped_listener() {
        //GIVEN
        mockOnScrollListener.onScroll(spiceListView, 0, 0, 0);
        EasyMock.replay(mockOnScrollListener);

        //WHEN
        spiceListView.setOnScrollListener(mockOnScrollListener);
        spiceListView.scrollTo(0, SCROLL_Y_AMOUNT_IN_PIXELS);

        //THEN
        EasyMock.verify(mockOnScrollListener);
    }

    @UiThreadTest
    public void testOnFling_triggers_wrapped_listener() throws InterruptedException {
        //GIVEN
        mockOnScrollListener.onScroll(spiceListView, 0, 0, 0);
        mockOnScrollListener.onScrollStateChanged(spiceListView, OnScrollListener.SCROLL_STATE_FLING);
        EasyMock.replay(mockOnScrollListener);
        
        //WHEN
        spiceListView.setOnScrollListener(mockOnScrollListener);
        spiceListView.onScrollListener.onScrollStateChanged(spiceListView, OnScrollListener.SCROLL_STATE_FLING);

        //THEN
        EasyMock.verify(mockOnScrollListener);
    }

}
