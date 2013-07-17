package com.octo.android.robospice.persistence.ormlite;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roboguice.util.temp.Ln;
import android.app.Application;
import android.net.Uri;

import com.j256.ormlite.table.TableUtils;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.ObjectPersisterFactory;

public class InDatabaseObjectPersisterFactory extends ObjectPersisterFactory {

    private RoboSpiceDatabaseHelper databaseHelper;
    private boolean isAllTableCreated = false;
    private Map<Class<?>, Uri> mapHandledClassesToNotificationUri;

    public InDatabaseObjectPersisterFactory(Application application, RoboSpiceDatabaseHelper databaseHelper, Map<Class<?>, Uri> mapHandledClassesToNotificationUri) {
        super(application, new ArrayList<Class<?>>(mapHandledClassesToNotificationUri.keySet()));
        this.databaseHelper = databaseHelper;
        this.mapHandledClassesToNotificationUri = mapHandledClassesToNotificationUri;
    }

    public InDatabaseObjectPersisterFactory(Application application, RoboSpiceDatabaseHelper databaseHelper, List<Class<?>> listHandledClasses) {
        super(application, listHandledClasses);
        this.databaseHelper = databaseHelper;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <DATA> ObjectPersister<DATA> createObjectPersister(Class<DATA> clazz) {
        initializeTablesIfNeeded();

        if (mapHandledClassesToNotificationUri != null && mapHandledClassesToNotificationUri.containsKey(clazz)) {
            Uri notificationUri = mapHandledClassesToNotificationUri.get(clazz);
            return new InDatabaseObjectPersister(getApplication(), databaseHelper, clazz, notificationUri);
        } else {
            return new InDatabaseObjectPersister(getApplication(), databaseHelper, clazz);
        }
    }

    private void createTableIfNotExists(Class<?> clazz) {
        try {
            TableUtils.createTableIfNotExists(databaseHelper.getConnectionSource(), clazz);
        } catch (SQLException e) {
            Ln.e(e, "RoboSpice", "Could not create cache entry table");
        }
    }

    private void initializeTablesIfNeeded() {
        if (!isAllTableCreated) {
            createTableIfNotExists(CacheEntry.class);

            if (getListHandledClasses() != null) {
                for (Class<?> clazz : getListHandledClasses()) {
                    createTableIfNotExists(clazz);
                }
            }
            isAllTableCreated = true;
        }

    }

}
