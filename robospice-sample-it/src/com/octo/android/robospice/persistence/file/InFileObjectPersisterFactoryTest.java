package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Application;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.google.common.io.Files;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.persistence.file.InFileObjectPersisterFactory;

@MediumTest
public class InFileObjectPersisterFactoryTest extends InstrumentationTestCase {

	InFileObjectPersisterFactory fileBasedClassCacheManagerFactory;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
		fileBasedClassCacheManagerFactory = new FileBaseClassCacheManagerFactoryUnderTest(application);
	}

	@Override
	protected void tearDown() throws Exception {
		fileBasedClassCacheManagerFactory.removeAllDataFromCache();
		super.tearDown();
	}

	public void testGetCachePrefix() {
		String actual = fileBasedClassCacheManagerFactory.getCachePrefix();
		assertEquals(FileBaseClassCacheManagerFactoryUnderTest.class.getSimpleName() + "_", actual);
	}

	public void testRemoveAllDataFromCache() throws FileNotFoundException, IOException {
		final String TEST_CACHE_KEY = "TEST_CACHE_KEY";

		Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
		File cacheDir = application.getCacheDir();
		File testFile1 = new File(cacheDir, fileBasedClassCacheManagerFactory.getCachePrefix() + TEST_CACHE_KEY);

		final String TEST_CACHE_KEY2 = "TEST_CACHE_KEY2";
		File testFile2 = new File(cacheDir, fileBasedClassCacheManagerFactory.getCachePrefix() + TEST_CACHE_KEY2);

		Files.touch(testFile1);
		Files.touch(testFile2);

		assertTrue(testFile1.exists());
		assertTrue(testFile2.exists());

		fileBasedClassCacheManagerFactory.removeAllDataFromCache();

		assertFalse(testFile1.exists());
		assertFalse(testFile2.exists());
	}

	// ============================================================================================
	// CLASS UNDER TEST
	// ============================================================================================
	private final class FileBaseClassCacheManagerFactoryUnderTest extends InFileObjectPersisterFactory {
		private FileBaseClassCacheManagerFactoryUnderTest(Application application) {
			super(application);
		}

		@Override
		public boolean canHandleClass(Class<?> arg0) {
			return false;
		}

		@Override
		public <DATA> InFileObjectPersister<DATA> createClassCacheManager(Class<DATA> clazz) {
			return null;
		}

	}

}
