
package com.stv.commonservice.util;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.stv.commonservice.remoterupdate.proxy.LeEcoRemoteControlManagerProxy;
import com.stv.commonservice.remoterupdate.proxy.OldControlManagerProxy;

import java.lang.reflect.Method;
import java.util.Locale;

import eui.tv.ReportManager;
import eui.tv.TvManager;

public class StvHideApi {
    private static LogUtils sLog = LogUtils.getInstance("Common", StvHideApi.class.getSimpleName());
    public static final String TV_SYSTEM_COMMONSERVICE = "tv.system.commonservice";

    public static String getDomain(Context context, String name) {
        String domain = TvManager.getDomain(context, name);
        sLog.d("getDomain() name: " + name + ", domain: " + domain);
        if (null == domain)
            return "";
        else
            return domain;
    }

    public static String getID() {
        String deviceId = TvManager.getDeviceId();
        if (deviceId == null) {
            StringBuffer buffer = new StringBuffer();
            String model = TvManager.getModel();
            if (model != null && model.length() > 0) {
                buffer.append(model);
            }
            buffer.append("-");
            String mac = TvManager.getEthMac();
            if (mac != null && mac.length() > 0) {
                buffer.append(mac.toLowerCase(Locale.getDefault()));
            }
            buffer.append("-");
            deviceId = buffer.toString();
        }

        return deviceId;
    }

    public static String getGeneralParam() {
        return TvManager.getGeneralParam();
    }

    public static String getLetvMac() {
        return TvManager.getEthMac();
    }

    public static String getLetvUiVersion() {
        return TvManager.getUiVersion();
    }

    public static String getLetvModel() {
        return TvManager.getModel();
    }

    public static String getStvUiType() {
        return TvManager.getUiType();
    }

    public static String getLetvUA(Context context) {
        return TvManager.getUA(context);
    }

    public static String getLetvSwVersion() {
        return TvManager.getSwVersion();
    }

    // public static void onReportLog(Context context, String msgType, String postMsg) {
    // try {
    // sLog.i("1-1-1--1-1-1-8-8-8");
    // // 获取Class
    // Class<?> cls = Class.forName("com.android.letvmanager.LetvManager");
    // // 设定构造函数的参数类型
    // Class<?>[] parTypes = new Class<?>[1];
    // parTypes[0] = Context.class;
    // sLog.i("1-1-1--1-1-1-6-6-6");
    // // 获取构造器
    // Constructor<?> con = cls.getConstructor(parTypes);
    // // 初始化构造参数
    // Object[] pars = new Object[1];
    // pars[0] = context;
    // Object mathobj = con.newInstance(pars);
    // sLog.i("1-1-1--1-1-1-5-5");
    // // 调用无参数对象方法add1
    // Method method = cls.getMethod("isRunReportLog");// 指定调用的方法
    // boolean isRun = (boolean) method.invoke(mathobj);// 调用mathobj1
    // sLog.i("1-1-1--1-1-1" + isRun);
    // if (!isRun) {
    // sLog.i("1-1-1--1-1-1");
    // Method startReportLog = cls.getMethod("startReportLog");
    // startReportLog.invoke(mathobj);
    // sLog.i("2-2-2-2-2-2");
    // }
    //
    // Method onReportLog = cls.getMethod("onReportLog", String.class, String.class);
    // sLog.i("2-2-2-2-2-2=2=2=2=2");
    // onReportLog.invoke(mathobj, msgType, postMsg);
    // sLog.i("3-3-3-3-3-3");
    // } catch (Exception e) {
    // sLog.i("onReportLog is Exception ." + e.getMessage());
    // } catch (Error e) {
    // sLog.i("onReportLog is Error ." + e.getMessage());
    // }
    // }

    public static void onReportLog(Context context, String msgType, String postMsg) {
        ReportManager.getInstance(context).reportLog(msgType, null, postMsg);
    }

    public static String getStvDeviceKey(Context context) {
        return TvManager.getDeviceKey(context);
    }

    /*************** SystemProperties start *************************/

    public static void setSystemProperties(Context context, String key, String value) {
        SystemPropertiesProxy.set(context, key, value);
    }

    public static String getSystemProperties(Context context, String key) {
        return SystemPropertiesProxy.get(context, key);
    }

    public static int getSystemPropertiesInt(Context context, String key, int defaultValue) {
        return SystemPropertiesProxy.getInt(context, key, defaultValue);
    }

    /*************** SystemProperties start *************************/

    /*************** RemoteManager start ********************/
    public static String getProperty(String str, Context context) {
        if (SystemUtils.isOldPlatform(context)) {
            return OldControlManagerProxy.getProperty(str, context);
        }
        return LeEcoRemoteControlManagerProxy.getProperty(str, context);
    }

    public static String getVersionAfterUpgrade(String name, boolean upgradeResult, Context context) {
        return LeEcoRemoteControlManagerProxy.getVersionAfterUpgrade(name, upgradeResult, context);
    }

    public static boolean isValidRemoteBin(String path, Context context) {
        if (SystemUtils.isOldPlatform(context)) {
            return OldControlManagerProxy.isValidRemoteBin(path, context, false);
        }
        return LeEcoRemoteControlManagerProxy.isValidRemoteBin(path, context, false);
    }

    public static void startUpgrade(String str, Context context) {
        if (SystemUtils.isOldPlatform(context)) {
            OldControlManagerProxy.startUpgrade(str, context);
            return;
        }
        LeEcoRemoteControlManagerProxy.startUpgrade(str, context);
    }

    /*************** RemoteManager end ********************/

}
