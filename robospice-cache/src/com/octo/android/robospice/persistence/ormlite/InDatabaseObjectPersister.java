package com.octo.android.robospice.persistence.ormlite;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import android.app.Application;
import android.util.Log;

import com.j256.ormlite.dao.LazyForeignCollection;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheLoadingException;
import com.octo.android.robospice.persistence.exception.CacheSavingException;

public class InDatabaseObjectPersister< T, ID > extends ObjectPersister< T > {

    public static final String TAG = "robospice-ormlite";

    private RoboSpiceDatabaseHelper databaseHelper;
    private Class< ID > idType;
    private RuntimeExceptionDao< T, ID > dao;

    /**
     * @param application
     *            the android context needed to access android file system or databases to store.
     */
    public InDatabaseObjectPersister( Application application, RoboSpiceDatabaseHelper databaseHelper, Class< T > modelObjectType, Class< ID > idType ) {
        super( application, modelObjectType );
        this.databaseHelper = databaseHelper;
        this.idType = idType;
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
            Object id = cacheEntry.getResultId();
            long timeInCache = System.currentTimeMillis() - cacheEntry.getTimestamp();
            if ( maxTimeInCache == 0 || timeInCache <= maxTimeInCache ) {
                result = databaseHelper.queryForIdFromDatabase( id, getHandledClass() );
            }
        } catch ( SQLException e ) {
            Log.e( TAG, "SQL error", e );
        }
        return result;
    }

    @Override
    public T saveDataToCacheAndReturnData( final T data, final Object cacheKey ) throws CacheSavingException {
        if ( !this.idType.equals( cacheKey.getClass() ) ) {
            throw new IllegalArgumentException( "cacheKey must be a " + idType.getSimpleName() );
        }
        try {
            dao.callBatchTasks( new Callable< Void >() {
                @Override
                public Void call() throws Exception {
                    databaseHelper.createOrUpdateInDatabase( data, getHandledClass() );
                    saveAllForeignObjectsToCache( data );
                    Object id = null;
                    @SuppressWarnings("unchecked")
                    DatabaseTableConfig< T > childDatabaseTableConfig = (DatabaseTableConfig< T >) DatabaseTableConfig.fromClass(
                            databaseHelper.getConnectionSource(), data.getClass() );
                    for ( FieldType childFieldType : childDatabaseTableConfig.getFieldTypes( null ) ) {
                        if ( childFieldType.isId() ) {
                            id = childFieldType.extractJavaFieldValue( data );
                        }
                    }
                    CacheEntry cacheEntry = new CacheEntry( (String) cacheKey, System.currentTimeMillis(), id );
                    databaseHelper.createOrUpdateCacheEntryInDatabase( cacheEntry );
                    return null;
                }
            } );

            databaseHelper.refreshFromDatabase( data, getHandledClass() );
            return data;
        } catch ( Exception e ) {
            Log.e( TAG, "SQL Error", e );
            return null;
        }
    }

    /**
     * During this operation, we must save a new POJO (parent) into the database. The problem is that is the POJO
     * contains children POJOs, then saving the parent would not work as the parent must exist in the database prior to
     * saving the children. SO :
     * <ul>
     * <li>we copy the children into memory,
     * <li>put the children to null on parent
     * <li>save the parent to the database
     * <li>if saving filled the parent with previous children, we remove them
     * <li>re inject the children into the parent
     * <li>save the children (as the parent now exists in database).
     * </ul>
     * 
     * @param data
     *            the parent POJO to save in the database.
     * @throws SQLException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected < E > void saveAllForeignObjectsToCache( E data ) throws SQLException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        // copy children on memory
        // set children to null on parents so that we can save the parent without cascade
        Map< Field, Collection< ? >> mapFieldToCollection = new HashMap< Field, Collection< ? >>();
        Class< ? extends Object > parentClass = data.getClass();
        for ( Field field : parentClass.getDeclaredFields() ) {
            ForeignCollectionField annotation = field.getAnnotation( ForeignCollectionField.class );
            if ( annotation != null ) {
                Collection< ? > collectionCopy = new ArrayList();
                Method getMethod = DatabaseFieldConfig.findGetMethod( field, true );
                Collection collectionInObject = (Collection< ? >) getMethod.invoke( data );
                if ( collectionInObject != null ) {
                    collectionCopy.addAll( collectionInObject );
                }
                Method setMethod = DatabaseFieldConfig.findSetMethod( field, true );
                setMethod.invoke( data, (Object) null );
                mapFieldToCollection.put( field, collectionCopy );
            }
        }

        // save parents without cascade
        databaseHelper.createOrUpdateInDatabase( data, (Class< E >) parentClass );

        // get the child from a previous database record
        databaseHelper.refreshFromDatabase( data, (Class< E >) parentClass );

        // future hook
        // delete children obtained from previous database record
        // we guess the type of children from the parametrized type of the collection field of the parent
        for ( Field field : parentClass.getDeclaredFields() ) {
            if ( field.getAnnotation( ForeignCollectionField.class ) != null ) {
                Method getMethod = DatabaseFieldConfig.findGetMethod( field, true );
                Collection collectionInObject = (Collection< ? >) getMethod.invoke( data );
                // lazy collection are not loaded from database, so we load them
                if ( collectionInObject instanceof LazyForeignCollection ) {
                    ( (LazyForeignCollection) collectionInObject ).refreshCollection();
                }
                ParameterizedType listType = (ParameterizedType) field.getGenericType();
                Class< ? > itemInListClass = (Class< ? >) listType.getActualTypeArguments()[ 0 ];
                databaseHelper.deleteFromDataBase( collectionInObject, itemInListClass );
            }
        }

        // recursive call on children
        // we now saved the parent and can fill the foreign key of the children
        for ( Field field : mapFieldToCollection.keySet() ) {
            Collection< ? > collection = mapFieldToCollection.get( field );
            // re-set complete children to the parent
            ConnectionSource connectionSource = databaseHelper.getConnectionSource();
            for ( Object object : collection ) {
                DatabaseTableConfig childDatabaseTableConfig = DatabaseTableConfig.fromClass( connectionSource, object.getClass() );
                for ( FieldType childFieldType : childDatabaseTableConfig.getFieldTypes( null ) ) {
                    if ( parentClass.equals( childFieldType.getType() ) && childFieldType.isForeign() ) {
                        childFieldType.assignField( object, data, true, null );
                    }
                }
                // save children recursively
                saveAllForeignObjectsToCache( object );

            }
        }

        // future hook

    }

    @Override
    public boolean removeDataFromCache( Object cacheKey ) {
        return false;
    }

    @Override
    public void removeAllDataFromCache() {
        try {
            databaseHelper.clearTableFromDataBase( getHandledClass() );
        } catch ( SQLException e ) {
            Log.e( TAG, "SQL Error", e );
        }
    }

    @Override
    public List< T > loadAllDataFromCache() throws CacheLoadingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List< Object > getAllCacheKeys() {
        // TODO Auto-generated method stub
        return null;
    }
}
