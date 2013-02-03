package com.octo.android.robospice;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.core.test.InvalidSpiceTestService;

//Thanks to http://stackoverflow.com/questions/2300029/servicetestcaset-getservice
@SmallTest
public class InvalidSpiceServiceTest extends
    ServiceTestCase<InvalidSpiceTestService> {

    public InvalidSpiceServiceTest() {
        super(InvalidSpiceTestService.class);
    }

    public void test_starting_service_throws_exception() {
        try {
            Intent startIntent = new Intent();
            startIntent.setClass(getContext(), InvalidSpiceTestService.class);
            startService(startIntent);
            fail("Should return IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(
                "createCacheManager() can't create a null cacheManager",
                e.getMessage());
        }
    }

}
