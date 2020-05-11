package com.stv.commonservice.remoterupdate.proxy;

import android.content.Context;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by zhaoyiming on 19-3-16.
 */

public class OldControlManagerProxy {
    public static String getProperty(String str, Context context) {
        String ret = "";
        try {
            ClassLoader cl = context.getClassLoader();
            Class LeEcoRemoteControlManager = cl.loadClass("letv.remotemanager.RemoteManager");
            Constructor[] constructors = LeEcoRemoteControlManager.getDeclaredConstructors();
            AccessibleObject.setAccessible(constructors, true);
            for (Constructor con : constructors) {
                if (con.isAccessible()) {
                    Object classObject = con.newInstance(context);
                    Method method = LeEcoRemoteControlManager.getMethod("getProperty", String.class);
                    ret = (String) method.invoke(classObject, str);
                }
            }

        } catch (IllegalArgumentException iAE) {
            iAE.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean isValidRemoteBin(String path, Context context, Boolean def) {
        Boolean ret = def;
        try {
            ClassLoader cl = context.getClassLoader();
            Class LeEcoRemoteControlManager = cl.loadClass("letv.remotemanager.RemoteManager");
            Constructor[] constructors = LeEcoRemoteControlManager.getDeclaredConstructors();
            AccessibleObject.setAccessible(constructors, true);
            for (Constructor con : constructors) {
                if (con.isAccessible()) {
                    Object classObject = con.newInstance(context);
                    Method method = LeEcoRemoteControlManager.getMethod("isValidRemoteBin", String.class);
                    ret = (Boolean) method.invoke(classObject, path);
                }
            }

        } catch (IllegalArgumentException iAE) {
            iAE.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void startUpgrade(String str, Context context) {
        try {
            ClassLoader cl = context.getClassLoader();
            Class LeEcoRemoteControlManager = cl.loadClass("letv.remotemanager.RemoteManager");
            Constructor[] constructors = LeEcoRemoteControlManager.getDeclaredConstructors();
            AccessibleObject.setAccessible(constructors, true);
            for (Constructor con : constructors) {
                if (con.isAccessible()) {
                    Object classObject = con.newInstance(context);
                    Method method = LeEcoRemoteControlManager.getMethod("startUpgrade", String.class);
                    method.invoke(classObject, str);
                }
            }

        } catch (IllegalArgumentException iAE) {
            iAE.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
