package com.octo.android.robospice.persistence.springandroid.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.springandroid.test.model.xml.Curren_weather;
import com.octo.android.robospice.springandroid.test.model.xml.Weather;
import com.octo.android.robospice.springandroid.test.model.xml.Wind;

@SmallTest
public class InFileWeatherPersisterTest extends InstrumentationTestCase {
    private static final long FIVE_SECONDS = 5 * DurationInMillis.ONE_SECOND;
    private InFileObjectPersister<Weather> dataPersistenceManager;
    private static final String FILE_NAME = "toto";
    private static final String FILE_NAME2 = "tutu";
    private static final Curren_weather TEST_TEMP = new Curren_weather();
    private static final Curren_weather TEST_TEMP2 = new Curren_weather();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Application application = (Application) getInstrumentation()
            .getTargetContext().getApplicationContext();
        SimpleSerializerObjectPersisterFactory factory = new SimpleSerializerObjectPersisterFactory(
            application);
        dataPersistenceManager = factory.createObjectPersister(Weather.class);
        TEST_TEMP.setTemp("28");
        TEST_TEMP.setTemp_unit("C");
        TEST_TEMP.setWind(new Wind());
        TEST_TEMP2.setTemp("30");
        TEST_TEMP2.setTemp_unit("C");
        TEST_TEMP2.setWind(new Wind());
    }

    @Override
    protected void tearDown() throws Exception {
        dataPersistenceManager.removeAllDataFromCache();
        super.tearDown();
    }

    public void test_canHandleClientRequestStatus() {
        boolean canHandleClientWeather = dataPersistenceManager
            .canHandleClass(Weather.class);
        assertEquals(true, canHandleClientWeather);
    }

    public void test_saveDataAndReturnData() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(TEST_TEMP);

        // WHEN
        Weather weatherReturned = dataPersistenceManager
            .saveDataToCacheAndReturnData(weatherRequestStatus, "weather.xml");

        // THEN
        assertTrue(weatherReturned.getListWeather().contains(TEST_TEMP));
    }

    public void test_loadDataFromCache_no_expiracy() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(TEST_TEMP);

        dataPersistenceManager.saveDataToCacheAndReturnData(
            weatherRequestStatus, FILE_NAME);

        // WHEN
        Weather weatherReturned = dataPersistenceManager.loadDataFromCache(
            FILE_NAME, DurationInMillis.ALWAYS);

        // THEN
        assertTrue(weatherReturned.getListWeather().contains(TEST_TEMP));
    }

    public void test_loadDataFromCache_not_expired() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(TEST_TEMP);

        dataPersistenceManager.saveDataToCacheAndReturnData(
            weatherRequestStatus, FILE_NAME);

        // WHEN
        Weather weatherReturned = dataPersistenceManager.loadDataFromCache(
            FILE_NAME, DurationInMillis.ONE_SECOND);

        // THEN
        assertTrue(weatherReturned.getListWeather().contains(TEST_TEMP));
    }

    public void test_loadDataFromCache_expired() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(TEST_TEMP);

        dataPersistenceManager.saveDataToCacheAndReturnData(
            weatherRequestStatus, FILE_NAME);
        File cachedFile = ((SimpleSerializerObjectPersister<?>) dataPersistenceManager)
            .getCacheFile(FILE_NAME);
        cachedFile.setLastModified(System.currentTimeMillis() - FIVE_SECONDS);

        // WHEN
        Weather weatherReturned = dataPersistenceManager.loadDataFromCache(
            FILE_NAME, DurationInMillis.ONE_SECOND);

        // THEN
        assertNull(weatherReturned);
    }

    public void test_loadAllDataFromCache_with_one_request_in_cache()
        throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(TEST_TEMP);

        dataPersistenceManager.saveDataToCacheAndReturnData(
            weatherRequestStatus, FILE_NAME);

        // WHEN
        List<Weather> listWeatherResult = dataPersistenceManager
            .loadAllDataFromCache();

        // THEN
        assertNotNull(listWeatherResult);
        assertEquals(1, listWeatherResult.size());
        assertEquals(weatherRequestStatus, listWeatherResult.get(0));
    }

    public void test_loadAllDataFromCache_with_two_requests_in_cache()
        throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(TEST_TEMP);

        dataPersistenceManager.saveDataToCacheAndReturnData(
            weatherRequestStatus, FILE_NAME);

        Weather weatherRequestStatus2 = buildWeather(TEST_TEMP2);

        dataPersistenceManager.saveDataToCacheAndReturnData(
            weatherRequestStatus2, FILE_NAME2);

        // WHEN
        List<Weather> listWeatherResult = dataPersistenceManager
            .loadAllDataFromCache();

        // THEN
        assertNotNull(listWeatherResult);
        assertEquals(2, listWeatherResult.size());
        assertTrue(listWeatherResult.contains(weatherRequestStatus));
        assertTrue(listWeatherResult.contains(weatherRequestStatus2));
    }

    public void test_loadAllDataFromCache_with_no_requests_in_cache()
        throws Exception {
        // GIVEN

        // WHEN
        List<Weather> listWeatherResult = dataPersistenceManager
            .loadAllDataFromCache();

        // THEN
        assertNotNull(listWeatherResult);
        assertTrue(listWeatherResult.isEmpty());
    }

    public void test_removeDataFromCache_when_two_requests_in_cache_and_one_removed()
        throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(TEST_TEMP);

        dataPersistenceManager.saveDataToCacheAndReturnData(
            weatherRequestStatus, FILE_NAME);

        Weather weatherRequestStatus2 = buildWeather(TEST_TEMP2);

        dataPersistenceManager.saveDataToCacheAndReturnData(
            weatherRequestStatus2, FILE_NAME2);

        dataPersistenceManager.removeDataFromCache(FILE_NAME2);

        // WHEN
        List<Weather> listWeatherResult = dataPersistenceManager
            .loadAllDataFromCache();

        // THEN
        assertNotNull(listWeatherResult);
        assertEquals(1, listWeatherResult.size());
        assertTrue(listWeatherResult.contains(weatherRequestStatus));
        assertFalse(listWeatherResult.contains(weatherRequestStatus2));
    }

    private Weather buildWeather(Curren_weather curren_weather) {
        Weather weather = new Weather();
        List<Curren_weather> currents = new ArrayList<Curren_weather>();
        currents.add(curren_weather);
        weather.setListWeather(currents);
        weather.setListForecast(null);
        return weather;
    }
}
