
package com.stv.commonservice.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.stv.library.common.util.FileUtils;
import com.stv.library.common.util.ServiceHelper;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;

public class InstallUtils {
    private static LogUtils sLog = LogUtils.getInstance(InstallUtils.class.getSimpleName());

    public static void quietInstall(Context context, String aPackageName) {
        String sk = getSecretKey(context);
        sLog.i("sk: " + sk);
        Intent intent = new Intent("android.intent.action.INSTALL_PACKAGE_QUIET");
        intent.putExtra("INSTALL_APP_NAME", "Text");
        intent.putExtra("INSTALL_PACKAGE_NAME", aPackageName);
        intent.putExtra("CLIENT_NAME", context.getPackageName());
        intent.putExtra("SECRET_KEY", getSecretKey(context));

        Uri uri;
        String path = StorageUtils.getApkDir() + aPackageName + Constants.Y_APK_SUFFIX;
        sLog.d("quietInstall() path: " + path);
        FileUtils.chmod(path, "777");
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider",
                    new File(path));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            uri = Uri.fromFile(new File(path));
            intent.setData(uri);
        }
        sLog.d("quietInstall() uri: " + uri);
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setComponent(new ComponentName("com.android.packageinstaller", "com.android.packageinstaller.PackageInstallerService"));
            ServiceHelper.startService(context, intent);
        } else {
            context.sendBroadcast(intent);
        }
    }

    /**
     * 静默卸载
     *
     * @param content
     * @param packageName 应用包名
     */
    public static void quietUninstall(Context content, String packageName) {
        sLog.d("quietUninstall:  " + packageName);
        if (TextUtils.isEmpty(packageName))
            return;
        Intent intent = new Intent("android.intent.action.UNINSTALL_PACKAGE_QUIET");
        intent.putExtra("INSTALL_PACKAGE_NAME", packageName); // 需要卸载的app包名
        intent.putExtra("CLIENT_NAME", content.getPackageName()); // 调用者app包名
        intent.putExtra("SECRET_KEY", getSecretKey(content)); // 校验key，校验失败会拒绝安装
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setComponent(new ComponentName("com.android.packageinstaller",
                    "com.android.packageinstaller.PackageInstallerService"));
            ServiceHelper.startService(content, intent);
        } else {
            content.sendBroadcast(intent);
        }
    }

    /**
     * 判断手机是否安装某个应用
     *
     * @param context
     * @param appPackageName 应用包名
     * @return true：安装，false：未安装
     */
    public static boolean isApplicationAvilible(Context context, String appPackageName) {
        PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (appPackageName.equals(pn)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getSecretKey(Context context) {
        String key = getDevice(context, context.getPackageName(), null);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        StringBuilder str = new StringBuilder();
        str.append("leinstall&").append((String) DateFormat.format("yyyy:MM:dd", cal)).append("&")
                .append(context.getPackageName()).append("&").append(key).append("&end");

        return md5(str.toString());
    }

    public static String getDevice(final Context context, final String common, final String def) {
        String result = def;
        try {
            ClassLoader cl = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class LetvManager = cl.loadClass("com.android.letvmanager.LetvManager");

            @SuppressWarnings("unchecked")
            Method set = LetvManager.getMethod("getDevice", String.class);

            result = (String) set.invoke(LetvManager, common);
            if (TextUtils.isEmpty(result)) {
                result = def;
            }
        } catch (ClassNotFoundException e) {
            sLog.e("getDevice(): " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            sLog.e("getDevice(): " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
}
