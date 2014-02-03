package com.octo.android.robospice.persistence.ormlite;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.test.mock.MockApplication;
import android.test.mock.MockContentResolver;
import android.test.suitebuilder.annotation.SmallTest;

import com.octo.android.robospice.ormlite.test.model.CurrenWeather;
import com.octo.android.robospice.ormlite.test.model.Day;
import com.octo.android.robospice.ormlite.test.model.Forecast;
import com.octo.android.robospice.ormlite.test.model.Night;
import com.octo.android.robospice.ormlite.test.model.Weather;
import com.octo.android.robospice.ormlite.test.model.Wind;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.ObjectPersister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SmallTest
public class InDatabaseWeatherPersisterTest extends AndroidTestCase {
    private ObjectPersister<Weather> dataPersistenceManager;
    private NotificationContentResolver mResolver;
    private Application mApplication;

    private static final CurrenWeather TEST_TEMP = new CurrenWeather();
    private static final CurrenWeather TEST_TEMP2 = new CurrenWeather();
    private static final int WEATHER_ID = 1;
    private static final int WEATHER_ID2 = 2;
    private static final int CACHE_KEY = 1;
    private static final int CACHE_KEY2 = 2;
    private static final String CACHE_KEY3_STRING = "cache_key_3";

    private static final Uri NOTIFICATION_URI1 = Uri.EMPTY.buildUpon().appendPath("path1").build();
    private static final Uri NOTIFICATION_URI2 = Uri.EMPTY.buildUpon().appendPath("path2").build();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Map<Class<?>, Uri> handledClassesToNotificationUri  = new HashMap<Class<?>, Uri>();

        // add persisted classes to mapping class to notification Uri
        handledClassesToNotificationUri.put(Weather.class, NOTIFICATION_URI1);
        handledClassesToNotificationUri.put(CurrenWeather.class, NOTIFICATION_URI2);
        handledClassesToNotificationUri.put(Day.class, null);
        handledClassesToNotificationUri.put(Forecast.class, null);
        handledClassesToNotificationUri.put(Night.class, null);
        handledClassesToNotificationUri.put(Wind.class, null);

        mResolver = new NotificationContentResolver();
        mApplication = new NotificationApplication(getContext(), mResolver);

        RoboSpiceDatabaseHelper databaseHelper = new RoboSpiceDatabaseHelper(mApplication, "sample_database.db", 1);
        databaseHelper.clearTableFromDataBase(Weather.class);
        InDatabaseObjectPersisterFactory inDatabaseObjectPersisterFactory = new InDatabaseObjectPersisterFactory(mApplication, databaseHelper,
                handledClassesToNotificationUri);
        dataPersistenceManager = inDatabaseObjectPersisterFactory.createObjectPersister(Weather.class);

