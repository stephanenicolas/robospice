package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.io.IOException;

import android.app.Application;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.octo.android.robospice.exception.CacheLoadingException;
import com.octo.android.robospice.exception.CacheSavingException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;

@MediumTest
public class InFileObjectPersisterTest extends InstrumentationTestCase {

	InFileObjectPersister<Object> fileBasedClassCacheManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
		fileBasedClassCacheManager = new InFileObjectPersisterUnderTest(application);
	}

	@Override
	protected void tearDown() throws Exception {
		fileBasedClassCacheManager.removeAllDataFromCache();
		super.tearDown();
	}

	public void testGetCachePrefix() {
		String actual = fileBasedClassCacheManager.getCachePrefix();
		assertEquals(InFileObjectPersisterUnderTest.class.getSimpleName() + "_", actual);
	}

	public void testRemoveDataFromCache() throws Exception {
		final String TEST_CACHE_KEY = "TEST_CACHE_KEY";
		fileBasedClassCacheManager.saveDataToCacheAndReturnData(new Object(), TEST_CACHE_KEY);

		File cacheFile = fileBasedClassCacheManager.getCacheFile(TEST_CACHE_KEY);
		assertTrue(cacheFile.exists());

		fileBasedClassCacheManager.removeDataFromCache(TEST_CACHE_KEY);
		assertFalse(cacheFile.exists());
	}

	public void testRemoveAllDataFromCache() throws Exception {
		final String TEST_CACHE_KEY = "TEST_CACHE_KEY";
		fileBasedClassCacheManager.saveDataToCacheAndReturnData(new Object(), TEST_CACHE_KEY);

		final String TEST_CACHE_KEY2 = "TEST_CACHE_KEY2";
		fileBasedClassCacheManager.saveDataToCacheAndReturnData(new Object(), TEST_CACHE_KEY2);

		File cacheFile = fileBasedClassCacheManager.getCacheFile(TEST_CACHE_KEY);
		assertTrue(cacheFile.exists());

		File cacheFile2 = fileBasedClassCacheManager.getCacheFile(TEST_CACHE_KEY2);
		assertTrue(cacheFile2.exists());

		fileBasedClassCacheManager.removeAllDataFromCache();
		assertFalse(cacheFile.exists());
		assertFalse(cacheFile2.exists());
	}

	// ============================================================================================
	// CLASS UNDER TEST
	// ============================================================================================
	private final class InFileObjectPersisterUnderTest extends InFileObjectPersister<Object> {
		private InFileObjectPersisterUnderTest(Application application) {
			super(application);
		}

		@Override
		public boolean canHandleClass(Class<?> arg0) {
			return false;
		}

		@Override
		public Object loadDataFromCache(Object arg0, long arg1) throws CacheLoadingException {
			return null;
		}

		@Override
		public Object saveDataToCacheAndReturnData(Object data, Object cacheKey) throws CacheSavingException {
			try {
				getCacheFile(cacheKey).createNewFile();
			} catch (IOException e) {
				throw new CacheSavingException(e);
			}
			return data;
		}
	}

}
