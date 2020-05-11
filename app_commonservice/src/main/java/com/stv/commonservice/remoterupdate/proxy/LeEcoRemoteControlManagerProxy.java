package com.stv.commonservice.remoterupdate.proxy;

import android.content.Context;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by zhaoyiming on 18-9-11.
 */

public class LeEcoRemoteControlManagerProxy {


    public static String getProperty(String str, Context context) {
        String ret = "";
        try {
            ClassLoader cl = context.getClassLoader();
            Class LeEcoRemoteControlManager = cl.loadClass("letv.remotemanager.LeEcoRemoteControlManager");
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

    public static String getVersionAfterUpgrade(String name, boolean upgradeResult, Context context) {
        String ret = "";
        try {
            ClassLoader cl = context.getClassLoader();
            Class LeEcoRemoteControlManager = cl.loadClass("letv.remotemanager.LeEcoRemoteControlManager");
            Constructor[] constructors = LeEcoRemoteControlManager.getDeclaredConstructors();
            AccessibleObject.setAccessible(constructors, true);
            for (Constructor con : constructors) {
                if (con.isAccessible()) {
                    Object classObject = con.newInstance(context);
                    Method method = LeEcoRemoteControlManager.getMethod("getVersionAfterUpgrade", String.class, boolean.class, Context.class);
                    ret = (String) method.invoke(classObject, name, upgradeResult, context);
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
            Class LeEcoRemoteControlManager = cl.loadClass("letv.remotemanager.LeEcoRemoteControlManager");
            Constructor[] constructors = LeEcoRemoteControlManager.getDeclaredConstructors();
            AccessibleObject.setAccessible(constructors, true);
            for (Constructor con : constructors) {
                if (con.isAccessible()) {
                    Object classObject = con.newInstance(context);
                    Method method = LeEcoRemoteControlManager.getMethod("isValidRemoteBin",String.class);
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
            Class LeEcoRemoteControlManager = cl.loadClass("letv.remotemanager.LeEcoRemoteControlManager");
            Constructor[] constructors = LeEcoRemoteControlManager.getDeclaredConstructors();
            AccessibleObject.setAccessible(constructors, true);
            for (Constructor con : constructors) {
                if (con.isAccessible()) {
                    Object classObject = con.newInstance(context);
                    Method method = LeEcoRemoteControlManager.getMethod("startUpgrade",String.class);
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
