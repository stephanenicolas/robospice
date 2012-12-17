package com.octo.android.robospice.persistence;

import java.util.List;

import android.app.Application;

/**
 * Super class of all factories of {@link ObjectPersisterFactory} classes. They are responsible for creating
 * {@link ObjectPersister} classes that will save/load data for a given class.
 * 
 * Such factories are useful because we sometimes need to create {@link ObjectPersister} dynamically. For instance when
 * we serialize items as JSON, we need to create a new {@link ObjectPersister} class for every class of items
 * saved/loaded into cache. A unique {@link ObjectPersister} would not be able to strongly type the load/save method and
 * we would have to cast the result of both operations, leading to less robust and less convenient code.
 * 
 * @author sni
 * 
 */
public abstract class ObjectPersisterFactory implements Persister {

    private Application mApplication;
    private boolean isAsyncSaveEnabled;
    private List< Class< ? >> listHandledClasses;

    /**
     * 
     * @param application
     *            the android context needed to access android file system or databases to store.
     */
    public ObjectPersisterFactory( Application application ) {
        this.mApplication = application;
    }

    public ObjectPersisterFactory( Application application, List< Class< ? >> listHandledClasses ) {
        this.mApplication = application;
        this.listHandledClasses = listHandledClasses;
    }

    protected final Application getApplication() {
        return mApplication;
    }

    /**
     * Wether or not this bus element can persist/unpersist objects of the given class clazz.
     * 
     * @param clazz
     *            the class of objets we are looking forward to persist.
     * @return true if this bus element can persist/unpersist objects of the given class clazz. False otherwise.
     */
    @Override
    public boolean canHandleClass( Class< ? > clazz ) {
        if ( listHandledClasses == null ) {
            return true;
        } else {
            return listHandledClasses.contains( clazz );
        }
    }

    /**
     * Creates a {@link ObjectPersister} for a given class.
     * 
     * @param clazz
     *            the class of the items that need to be saved/loaded from cache.
     * @return a {@link ObjectPersister} able to load/save instances of class clazz.
     */
    public abstract < DATA > ObjectPersister< DATA > createObjectPersister( Class< DATA > clazz );

    public void setAsyncSaveEnabled( boolean isAsyncSaveEnabled ) {
        this.isAsyncSaveEnabled = isAsyncSaveEnabled;
    }

    public boolean isAsyncSaveEnabled() {
        return isAsyncSaveEnabled;
    }

    protected List< Class< ? >> getListHandledClasses() {
        return listHandledClasses;
    }
}