        TEST_TEMP.setTemp("28");
        TEST_TEMP.setTemp_unit("C");
        TEST_TEMP2.setTemp("30");
        TEST_TEMP2.setTemp_unit("C");
    }

    @Override
    protected void tearDown() throws Exception {
        dataPersistenceManager.removeAllDataFromCache();
        super.tearDown();
    }

    public void test_canHandleClientRequestStatus() {
        boolean canHandleClientWeather = dataPersistenceManager.canHandleClass(Weather.class);
        assertEquals(true, canHandleClientWeather);
    }

    public void test_saveDataAndReturnData() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(WEATHER_ID, TEST_TEMP);

        // WHEN
        Weather weatherReturned = dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, 1);

        // THEN
        assertTrue(weatherReturned.getListWeather().contains(TEST_TEMP));
    }

    public void test_loadDataFromCache_no_expiracy() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(WEATHER_ID, TEST_TEMP);
        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, CACHE_KEY);

        // WHEN
        Weather weatherReturned = dataPersistenceManager.loadDataFromCache(CACHE_KEY, DurationInMillis.ALWAYS_RETURNED);

        // THEN
        assertEquals(WEATHER_ID, weatherReturned.getId());
        assertTrue(weatherReturned.getListWeather().contains(TEST_TEMP));
    }

    public void test_loadDataFromCache_not_expired() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(WEATHER_ID, TEST_TEMP);
        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, CACHE_KEY);

        // WHEN
        Weather weatherReturned = dataPersistenceManager.loadDataFromCache(CACHE_KEY, DurationInMillis.ONE_SECOND);

        // THEN
        assertTrue(weatherReturned.getListWeather().contains(TEST_TEMP));
    }

    public void test_loadDataFromCache_expired() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(WEATHER_ID, TEST_TEMP);
        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, CACHE_KEY);
        Thread.sleep(DurationInMillis.ONE_SECOND);
        // WHEN
        Weather weatherReturned = dataPersistenceManager.loadDataFromCache(CACHE_KEY, DurationInMillis.ONE_SECOND);

        // THEN
        assertNull(weatherReturned);
    }

    public void test_loadAllDataFromCache_with_one_request_in_cache() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(WEATHER_ID, TEST_TEMP);
        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, CACHE_KEY);

        // WHEN
        List<Weather> listWeatherResult = dataPersistenceManager.loadAllDataFromCache();

        // THEN
        assertNotNull(listWeatherResult);
        assertEquals(1, listWeatherResult.size());
        assertEquals(weatherRequestStatus, listWeatherResult.get(0));
    }

    public void test_loadAllDataFromCache_with_two_requests_in_cache() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(WEATHER_ID, TEST_TEMP);
        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, CACHE_KEY);
        Weather weatherRequestStatus2 = buildWeather(WEATHER_ID2, TEST_TEMP2);
        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus2, CACHE_KEY2);

        // WHEN
        List<Weather> listWeatherResult = dataPersistenceManager.loadAllDataFromCache();

        // THEN
        assertNotNull(listWeatherResult);
        assertEquals(2, listWeatherResult.size());
        assertTrue(listWeatherResult.contains(weatherRequestStatus));
        assertTrue(listWeatherResult.contains(weatherRequestStatus2));
    }

    public void test_loadAllDataFromCache_with_no_requests_in_cache() throws Exception {
        // GIVEN

        // WHEN
        List<Weather> listWeather = dataPersistenceManager.loadAllDataFromCache();

        // THEN
        assertNotNull(listWeather);
        assertTrue(listWeather.isEmpty());
    }

    public void test_removeDataFromCache_when_two_requests_in_cache_and_one_removed() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(WEATHER_ID, TEST_TEMP);
        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, CACHE_KEY);
        Weather weatherRequestStatus2 = buildWeather(WEATHER_ID2, TEST_TEMP2);
        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus2, CACHE_KEY2);

        dataPersistenceManager.removeDataFromCache(CACHE_KEY2);

        // WHEN
        List<Weather> listWeatherResult = dataPersistenceManager.loadAllDataFromCache();

        // THEN
        assertNotNull(listWeatherResult);
        assertEquals(1, listWeatherResult.size());
        assertTrue(listWeatherResult.contains(weatherRequestStatus));
        assertFalse(listWeatherResult.contains(weatherRequestStatus2));
    }

    public void test_cacheKey_can_be_string_when_object_type_has_int_id() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(WEATHER_ID, TEST_TEMP);
        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, CACHE_KEY3_STRING);

        // WHEN
        Weather weatherReturned = dataPersistenceManager.loadDataFromCache(CACHE_KEY3_STRING, DurationInMillis.ALWAYS_RETURNED);
        List<Weather> listWeatherResult = dataPersistenceManager.loadAllDataFromCache();

        // THEN
        assertNotNull(listWeatherResult);
        assertEquals(1, listWeatherResult.size());
        assertTrue(listWeatherResult.contains(weatherRequestStatus));
        assertEquals(WEATHER_ID, weatherReturned.getId());
    }


    public void test_notification() throws Exception {
        // GIVEN
        Weather weatherRequestStatus = buildWeather(WEATHER_ID, TEST_TEMP);
        dataPersistenceManager.saveDataToCacheAndReturnData(weatherRequestStatus, CACHE_KEY3_STRING);

        // THAN
        assertTrue(mResolver.getNotificationUris().contains(NOTIFICATION_URI1));
        assertTrue(mResolver.getNotificationUris().contains(NOTIFICATION_URI2));
    }

    private Weather buildWeather(int id, CurrenWeather currenWeather) {
        Weather weather = new Weather();
        weather.setId(id);
        List<CurrenWeather> currents = new ArrayList<CurrenWeather>();
        currents.add(currenWeather);
        weather.setListWeather(currents);
        weather.setListForecast(null);
        return weather;
    }


    private static class NotificationContentResolver extends MockContentResolver {

        private ArrayList<Uri> mNotifcationUris = new ArrayList<Uri>();
        @Override
        public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
            mNotifcationUris.add(uri);
        }

        public List<Uri> getNotificationUris() {
            return mNotifcationUris;
        }
    }

    private static class NotificationApplication extends MockApplication {

        private Context mContext;
        private NotificationContentResolver mContentResolver;


        public NotificationApplication(Context context, NotificationContentResolver contentResolver) {
            super();
            mContext = context;
            mContentResolver = contentResolver;

        }

        @Override
        public ContentResolver getContentResolver() {
            return mContentResolver;
        }

        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
            return mContext.openOrCreateDatabase(name, mode, factory);
        }

        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
            return mContext.openOrCreateDatabase(name, mode, factory);
        }
    }
}
