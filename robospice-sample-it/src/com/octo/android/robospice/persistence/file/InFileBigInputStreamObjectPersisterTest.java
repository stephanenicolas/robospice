package com.octo.android.robospice.persistence.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import android.app.Application;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.file.InFileBigInputStreamObjectPersister;

@MediumTest
public class InFileBigInputStreamObjectPersisterTest extends InstrumentationTestCase {

	private static final String TEST_CACHE_KEY = "TEST_CACHE_KEY";

	private InFileBigInputStreamObjectPersister inputStreamCacheManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
		inputStreamCacheManager = new InFileBigInputStreamObjectPersister(application);
	}

	public void testSaveDataToCacheAndReturnData() throws Exception {
		inputStreamCacheManager.saveDataToCacheAndReturnData(new ByteArrayInputStream("coucou".getBytes()), TEST_CACHE_KEY);

		File cachedFile = inputStreamCacheManager.getCacheFile(TEST_CACHE_KEY);
		assertTrue(cachedFile.exists());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ByteStreams.copy(new FileInputStream(cachedFile), bos);
		assertTrue(Arrays.equals("coucou".getBytes(), bos.toByteArray()));
	}

	public void testLoadDataFromCache_no_expiracy() throws Exception {
		File cachedFile = inputStreamCacheManager.getCacheFile(TEST_CACHE_KEY);
		Files.write("coucou".getBytes(), cachedFile);

		InputStream inputStream = inputStreamCacheManager.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ALWAYS);
		byte[] actual = ByteStreams.toByteArray(inputStream);
		assertTrue(Arrays.equals("coucou".getBytes(), actual));
	}

	public void testLoadDataFromCache_not_expired() throws Exception {
		File cachedFile = inputStreamCacheManager.getCacheFile(TEST_CACHE_KEY);
		Files.write("coucou".getBytes(), cachedFile);

		InputStream inputStream = inputStreamCacheManager.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ONE_SECOND);
		byte[] actual = ByteStreams.toByteArray(inputStream);
		assertTrue(Arrays.equals("coucou".getBytes(), actual));
	}

	public void testLoadDataFromCache_expired() throws Exception {
		File cachedFile = inputStreamCacheManager.getCacheFile(TEST_CACHE_KEY);
		Files.write("coucou".getBytes(), cachedFile);
		cachedFile.setLastModified(System.currentTimeMillis() - 5 * DurationInMillis.ONE_SECOND);

		InputStream inputStream = inputStreamCacheManager.loadDataFromCache(TEST_CACHE_KEY, DurationInMillis.ONE_SECOND);
		assertNull(inputStream);
	}

	@Override
	protected void tearDown() throws Exception {
		inputStreamCacheManager.removeAllDataFromCache();
		super.tearDown();
	}

}
