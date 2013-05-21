package com.octo.android.robospice.persistence.ormlite;

import java.util.List;

import android.app.Application;

public class InDatabaseObjectPersisterFactoryWithContentProvider extends InDatabaseObjectPersisterFactory {

    public InDatabaseObjectPersisterFactoryWithContentProvider(Application application, RoboSpiceDatabaseHelper databaseHelper, List<Class<?>> listHandledClasses) {
        super(application, databaseHelper, ContractHelper.getContractClasses(listHandledClasses));
    }

}
