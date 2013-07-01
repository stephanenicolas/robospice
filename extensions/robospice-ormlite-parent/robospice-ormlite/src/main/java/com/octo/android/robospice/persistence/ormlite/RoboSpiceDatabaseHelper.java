package com.octo.android.robospice.persistence.ormlite;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import roboguice.util.temp.Ln;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Helper class which creates/updates our database and provides the DAOs.
 */
public class RoboSpiceDatabaseHelper extends OrmLiteSqliteOpenHelper {

    public static String getCacheKeyForId(String id, Class<?> clazz) {
        return clazz.getSimpleName() + "_" + id.toString();
    }

    public static String getIdForCacheKey(String cacheKey) {
        return cacheKey.substring(cacheKey.indexOf('_') + 1);
    }

    public RoboSpiceDatabaseHelper(Application application, String databaseName, int databaseVersion) {
        super(application, databaseName, null, databaseVersion);
    }

    public RoboSpiceDatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, databaseName, factory, databaseVersion);
    }

    public RoboSpiceDatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion, int configFileId) {
        super(context, databaseName, factory, databaseVersion, configFileId);
    }

    public RoboSpiceDatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion, File configFile) {
        super(context, databaseName, factory, databaseVersion, configFile);
    }

    public RoboSpiceDatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion, InputStream stream) {
        super(context, databaseName, factory, databaseVersion, stream);
    }

    public <T> void updateObjectInDatabase(T modelObject, Class<T> modelObjectClass) throws SQLException {
        if (modelObject != null) {
            Dao<T, ?> dao = getDao(modelObjectClass);
            dao.update(modelObject);
        }
    }

    public <T> void refreshFromDatabase(T modelObject, Class<T> modelObjectClass) throws SQLException {
        if (modelObject != null) {
            Dao<T, ?> dao = getDao(modelObjectClass);
            dao.refresh(modelObject);
        }
    }

    public <T> void createOrUpdateInDatabase(T modelObject, Class<T> modelObjectClass) throws SQLException {
        if (modelObject != null) {
            Dao<T, ?> dao = getDao(modelObjectClass);
            dao.createOrUpdate(modelObject);
        }
    }

    public void createOrUpdateCacheEntryInDatabase(CacheEntry cacheEntry) throws SQLException {
        if (cacheEntry != null) {
            Dao<CacheEntry, ?> dao = getDao(CacheEntry.class);
            dao.createOrUpdate(cacheEntry);
        }
    }

    public <T> List<T> queryForAllFromDatabase(Class<T> modelObjectClass) throws SQLException {
        List<T> allObjectsInTable = new ArrayList<T>();
        Dao<T, ?> dao = getDao(modelObjectClass);
        allObjectsInTable.addAll(dao.queryForAll());
        return allObjectsInTable;
    }

    public <T, ID> T queryForIdFromDatabase(ID id, Class<T> modelObjectClass) throws SQLException {
        Dao<T, ID> dao = getDao(modelObjectClass);
        T result = dao.queryForId(id);

        return result;
    }

    public CacheEntry queryCacheKeyForIdFromDatabase(String id) throws SQLException {
        Dao<CacheEntry, String> dao = getDao(CacheEntry.class);
        return dao.queryForId(id);
    }

    public <T, ID> void deleteByIdFromDataBase(ID id, Class<T> modelObjectClass) throws SQLException {
        Dao<T, ID> dao = getDao(modelObjectClass);
        dao.deleteById(id);
    }

    public <T> void clearTableFromDataBase(Class<T> modelObjectClass) throws SQLException {
        try {
            clearTable(modelObjectClass);
        } catch (Exception ex) {
            Ln.d(ex, "An exception occurred when cleaning table");
        }
    }

    private <T> void clearTable(Class<T> clazz) throws SQLException {
        for (Field field : clazz.getDeclaredFields()) {
            ForeignCollectionField annotation = field.getAnnotation(ForeignCollectionField.class);
            if (annotation != null) {
                ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
                Class<?> itemInListClass = (Class<?>) stringListType.getActualTypeArguments()[0];
                clearTable(itemInListClass);
            }
        }
        TableUtils.dropTable(getConnectionSource(), clazz, true);
        TableUtils.createTableIfNotExists(getConnectionSource(), clazz);
    }

    public <T> void deleteFromDataBase(Collection<T> objectList, Class<T> modelObjectClass) throws SQLException {
        if (objectList != null && !objectList.isEmpty()) {
            Dao<T, ?> dao = getDao(modelObjectClass);
            dao.delete(objectList);
        }
    }

    public <T> void createInDatabaseIfNotExist(T modelObject, Class<T> modelObjectClass) throws SQLException {
        if (modelObject != null) {
            Dao<T, ?> dao = getDao(modelObjectClass);
            dao.createIfNotExists(modelObject);
        }
    }

    public <T> void deleteFromDataBase(T modelObject, Class<T> modelObjectClass) throws SQLException {
        if (modelObject != null) {
            Dao<T, ?> dao = getDao(modelObjectClass);
            dao.delete(modelObject);
        }
    }

    public <T> void deleteFromDataBase(ForeignCollection<T> modelObjectCollection, Class<T> modelObjectClass) throws SQLException {
        if (modelObjectCollection != null) {
            Dao<T, ?> dao = getDao(modelObjectClass);
            dao.delete(modelObjectCollection);
        }
    }

    public <T> long countOfAllObjectsOfTable(Class<T> modelObjectClass) throws SQLException {
        Dao<T, ?> dao = getDao(modelObjectClass);
        return dao.countOf();
    }

    public <T, FT> ForeignCollection<FT> getNewEmptyForeignCollection(String foreignKeyColumnName, Class<T> modelObjectClass, Class<FT> foreignObjectClass) throws SQLException {
        Dao<T, ?> dao = getDao(modelObjectClass);
        return dao.getEmptyForeignCollection(foreignKeyColumnName);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        // override if needed
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        // override if needed
    }
}
