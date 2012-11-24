package com.octo.android.robospice.persistence.json.gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import roboguice.util.temp.Ln;
import android.app.Application;

import com.google.gson.Gson;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;

public final class GsonObjectPersister<T> extends InFileObjectPersister<T> {

	// ============================================================================================
	// ATTRIBUTES
	// ============================================================================================

	private final Gson gson;

	private String mFactoryPrefix;

	// ============================================================================================
	// CONSTRUCTOR
	// ============================================================================================
	public GsonObjectPersister(Application application, Class<T> clazz, String factoryPrefix) {
		super(application, clazz);
		this.gson = new Gson();
		this.mFactoryPrefix = factoryPrefix;
	}

	// ============================================================================================
	// METHODS
	// ============================================================================================

	@Override
	protected String getCachePrefix() {
		return mFactoryPrefix + super.getCachePrefix();
	}

	@Override
	public final T loadDataFromCache(Object cacheKey, long maxTimeInCacheBeforeExpiry) throws CacheLoadingException {
		T result = null;
		String resultJson = null;

		File file = getCacheFile(cacheKey);
		if (file.exists()) {
			long timeInCache = System.currentTimeMillis() - file.lastModified();
			if (maxTimeInCacheBeforeExpiry == 0 || timeInCache <= maxTimeInCacheBeforeExpiry) {
				try {
					resultJson = FileUtils.readFileToString(file, CharEncoding.UTF_8);

					// finally transform json in object
					if (!StringUtils.isEmpty(resultJson)) {
						result = gson.fromJson(resultJson, getHandledClass());
						return result;
					}
					throw new CacheLoadingException("Unable to restore cache content : cache file is empty");
				}
				catch (FileNotFoundException e) {
					// Should not occur (we test before if file exists)
					// Do not throw, file is not cached
					Ln.w("file " + file.getAbsolutePath() + " does not exists", e);
					return null;
				}
				catch (CacheLoadingException e) {
					throw e;
				}
				catch (Exception e) {
					throw new CacheLoadingException(e);
				}
			}
			Ln.v("Cache content is expired since " + (maxTimeInCacheBeforeExpiry - timeInCache));
			return null;
		}
		Ln.v("file " + file.getAbsolutePath() + " does not exists");
		return null;
	}

	@Override
	public T saveDataToCacheAndReturnData(final T data, final Object cacheKey) throws CacheSavingException {

		try {
			if (isAsyncSaveEnabled) {
				new Thread() {
					@Override
					public void run() {
						try {
							saveData(data, cacheKey);
						}
						catch (IOException e) {
							Ln.e(e, "An error occured on saving request " + cacheKey + " data asynchronously");
						}
						catch (CacheSavingException e) {
							Ln.e(e, "An error occured on saving request " + cacheKey + " data asynchronously");
						}
						finally {
							// notify that saving is finished for test purpose
							lock.lock();
							condition.signal();
							lock.unlock();
						}
					};
				}.start();
			}
			else {
				saveData(data, cacheKey);
			}
		}
		catch (CacheSavingException e) {
			throw e;
		}
		catch (Exception e) {
			throw new CacheSavingException(e);
		}
		return data;
	}

	private void saveData(T data, Object cacheKey) throws IOException, JsonGenerationException, JsonMappingException, CacheSavingException {
		String resultJson;
		// transform the content in json to store it in the cache
		resultJson = gson.toJson(data);

		// finally store the json in the cache
		if (!StringUtils.isEmpty(resultJson)) {
			FileUtils.writeStringToFile(getCacheFile(cacheKey), resultJson, CharEncoding.UTF_8);
		}
		else {
			throw new CacheSavingException("Data was null and could not be serialized in json");
		}
	}

	@Override
	public boolean canHandleClass(Class<?> clazz) {
		return true;
	}

	/** for testing purpose only. Overriding allows to regive package level visibility. */
	@Override
	protected void awaitForSaveAsyncTermination(long time, TimeUnit timeUnit) throws InterruptedException {
		super.awaitForSaveAsyncTermination(time, timeUnit);
	}

	/** for testing purpose only. Overriding allows to regive package level visibility. */
	@Override
	protected File getCacheFile(Object cacheKey) {
		return super.getCacheFile(cacheKey);
	}

}
