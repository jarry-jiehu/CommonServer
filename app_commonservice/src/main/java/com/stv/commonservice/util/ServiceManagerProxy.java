package com.stv.commonservice.util;

import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.Method;

/**
 * Created by jiehu on 18-5-25.
 */

public class ServiceManagerProxy {
    /**
     * 根据给定Key获取值.
     *
     * @return 如果不存在该key则返回空字符串
     * @throws IllegalArgumentException 如果key超过32个字符则抛出该异常
     */
    public static void AddService(Context context, String name, IBinder service) {
        try {
            ClassLoader cl = context.getClassLoader();
            Class SystemManager = cl.loadClass("android.os.ServiceManager");

            Method addService = SystemManager.getMethod("addService", String.class, IBinder.class);

            addService.invoke(SystemManager, name, service);
        } catch (IllegalArgumentException iAE) {
        } catch (Exception e) {
        }
    }
}
