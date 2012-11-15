package com.octo.android.robospice.persistence.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.Application;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.file.InFileObjectPersister;
import com.octo.android.robospice.sample.model.xml.Curren_weather;
import com.octo.android.robospice.sample.model.xml.Forecast;
import com.octo.android.robospice.sample.model.xml.Weather;
import com.octo.android.robospice.sample.model.xml.Wind;

@SmallTest
public class InFileWeatherPersisterTest extends InstrumentationTestCase {
    private InFileObjectPersister< Weather > dataPersistenceManager;
    private static final Curren_weather TEST_TEMP = new Curren_weather();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Application application = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        SimpleSerializerObjectPersisterFactory factory = new SimpleSerializerObjectPersisterFactory( application );
        dataPersistenceManager = factory.createObjectPersister( Weather.class );
        TEST_TEMP.setTemp( "28" );
        TEST_TEMP.setTemp_unit( "C" );
        TEST_TEMP.setWind( new Wind() );
    }

    public void test_canHandleClientRequestStatus() {
        boolean canHandleClientWeather = dataPersistenceManager.canHandleClass( Weather.class );
        assertEquals( true, canHandleClientWeather );
    }

    public void test_saveDataAndReturnData() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather();

        // WHEN
        Weather weatherReturned = dataPersistenceManager.saveDataToCacheAndReturnData( weatherRequestStatus, "weather.xml" );

        // THEN
        assertTrue( weatherReturned.getCurren_weather().contains( TEST_TEMP ) );
    }

    public void test_saveDataAndReturnData_async() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather();

        // WHEN
        dataPersistenceManager.setAsyncSaveEnabled( true );
        Weather weatherReturned = dataPersistenceManager.saveDataToCacheAndReturnData( weatherRequestStatus, "weather.xml" );

        // THEN
        ( (SimpleSerializerObjectPersister< ? >) dataPersistenceManager ).awaitForSaveAsyncTermination( 500, TimeUnit.MILLISECONDS );
        assertTrue( weatherReturned.getCurren_weather().contains( TEST_TEMP ) );
    }

    public void test_loadDataFromCache_no_expiracy() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather();
        final String FILE_NAME = "toto";
        dataPersistenceManager.saveDataToCacheAndReturnData( weatherRequestStatus, FILE_NAME );

        // WHEN
        Weather weatherReturned = dataPersistenceManager.loadDataFromCache( FILE_NAME, DurationInMillis.ALWAYS );

        // THEN
        assertTrue( weatherReturned.getCurren_weather().contains( TEST_TEMP ) );
    }

    public void test_loadDataFromCache_not_expired() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather();
        final String FILE_NAME = "toto";
        dataPersistenceManager.saveDataToCacheAndReturnData( weatherRequestStatus, FILE_NAME );

        // WHEN
        Weather weatherReturned = dataPersistenceManager.loadDataFromCache( FILE_NAME, DurationInMillis.ONE_SECOND );

        // THEN
        assertTrue( weatherReturned.getCurren_weather().contains( TEST_TEMP ) );
    }

    public void test_loadDataFromCache_expired() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather();
        final String FILE_NAME = "toto";
        dataPersistenceManager.saveDataToCacheAndReturnData( weatherRequestStatus, FILE_NAME );
        File cachedFile = ( (SimpleSerializerObjectPersister< ? >) dataPersistenceManager ).getCacheFile( FILE_NAME );
        cachedFile.setLastModified( System.currentTimeMillis() - 5 * DurationInMillis.ONE_SECOND );

        // WHEN
        Weather weatherReturned = dataPersistenceManager.loadDataFromCache( FILE_NAME, DurationInMillis.ONE_SECOND );

        // THEN
        assertNull( weatherReturned );
    }

    private Weather buildWeather() {
        Weather weather = new Weather();
        List< Curren_weather > currents = new ArrayList< Curren_weather >();
        currents.add( TEST_TEMP );
        weather.setCurren_weather( currents );
        List< Forecast > forecasts = new ArrayList< Forecast >();
        weather.setForecast( forecasts );
        return weather;
    }
}
