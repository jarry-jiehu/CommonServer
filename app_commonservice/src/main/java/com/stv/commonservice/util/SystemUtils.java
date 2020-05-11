
package com.stv.commonservice.util;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemClock;
import android.os.UserManager;
import android.text.TextUtils;

import java.util.Locale;

import eui.tv.TvManager;

public class SystemUtils {
    private static LogUtils sLog = LogUtils.getInstance("Common", SystemUtils.class.getSimpleName());

    /**
     * 网络是否连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            if (context != null) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mNetworkInfo = connectivityManager
                        .getActiveNetworkInfo();
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable();
                }
            }
        } catch (Exception e) {
            sLog.e("Unable to reflect method: isNetworkAvailable,e:" + e.getMessage());
        }
        return false;
    }

    /**
     * 是否在前台运行
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isRunningForeground(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String currentPackageName = cn.getPackageName();
        if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(packageName)) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是cibn电视
     *
     * @return
     */
    public static boolean isCibn() {
        String uiType = StvHideApi.getStvUiType();
        if (null != uiType
                && !"".equals(uiType)
                && uiType.toUpperCase(Locale.getDefault()).contains(
                "CIBN".toUpperCase(Locale.getDefault()))) {
            sLog.i("isCibn : " + true);
            return true;
        }
        sLog.i("isCibn : " + false);
        return false;
    }

    public static boolean isChangeUiType(Context context, DataPref pref) {
        String lastUitype = pref.getKeyUiType();
        String uiType = StvHideApi.getStvUiType();
        if (null != uiType && uiType.equals(lastUitype)) {
            return false;
        } else {
            pref.setKeyUiType(uiType);
            return true;
        }
    }

    /**
     * 判断是否是稳定版本
     */
    public static boolean isStableBuild() {
        String releaseVersion = StvHideApi.getLetvSwVersion();
        sLog.i("releaseVersion is " + releaseVersion);
        if (releaseVersion != null && releaseVersion.endsWith("S")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param context 设置定时器
     */
    public static void setAttestAmler(Context context, int requestCode, long triggerAtMillis, String action) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + triggerAtMillis,
                pendingIntent);
        sLog.i("setAttestAmler after " + triggerAtMillis + " Request ");
    }

    /**
     * 取消定时器
     *
     * @param context
     */
    public static void cancelAttestAmler(Context context, int requestCode, String action) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pendingIntent);
        sLog.i("attest Amler cancel ");
    }

    public static boolean isOldPlatform(Context context) {
        boolean isOldPlatform = false;
        int platform = TvManager.getPlatform();
        if (Constants.PLATFORM_6A801 == platform ||
                Constants.PLATFORM_6A918 == platform ||
                Constants.PLATFORM_6A928 == platform ||
                Constants.PLATFORM_APQ8064 == platform) {
            isOldPlatform = true;
        } else {
            isOldPlatform = false;
        }
        return isOldPlatform;
    }

    /**
     * 当前系统是否已解锁(针对848机型)
     * @param context
     * @return
     */
    public static boolean isUserUnlocked(Context context) {
        boolean isUserUnlocked = true;
        if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isUserUnlocked = context.getSystemService(UserManager.class).isUserUnlocked();
        }
        sLog.w("isUserUnlocked: " + isUserUnlocked);
        return isUserUnlocked;
    }

}
