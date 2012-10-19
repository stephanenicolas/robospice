package com.octo.android.robospice.persistence.ormlite;

import android.app.Application;

import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.ObjectPersisterFactory;

public class InDatabaseObjectPersisterFactory extends ObjectPersisterFactory {

    private RoboSpiceDatabaseHelper databaseHelper;

    public InDatabaseObjectPersisterFactory( Application application, RoboSpiceDatabaseHelper databaseHelper ) {
        super( application );
        this.databaseHelper = databaseHelper;
    }

    @Override
    public boolean canHandleClass( Class< ? > clazz ) {
        return databaseHelper.getClassCollection().contains( clazz );
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public < DATA > ObjectPersister< DATA > createObjectPersister( Class< DATA > clazz ) {
        return new InDatabaseObjectPersister( getApplication(), databaseHelper, clazz );
    }

}
