package com.octo.android.robospice.persistence.ormlite;

import java.util.List;

import roboguice.util.temp.Ln;
import android.app.Application;

import com.tojc.ormlite.android.OrmLiteSimpleContentProvider;
import com.tojc.ormlite.android.annotation.AdditionalAnnotation.Contract;
import com.tojc.ormlite.android.framework.MatcherController;
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
        MatcherController controller = new MatcherController();
        for (Class<?> clazz : getExposedClasses()) {
            try {
                if (!clazz.isAnnotationPresent(Contract.class)) {
                    throw new Exception("Class " + clazz + " is not annotated with the @Contract annotation.");
                }
                Class<?> contractClazz = ContractHelper.getContractClassForClass(clazz);
                int contentUriPatternMany = ContractHelper.getContentUriPatternMany(contractClazz);
                int contentUriPatternOne = ContractHelper.getContentUriPatternOne(contractClazz);
                controller.add(clazz, SubType.DIRECTORY, "", contentUriPatternMany);
                controller.add(clazz, SubType.ITEM, "#", contentUriPatternOne);
            } catch (Exception e) {
                Ln.e(e);
            }
        }
        setMatcherController(controller);
        return true;
    }

    public abstract List<Class<?>> getExposedClasses();

    public abstract String getDatabaseName();

    public abstract int getDatabaseVersion();

}
