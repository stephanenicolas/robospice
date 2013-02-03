package com.octo.android.robospice.persistence.ormlite;

import java.sql.SQLException;
import java.util.List;

import roboguice.util.temp.Ln;
import android.app.Application;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import com.octo.android.robospice.persistence.ObjectPersister;
import com.octo.android.robospice.persistence.ObjectPersisterFactory;

public class InDatabaseObjectPersisterFactory extends ObjectPersisterFactory {

    private RoboSpiceDatabaseHelper databaseHelper;
    private boolean isAllTableCreated = false;

    public InDatabaseObjectPersisterFactory(Application application,
        RoboSpiceDatabaseHelper databaseHelper,
        List<Class<?>> listHandledClasses) {
        super(application, listHandledClasses);
        this.databaseHelper = databaseHelper;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <DATA> ObjectPersister<DATA> createObjectPersister(Class<DATA> clazz) {
        initializeTablesIfNeeded();

        Class<?> idType;
        try {
            idType = getIdType(clazz);
        } catch (SQLException e) {
            throw new RuntimeException(
                "Impossible to determine the type of the ID used in class "
                    + clazz.getName(), e);
        }
        return new InDatabaseObjectPersister(getApplication(), databaseHelper,
            clazz, idType);
    }

    private void createTableIfNotExists(Class<?> clazz) {
        try {
            TableUtils.createTableIfNotExists(
                databaseHelper.getConnectionSource(), clazz);
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

    private <DATA> Class<?> getIdType(Class<DATA> clazz) throws SQLException {
        DatabaseTableConfig<DATA> childDatabaseTableConfig = DatabaseTableConfig
            .fromClass(databaseHelper.getConnectionSource(), clazz);
        for (FieldType childFieldType : childDatabaseTableConfig
            .getFieldTypes(null)) {
            if (childFieldType.isId()) {
                if (childFieldType.getType().equals(int.class)) {
                    return Integer.class;
                }
                if (childFieldType.getType().equals(long.class)) {
                    return Long.class;
                }
                return childFieldType.getType();
            }
        }
        throw new RuntimeException(
            "Impossible to determine the type of the ID used in class "
                + clazz.getName());
    }

}
