package com.octo.android.robospice.persistence.springandroid.json.jackson2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.springandroid.test.model.json.CurrenWeather;
import com.octo.android.robospice.springandroid.test.model.json.Weather;
import com.octo.android.robospice.springandroid.test.model.json.WeatherResult;

@SmallTest
public class InFileWeatherPersisterTest extends InstrumentationTestCase {
    private static final long FIVE_SECONDS = 5 * DurationInMillis.ONE_SECOND;
    private static final String TEST_TEMP_UNIT = "C";
    private static final String TEST_TEMP = "28";
    private static final String TEST_TEMP2 = "30";
    private static final String FILE_NAME = "toto";
    private static final String FILE_NAME2 = "tutu";
    private InFileObjectPersister<WeatherResult> dataPersistenceManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        Jackson2ObjectPersisterFactory factory = new Jackson2ObjectPersisterFactory(application);
        dataPersistenceManager = factory.createObjectPersister(WeatherResult.class);
    }

    @Override
    protected void tearDown() throws Exception {
        dataPersistenceManager.removeAllDataFromCache();
        super.tearDown();
    }

    public void test_canHandleClientRequestStatus() {
        boolean canHandleClientWeatherResult = dataPersistenceManager.canHandleClass(WeatherResult.class);
        assertEquals(true, canHandleClientWeatherResult);
    }

    public void test_saveDataAndReturnData() throws Exception {
        // GIVEN
        WeatherResult weatherRequestStatus = buildWeather(TEST_TEMP, TEST_TEMP_UNIT);

        // WHEN
        WeatherResult weatherReturned = dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, "weather.json");

        // THEN
        assertEquals(TEST_TEMP, weatherReturned.getWeather().getCurren_weather().get(0).getTemp());
    }

    public void test_loadDataFromCache_no_expiracy() throws Exception {
        // GIVEN
        WeatherResult weatherRequestStatus = buildWeather(TEST_TEMP, TEST_TEMP_UNIT);

        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, FILE_NAME);

        // WHEN
        WeatherResult weatherReturned = dataPersistenceManager.loadDataFromCache(FILE_NAME, DurationInMillis.ALWAYS_RETURNED);

        // THEN
        assertEquals(TEST_TEMP, weatherReturned.getWeather().getCurren_weather().get(0).getTemp());
    }

    public void test_loadDataFromCache_not_expired() throws Exception {
        // GIVEN
        WeatherResult weatherRequestStatus = buildWeather(TEST_TEMP, TEST_TEMP_UNIT);

        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, FILE_NAME);

        // WHEN
        WeatherResult weatherReturned = dataPersistenceManager.loadDataFromCache(FILE_NAME, FIVE_SECONDS);

        // THEN
        assertEquals(TEST_TEMP, weatherReturned.getWeather().getCurren_weather().get(0).getTemp());
    }

    public void test_loadDataFromCache_expired() throws Exception {
        // GIVEN
        WeatherResult weatherRequestStatus = buildWeather(TEST_TEMP2, TEST_TEMP_UNIT);

        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, FILE_NAME);
        File cachedFile = ((Jackson2ObjectPersister<?>) dataPersistenceManager).getCacheFile(FILE_NAME);
        cachedFile.setLastModified(System.currentTimeMillis() - FIVE_SECONDS);

        // WHEN
        WeatherResult weatherReturned = dataPersistenceManager.loadDataFromCache(FILE_NAME, DurationInMillis.ONE_SECOND);

        // THEN
        assertNull(weatherReturned);
    }

    public void test_loadAllDataFromCache_with_one_request_in_cache() throws Exception {
        // GIVEN
        WeatherResult weatherRequestStatus = buildWeather(TEST_TEMP, TEST_TEMP_UNIT);

        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, FILE_NAME);

        // WHEN
        List<WeatherResult> listWeatherResult = dataPersistenceManager.loadAllDataFromCache();

        // THEN
        assertNotNull(listWeatherResult);
        assertEquals(1, listWeatherResult.size());
        assertEquals(weatherRequestStatus, listWeatherResult.get(0));
    }

    public void test_loadAllDataFromCache_with_two_requests_in_cache() throws Exception {
        // GIVEN
        WeatherResult weatherRequestStatus = buildWeather(TEST_TEMP, TEST_TEMP_UNIT);

        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, FILE_NAME);

        WeatherResult weatherRequestStatus2 = buildWeather(TEST_TEMP2, TEST_TEMP_UNIT);

        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus2, FILE_NAME2);

        // WHEN
        List<WeatherResult> listWeatherResult = dataPersistenceManager.loadAllDataFromCache();

        // THEN
        assertNotNull(listWeatherResult);
        assertEquals(2, listWeatherResult.size());
        assertTrue(listWeatherResult.contains(weatherRequestStatus));
        assertTrue(listWeatherResult.contains(weatherRequestStatus2));
    }

    public void test_loadAllDataFromCache_with_no_requests_in_cache() throws Exception {
        // GIVEN

        // WHEN
        List<WeatherResult> listWeatherResult = dataPersistenceManager.loadAllDataFromCache();

        // THEN
        assertNotNull(listWeatherResult);
        assertTrue(listWeatherResult.isEmpty());
    }

    public void test_removeDataFromCache_when_two_requests_in_cache_and_one_removed() throws Exception {
        // GIVEN
        WeatherResult weatherRequestStatus = buildWeather(TEST_TEMP, TEST_TEMP_UNIT);

        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, FILE_NAME);

        WeatherResult weatherRequestStatus2 = buildWeather(TEST_TEMP2, TEST_TEMP_UNIT);

        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus2, FILE_NAME2);

        dataPersistenceManager.removeDataFromCache(FILE_NAME2);

        // WHEN
        List<WeatherResult> listWeatherResult = dataPersistenceManager.loadAllDataFromCache();

        // THEN
        assertNotNull(listWeatherResult);
        assertEquals(1, listWeatherResult.size());
        assertTrue(listWeatherResult.contains(weatherRequestStatus));
        assertFalse(listWeatherResult.contains(weatherRequestStatus2));
    }

    private WeatherResult buildWeather(String temp, String tempUnit) {
        WeatherResult weatherRequestStatus = new WeatherResult();
        Weather weather = new Weather();
        List<CurrenWeather> currents = new ArrayList<CurrenWeather>();
        CurrenWeather current_weather = new CurrenWeather();
        current_weather.setTemp(temp);
        current_weather.setTemp_unit(tempUnit);
        currents.add(current_weather);
        weather.setCurren_weather(currents);
        weatherRequestStatus.setWeather(weather);
        return weatherRequestStatus;
    }
}
