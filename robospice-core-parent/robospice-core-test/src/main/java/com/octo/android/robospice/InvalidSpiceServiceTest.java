package com.octo.android.robospice;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.core.test.InvalidSpiceTestService;

//Thanks to http://stackoverflow.com/questions/2300029/servicetestcaset-getservice
@SmallTest
public class InvalidSpiceServiceTest extends AndroidTestCase {

    public void test_creating_service_with_null_cache_manager_throws_exception() {
        InvalidSpiceTestService invalidSpiceTestService = new InvalidSpiceTestService();
        invalidSpiceTestService.onCreate();
        assertFalse(invalidSpiceTestService.isCreated());
    }
}
