package com.octo.android.robospice.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.test.AndroidTestCase;

import com.octo.android.robospice.stub.SpiceRequestStub;
import com.octo.android.robospice.stub.SpiceRequestSucceedingStub;

public class SpiceRequestTest extends AndroidTestCase {

    private static final String TEST_RETURNED_DATA = "toto";

    public void testComapareTo_ordering_should_use_priorities() {
        // given
        SpiceRequestStub<String> stubRequestLowPriority = new SpiceRequestSucceedingStub<String>(String.class, TEST_RETURNED_DATA);
        stubRequestLowPriority.setPriority(SpiceRequest.PRIORITY_LOW);
        SpiceRequestStub<String> stubRequestNormalPriority = new SpiceRequestSucceedingStub<String>(String.class, TEST_RETURNED_DATA);
        SpiceRequestStub<String> stubRequestHighPriority = new SpiceRequestSucceedingStub<String>(String.class, TEST_RETURNED_DATA);
        stubRequestHighPriority.setPriority(SpiceRequest.PRIORITY_HIGH);

        List<SpiceRequest<String>> spiceRequestList = new ArrayList<SpiceRequest<String>>();
        spiceRequestList.add(stubRequestNormalPriority);
        spiceRequestList.add(stubRequestLowPriority);
        spiceRequestList.add(stubRequestHighPriority);

        // when
        Collections.sort(spiceRequestList);

        // then
        assertEquals(stubRequestHighPriority, spiceRequestList.get(0));
        assertEquals(stubRequestNormalPriority, spiceRequestList.get(1));
        assertEquals(stubRequestLowPriority, spiceRequestList.get(2));
    }
}
