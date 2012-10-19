package com.octo.android.robospice.persistence;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

@SmallTest
public class CacheManagerTest extends AndroidTestCase {

    private CacheManager cacheManager;

    @Override
    protected void setUp() throws Exception {
        cacheManager = new CacheManager();
    }

    public void testEmptyDataPersistenceManager() {
        try {
            cacheManager.getObjectPersister( Object.class );
            fail( "No data class persistence manager should have been found as none had been registered" );
        } catch ( Exception ex ) {
            assertTrue( true );
        }
    }

    public void testRegisterDataClassPersistenceManager() {
        MockDataClassPersistenceManager mockDataClassPersistenceManager = new MockDataClassPersistenceManager();
        cacheManager.addPersister( mockDataClassPersistenceManager );
        ObjectPersister< ? > actual = cacheManager.getObjectPersister( String.class );
        assertEquals( mockDataClassPersistenceManager, actual );
    }

    public void testGetDataClassPersistenceManager_returns_CacheManagerBusElement_in_order() {
        // register a data class persistence manager first
        MockDataClassPersistenceManager mockDataClassPersistenceManager = new MockDataClassPersistenceManager();
        cacheManager.addPersister( mockDataClassPersistenceManager );

        // register a second data class persistence manager
        MockDataClassPersistenceManager mockDataClassPersistenceManager2 = new MockDataClassPersistenceManager();
        cacheManager.addPersister( mockDataClassPersistenceManager2 );

        ObjectPersister< ? > actual = cacheManager.getObjectPersister( String.class );
        assertEquals( mockDataClassPersistenceManager, actual );
    }

    public void testUnRegisterDataClassPersistenceManager() {
        // register a data class persistence manager first
        MockDataClassPersistenceManager mockDataClassPersistenceManager = new MockDataClassPersistenceManager();
        cacheManager.addPersister( mockDataClassPersistenceManager );
        ObjectPersister< ? > actual = cacheManager.getObjectPersister( String.class );
        assertEquals( mockDataClassPersistenceManager, actual );

        // unregister it
        cacheManager.removePersister( mockDataClassPersistenceManager );

        // no persistence manager should be found any more
        try {
            cacheManager.getObjectPersister( String.class );
            fail( "No data class persistence manager should have been found as none had been registered" );
        } catch ( Exception ex ) {
            assertTrue( true );
        }
    }

    private class MockDataClassPersistenceManager extends ObjectPersister< String > {
        private static final String TEST_PERSISTED_STRING = "TEST";

        public MockDataClassPersistenceManager() {
            super( null, String.class );
        }

        @Override
        public boolean canHandleClass( Class< ? > arg0 ) {
            return arg0.equals( String.class );
        }

        @Override
        public String loadDataFromCache( Object arg0, long arg1 ) throws CacheLoadingException {
            return TEST_PERSISTED_STRING;
        }

        @Override
        public String saveDataToCacheAndReturnData( String arg0, Object arg1 ) throws CacheSavingException {
            return TEST_PERSISTED_STRING;
        }

        @Override
        public void removeAllDataFromCache() {
        }

        @Override
        public boolean removeDataFromCache( Object arg0 ) {
            return true;
        }

        @Override
        public List< String > loadAllDataFromCache() throws CacheLoadingException {
            ArrayList< String > listString = new ArrayList< String >();
            listString.add( TEST_PERSISTED_STRING );
            return listString;
        }

        @Override
        public List< Object > getAllCacheKeys() {
            return null;
        }
    }
}
