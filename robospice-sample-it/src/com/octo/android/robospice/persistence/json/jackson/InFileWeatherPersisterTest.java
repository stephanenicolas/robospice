package com.octo.android.robospice.persistence.json.jackson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Application;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.persistence.json.jackson.JacksonObjectPersister;
import com.octo.android.robospice.persistence.json.jackson.JacksonObjectPersisterFactory;
import com.octo.android.robospice.sample.model.json.Curren_weather;
import com.octo.android.robospice.sample.model.json.Weather;
import com.octo.android.robospice.sample.model.json.WeatherResult;

@SmallTest
public class InFileWeatherPersisterTest extends InstrumentationTestCase {
    private InFileObjectPersister< WeatherResult > dataPersistenceManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        JacksonObjectPersisterFactory factory = new JacksonObjectPersisterFactory( application );
        dataPersistenceManager = factory.createObjectPersister( WeatherResult.class );
    }

    public void test_canHandleClientRequestStatus() {
        boolean canHandleClientWeatherResult = dataPersistenceManager.canHandleClass( WeatherResult.class );
        assertEquals( true, canHandleClientWeatherResult );
    }

    public void test_saveDataAndReturnData() throws Exception {
        // GIVEN
        WeatherResult weatherRequestStatus = buildWeather();

        // WHEN
        WeatherResult weatherReturned = dataPersistenceManager.saveDataToCacheAndReturnData( weatherRequestStatus, "weather.json" );

        // THEN
        assertEquals( "28", weatherReturned.getWeather().getCurren_weather().get( 0 ).getTemp() );
    }

    public void test_saveDataAndReturnData_async() throws Exception {
        // GIVEN
        WeatherResult weatherRequestStatus = buildWeather();

        // WHEN
        dataPersistenceManager.setAsyncSaveEnabled( true );
        WeatherResult weatherReturned = dataPersistenceManager.saveDataToCacheAndReturnData( weatherRequestStatus, "weather.json" );

        // THEN
        ( (JacksonObjectPersister< ? >) dataPersistenceManager ).awaitForSaveAsyncTermination( 500, TimeUnit.MILLISECONDS );
        assertEquals( "28", weatherReturned.getWeather().getCurren_weather().get( 0 ).getTemp() );
    }

    public void test_loadDataFromCache_no_expiracy() throws Exception {
        // GIVEN
        WeatherResult weatherRequestStatus = buildWeather();
        final String FILE_NAME = "toto";
        dataPersistenceManager.saveDataToCacheAndReturnData( weatherRequestStatus, FILE_NAME );

        // WHEN
        WeatherResult weatherReturned = dataPersistenceManager.loadDataFromCache( FILE_NAME, DurationInMillis.ALWAYS );

        // THEN
        assertEquals( "28", weatherReturned.getWeather().getCurren_weather().get( 0 ).getTemp() );
    }

    public void test_loadDataFromCache_not_expired() throws Exception {
        // GIVEN
        WeatherResult weatherRequestStatus = buildWeather();
        final String FILE_NAME = "toto";
        dataPersistenceManager.saveDataToCacheAndReturnData( weatherRequestStatus, FILE_NAME );

        // WHEN
        WeatherResult weatherReturned = dataPersistenceManager.loadDataFromCache( FILE_NAME, DurationInMillis.ONE_SECOND );

        // THEN
        assertEquals( "28", weatherReturned.getWeather().getCurren_weather().get( 0 ).getTemp() );
    }

    public void test_loadDataFromCache_expired() throws Exception {
        // GIVEN
        WeatherResult weatherRequestStatus = buildWeather();
        final String FILE_NAME = "toto";
        dataPersistenceManager.saveDataToCacheAndReturnData( weatherRequestStatus, FILE_NAME );
        File cachedFile = ( (JacksonObjectPersister< ? >) dataPersistenceManager ).getCacheFile( FILE_NAME );
        cachedFile.setLastModified( System.currentTimeMillis() - 5 * DurationInMillis.ONE_SECOND );

        // WHEN
        WeatherResult weatherReturned = dataPersistenceManager.loadDataFromCache( FILE_NAME, DurationInMillis.ONE_SECOND );

        // THEN
        assertNull( weatherReturned );
    }

    private WeatherResult buildWeather() {
        WeatherResult weatherRequestStatus = new WeatherResult();
        Weather weather = new Weather();
        List< Curren_weather > currents = new ArrayList< Curren_weather >();
        Curren_weather current_weather = new Curren_weather();
        current_weather.setTemp( "28" );
        current_weather.setTemp_unit( "C" );
        currents.add( current_weather );
        weather.setCurren_weather( currents );
        weatherRequestStatus.setWeather( weather );
        return weatherRequestStatus;
    }
}
