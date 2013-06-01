package com.octo.android.robospice.priority;

import android.test.AndroidTestCase;

public class CustomizablePriorityThreadFactoryTest extends AndroidTestCase {

    CustomizablePriorityThreadFactory threadFactory;

    public void testCreateThread_with_default_priority() {

        // given
        threadFactory = new CustomizablePriorityThreadFactory();

        // when
        Thread t = threadFactory.newThread(new DummyRunnable());

        // then
        assertEquals(Thread.MIN_PRIORITY, t.getPriority());
    }

    public void testCreateThread_with_custom_priority() {

        // given
        final int priority = Thread.MAX_PRIORITY;
        threadFactory = new CustomizablePriorityThreadFactory(priority);

        // when
        Thread t = threadFactory.newThread(new DummyRunnable());

        // then
        assertEquals(priority, t.getPriority());
    }

    // ----------------------------------
    // CLASSES UNDER TESTS
    // ----------------------------------
    private final class DummyRunnable implements Runnable {
        @Override
        public void run() {
        }
    }

}
