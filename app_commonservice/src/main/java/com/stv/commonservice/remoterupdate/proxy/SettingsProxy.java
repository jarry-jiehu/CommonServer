package com.stv.commonservice.remoterupdate.proxy;


import java.lang.reflect.Field;

/**
 * Created by zhaoyiming on 18-9-11.
 */

public class SettingsProxy {
    public static String getControlerVersion() {
        String ret = "";
        try {
            Class System = Class.forName("");
            Object obj = System.newInstance();
            Field field = System.getDeclaredField("INTELLIGENT_REMOTE_CONTROLER_VERSION");
            field.setAccessible(true);
            ret = (String) field.get(obj);
        } catch (IllegalArgumentException iAE) {
            iAE.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String getControlerIIVersion() {
        String ret = "";
        try {
            Class System = Class.forName("android.provider.Settings$System");
            Object obj = System.newInstance();
            Field field = System.getDeclaredField("INTELLIGENT_REMOTE_CONTROLER_II_VERSION");
            field.setAccessible(true);
            ret = (String) field.get(obj);
        } catch (IllegalArgumentException iAE) {
            iAE.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String getDeviceGuided() {
        String ret = "";
        try {
            Class System = Class.forName("android.provider.Settings$Secure");
            Object obj = System.newInstance();
            Field field = System.getDeclaredField("DEVICE_GUIDEd");
            field.setAccessible(true);
            ret = (String) field.get(obj);
        } catch (IllegalArgumentException iAE) {
            iAE.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
