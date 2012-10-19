package com.octo.android.robospice.persistence.ormlite;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import android.app.Application;
import android.util.Log;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.TableUtils;
import com.octo.android.robospice.exception.CacheLoadingException;
import com.octo.android.robospice.exception.CacheSavingException;
import com.octo.android.robospice.persistence.ObjectPersister;

public class InDatabaseObjectPersister< T > extends ObjectPersister< T > {

    public static final String TAG = "robospice-ormlite";

    private RoboSpiceDatabaseHelper databaseHelper;
    private Class< T > modelObjectType;
    private RuntimeExceptionDao dao;

    /**
     * @param application
     *            the android context needed to access android file system or databases to store.
     */
    public InDatabaseObjectPersister( Application application, RoboSpiceDatabaseHelper databaseHelper, Class< T > modelObjectType ) {
        super( application );
        this.modelObjectType = modelObjectType;
        this.databaseHelper = databaseHelper;
        try {
            TableUtils.createTableIfNotExists( databaseHelper.getConnectionSource(), modelObjectType );
        } catch ( SQLException e1 ) {
            Log.e( TAG, "SQL Error while creating table for " + modelObjectType, e1 );
        }

        try {
            dao = databaseHelper.getRuntimeExceptionDao( modelObjectType );
        } catch ( Throwable e ) {
            Log.e( TAG, "SQL Error", e );
        }
    }

    @Override
    public boolean canHandleClass( Class< ? > clazz ) {
        return modelObjectType.equals( clazz );
    }

    @Override
    public T loadDataFromCache( Object cacheKey, long maxTimeInCache ) throws CacheLoadingException {
        if ( !( cacheKey instanceof String ) ) {
            throw new IllegalArgumentException( "cacheKey must be a String" );
        }
        T result = null;

        try {
            CacheEntry cacheEntry = databaseHelper.queryCacheKeyForIdFromDatabase( (String) cacheKey );
            if ( cacheEntry == null ) {
                return null;
            }
            long timeInCache = System.currentTimeMillis() - cacheEntry.getTimestamp();
            if ( maxTimeInCache == 0 || timeInCache <= maxTimeInCache ) {
                result = databaseHelper.queryForIdFromDatabase( RoboSpiceDatabaseHelper.getIdForCacheKey( (String) cacheKey ), modelObjectType );
            }
        } catch ( SQLException e ) {
            Log.e( TAG, "SQL error", e );
        }
        return result;
    }

    @Override
    public T saveDataToCacheAndReturnData( final T data, final Object cacheKey ) throws CacheSavingException {
        if ( !( cacheKey instanceof String ) ) {
            throw new IllegalArgumentException( "cacheKey must be a String" );
        }
        try {
            dao.callBatchTasks( new Callable< Void >() {
                @Override
                public Void call() throws Exception {
                    dao.updateId( data, cacheKey );
                    databaseHelper.createOrUpdateInDatabase( data, modelObjectType );
                    saveAllForeignObjectsToCache( data );
                    CacheEntry cacheEntry = new CacheEntry( (String) cacheKey, System.currentTimeMillis() );
                    databaseHelper.createOrUpdateCacheEntryInDatabase( cacheEntry );
                    return null;
                }
            } );
        } catch ( Exception e ) {
            Log.e( TAG, "SQL Error", e );
        }
        return data;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private < E > void saveAllForeignObjectsToCache( E data ) throws SQLException, IllegalArgumentException, IllegalAccessException {
        // copier les childs en ram
        // mettre a null les child dans parents
        Map< Field, Collection< ? >> mapFieldToCollection = new HashMap< Field, Collection< ? >>();
        for ( Field field : data.getClass().getDeclaredFields() ) {
            if ( field.getAnnotation( ForeignCollectionField.class ) != null ) {
                field.setAccessible( true );
                Collection< ? > collectionCopy = new ArrayList( (Collection< ? >) field.get( data ) );
                field.set( data, null );
                mapFieldToCollection.put( field, collectionCopy );
            }
        }

        // sauver parents
        databaseHelper.createOrUpdateInDatabase( data, (Class< E >) data.getClass() );

        // recursif sur les childs dans la copy
        for ( Field field : mapFieldToCollection.keySet() ) {
            field.setAccessible( true );
            Collection< ? > collection = mapFieldToCollection.get( field );
            ForeignCollection foreignCollection = databaseHelper.getDao( data.getClass() ).getEmptyForeignCollection( field.getName() );
            // rebranche dans le parent des foreign collection avec le contenu de copy
            field.set( data, foreignCollection );
            for ( Object object : collection ) {
                saveAllForeignObjectsToCache( object );
                foreignCollection.add( object );
            }
        }
    }

    @Override
    public boolean removeDataFromCache( Object cacheKey ) {
        return false;
    }

    @Override
    public void removeAllDataFromCache() {
        try {
            databaseHelper.clearTableFromDataBase( modelObjectType );
        } catch ( SQLException e ) {
            Log.e( TAG, "SQL Error", e );
        }
    }
}
