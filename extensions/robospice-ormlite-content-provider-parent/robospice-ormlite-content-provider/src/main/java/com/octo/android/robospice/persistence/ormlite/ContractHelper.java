package com.octo.android.robospice.persistence.ormlite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.util.temp.Ln;
import android.net.Uri;

import com.tojc.ormlite.android.annotation.AdditionalAnnotation.Contract;

/**
 * Helper to get contract class and relevent fields.
 * @author SNI
 */
public final class ContractHelper {
    private ContractHelper() {
        // utility class private constructor
    }

    public static Map<Class<?>, Uri> getContractClasses(List<Class<?>> classList) {
        Map<Class<?>, Uri> mapHandledClassesToNotificationUri = new HashMap<Class<?>, Uri>();
        for (Class<?> clazz : classList) {
            Uri uri = null;
            try {
                Class<?> contractClass = getContractClassForClass(clazz);
                uri = getContentUri(contractClass);
            } catch (Exception e) {
                Ln.v("Contract class not found for " + clazz.getName());
            }
            mapHandledClassesToNotificationUri.put(clazz, uri);
        }
        return mapHandledClassesToNotificationUri;
    }

    public static Class<?> getContractClassForClass(Class<?> clazz) throws ClassNotFoundException {
        String className;
        if (clazz.isAnnotationPresent(Contract.class)) {
            className = clazz.getAnnotation(Contract.class).contractClassName();
            if (className == null || className.isEmpty()) {
                className = clazz.getName() + "Contract";
            }
            return Class.forName(className);
        } else {
            return null;
        }
    }

    public static Uri getContentUri(Class<?> contractClass) throws IllegalAccessException, NoSuchFieldException {
        return (Uri) contractClass.getField("CONTENT_URI").get(null);
    }

    public static int getContentUriPatternMany(Class<?> contractClass) throws IllegalAccessException, NoSuchFieldException {
        return contractClass.getField("CONTENT_URI_PATTERN_MANY").getInt(null);
    }

    public static int getContentUriPatternOne(Class<?> contractClass) throws IllegalAccessException, NoSuchFieldException {
        return contractClass.getField("CONTENT_URI_PATTERN_ONE").getInt(null);
    }
}
