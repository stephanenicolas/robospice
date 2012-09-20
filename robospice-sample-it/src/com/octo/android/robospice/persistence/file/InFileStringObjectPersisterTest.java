package com.octo.android.robospice.persistence.file;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Application;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.google.common.io.Files;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.file.InFileStringObjectPersister;

@MediumTest
public class InFileStringObjectPersisterTest extends InstrumentationTestCase {

	private static final String TEST_CACHE_KEY = "TEST_CACHE_KEY";

	private InFileStringObjectPersister inFileStringObjectPersister;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
		inFileStringObjectPersister = new InFileStringObjectPersister(application);
	}

	public void testSaveDataToCacheAndReturnData() throws Exception {
		inFileStringObjectPersister.saveDataToCacheAndReturnData("coucou", TEST_CACHE_KEY);

		File cachedFile = inFileStringObjectPersister.getCacheFile(TEST_CACHE_KEY);
		assertTrue(cachedFile.exists());

		List<String> actual = Files.readLines(cachedFile, Charset.forName("UTF-8"));
		assertEquals(1, actual.size());
		assertEquals("coucou", actual.get(0));
	}

	public void testSaveDataToCacheAndReturnData_async() throws Exception {
		inFileStringObjectPersister.setAsyncSaveEnabled(true);
		inFileStringObjectPersister.saveDataToCacheAndReturnData("coucou", TEST_CACHE_KEY);

		inFileStringObjectPersister.awaitForSaveAsyncTermination(500, TimeUnit.MILLISECONDS);
		File cachedFile = inFileStringObjectPersister.getCacheFile(TEST_CACHE_KEY);
		assertTrue(cachedFile.exists());

		List<String> actual = Files.readLines(cachedFile, Charset.forName("UTF-8"));
		assertEquals(1, actual.size());
		assertEquals("coucou", actual.get(0));
	}

	public void testLoadDataFromCache_no_expiracy() throws Exception {
		File cachedFile = inFileStringObjectPersister.getCacheFile(TEST_CACHE_KEY);
		Files.write("coucou", cachedFile, Charset.forName("UTF-8"));

		String actual = inFileStringObjectPersister.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ALWAYS);
		assertEquals("coucou", actual);
	}

	public void testLoadDataFromCache_not_expired() throws Exception {
		File cachedFile = inFileStringObjectPersister.getCacheFile(TEST_CACHE_KEY);
		Files.write("coucou", cachedFile, Charset.forName("UTF-8"));

		String actual = inFileStringObjectPersister.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ONE_SECOND);
		assertEquals("coucou", actual);
	}

	public void testLoadDataFromCache_expired() throws Exception {
		File cachedFile = inFileStringObjectPersister.getCacheFile(TEST_CACHE_KEY);
		Files.write("coucou", cachedFile, Charset.forName("UTF-8"));
		cachedFile.setLastModified(System.currentTimeMillis() - 5 * DurationInMillis.ONE_SECOND);

		String actual = inFileStringObjectPersister.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ONE_SECOND);
		assertNull(actual);
	}

	@Override
	protected void tearDown() throws Exception {
		inFileStringObjectPersister.removeAllDataFromCache();
	}

}
