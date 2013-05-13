package com.octo.android.robospice.persistence.ormlite;

import java.util.List;

import roboguice.util.temp.Ln;
import android.app.Application;

import com.tojc.ormlite.android.OrmLiteSimpleContentProvider;
import com.tojc.ormlite.android.framework.MimeTypeVnd.SubType;

public abstract class RoboSpiceContentProvider extends OrmLiteSimpleContentProvider<RoboSpiceDatabaseHelper> {
    @Override
    protected Class<RoboSpiceDatabaseHelper> getHelperClass() {
        return RoboSpiceDatabaseHelper.class;
    }

    @Override
    public RoboSpiceDatabaseHelper getHelper() {
        return new RoboSpiceDatabaseHelper((Application) getContext().getApplicationContext(), getDatabaseName(), getDatabaseVersion());
    }

    @Override
    public boolean onCreate() {
        Controller = new MatcherController();
        for (Class<?> clazz : getExposedClasses()) {
            try {
                Class<?> contractClazz;
                contractClazz = getContractClassForClass(clazz);
                int contentUriPatternMany = getContentUriPatternMany(contractClazz);
                int contentUriPatternOne = getContentUriPatternOne(contractClazz);
                Controller.add(clazz, SubType.Directory, "", contentUriPatternMany);
                Controller.add(clazz, SubType.Directory, "#", contentUriPatternOne);
            } catch (Exception e) {
                Ln.e(e);
            }
        }
        Controller.initialize();
        return true;
    }

    protected Class<?> getContractClassForClass(Class<?> clazz) throws ClassNotFoundException {
        String className = clazz.getName() + "Contract";
        return Class.forName(className);
    }

    protected int getContentUriPatternMany(Class<?> contractClass) throws IllegalAccessException, NoSuchFieldException {
        return contractClass.getField("CONTENT_URI_PATTERN_MANY").getInt(null);
    }

    protected int getContentUriPatternOne(Class<?> contractClass) throws IllegalAccessException, NoSuchFieldException {
        return contractClass.getField("CONTENT_URI_PATTERN_ONE").getInt(null);
    }

    public abstract List<Class<?>> getExposedClasses();

    public abstract String getDatabaseName();

    public abstract int getDatabaseVersion();

}
