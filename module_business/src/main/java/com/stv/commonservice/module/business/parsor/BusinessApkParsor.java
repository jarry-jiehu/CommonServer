
package com.stv.commonservice.module.business.parsor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;

import com.stv.commonservice.library.base.BaseHelper;
import com.stv.commonservice.module.business.callback.BusinessListener;
import com.stv.library.common.util.AppInfo;
import com.stv.library.common.util.FileUtils;
import com.stv.library.common.util.LogUtils;
import com.stv.library.common.util.ThreadUtils;
import com.stv.library.letv.tv.InstallUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class BusinessApkParsor {
    private static final String TAG = "BusinessApkParsor";

    private static int index = 0;

    private static volatile boolean running = false;
    private static volatile String runningFlag;
    private static final long TIME_OUT = 3 * 60 * 1000L;

    private static final String FILE_DIR_APP_APK = BaseHelper.getContext()
            .getExternalFilesDir("apk").getAbsolutePath() + File.separator;

    private static final String APK_DIR_PATH = "/";
    private static final String APK_PKGS = "/uninstallApp.txt";

    public static final String ACTION_INSTALL_SUCCESS = "com.android.packageinstaller.action.APP_INSTALL_SUCCESS";
    public static final String ACTION_INSTALL_FAILED = "com.android.packageinstaller.action.APP_INSTALL_FAILED";
    private static final String EXTRA_INSTALL_ERROR_RESON = "storage_error_reason";

    public static final String ACTION_UNINSTALL_SUCCESS = "com.android.packageinstaller.action.APP_UNINSTALL_SUCCESS";
    public static final String ACTION_UNINSTALL_FAILED = "com.android.packageinstaller.action.APP_UNINSTALL_FAILED";
    private static final String EXTRA_UNINSTALL_ERROR_RESON = "error_reason";

    public static final String EXTRA_PACKAGE = "com.android.packageinstaller.action.package";
    public static final String EXTRA_NAME = "com.android.packageinstaller.action.appname";

    public static void installApks(String path, BusinessListener listener) {
        File dir = new File(path + APK_DIR_PATH);
        String[] apkNames = null;
        if (dir.exists() && dir.isDirectory()) {
            apkNames = dir.list();
        }
        if (null == apkNames || 0 == apkNames.length) {
            listener.onInstallApks(0, 0, true, null);
            return;
        }
        FileUtils.checkDirExists(FILE_DIR_APP_APK);
        ArrayList<String> list = new ArrayList<>();
        for (String name : apkNames) {
            if (TextUtils.isEmpty(name) || !name.endsWith(".apk") || name.startsWith(".")) {
                continue;
            }
            final String srcPath = path + APK_DIR_PATH + name;
            if (!FileUtils.isExists(srcPath)) {
                continue;
            }
            final String destPath = FILE_DIR_APP_APK + "/" + name;
            FileUtils.deleteDirOrFile(destPath);
            FileUtils.copyFile(srcPath, destPath);
            list.add(destPath);
        }
        if (list.size() == 0) {
            listener.onInstallApks(0, 0, true, null);
            return;
        }
        installApkSync(list, listener);
    }

    private static void installApkSync(ArrayList<String> list, BusinessListener listener) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (null == intent) {
                    return;
                }
                if (ACTION_INSTALL_SUCCESS.equals(intent.getAction())) {
                    LogUtils.i(TAG, "Install success"
                            + " name: " + intent.getStringExtra(EXTRA_NAME)
                            + " pkg: " + intent.getStringExtra(EXTRA_PACKAGE)
                            + " path: " + runningFlag);
                    listener.onInstallApks(list.size(), index, true, runningFlag);
                } else if (ACTION_INSTALL_FAILED.equals(intent.getAction())) {
                    LogUtils.i(TAG, "Install failure"
                            + " name: " + intent.getStringExtra(EXTRA_NAME)
                            + " pkg: " + intent.getStringExtra(EXTRA_PACKAGE)
                            + " err: " + intent.getStringExtra(EXTRA_INSTALL_ERROR_RESON)
                            + " path: " + runningFlag);
                    listener.onInstallApks(list.size(), index, false, runningFlag);
                } else {
                    return;
                }
                BaseHelper.getContext().unregisterReceiver(this);
                running = false;
                runningFlag = null;
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_INSTALL_SUCCESS);
        intentFilter.addAction(ACTION_INSTALL_FAILED);
        running = false;
        for (int i = 0; i < list.size(); i++) {
            index = i + 1;
            String apkPath = list.get(i);
            LogUtils.i(TAG, "Installing: " + apkPath);
            running = true;
            runningFlag = apkPath;
            BaseHelper.getContext().registerReceiver(receiver, intentFilter);
            InstallUtils.quitInstall(BaseHelper.getContext(), apkPath, apkPath, apkPath);
            long startT = SystemClock.elapsedRealtime();
            do {
                if (TIME_OUT <= SystemClock.elapsedRealtime() - startT) {
                    LogUtils.i(TAG, "Install timeout: " + runningFlag);
                    BaseHelper.getContext().unregisterReceiver(receiver);
                    listener.onInstallApks(list.size(), index, false, runningFlag);
                    running = false;
                    runningFlag = null;
                    break;
                }
                ThreadUtils.sleep(1000);
            } while (running);
            FileUtils.deleteDirOrFile(apkPath);
        }
        index = 0;
    }

    public static void uninstallApks(String path, BusinessListener listener) {
        File config = new File(path + APK_PKGS);
        if (!config.exists() || !config.isFile() || !config.canRead()) {
            listener.onUnInstallApks(0, 0, true, null);
            return;
        }
        FileInputStream inputStream = null;
        BufferedReader reader = null;
        String[] pkgs = null;
        try {
            inputStream = new FileInputStream(config);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer buffer = new StringBuffer();
            String line = null;
            boolean hasEnter = false;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                line = line.replaceAll("\\s*", "");
                if (TextUtils.isEmpty(line)) {
                    continue;
                }
                buffer.append(line);
                buffer.append("\n");
                hasEnter = true;
            }
            if (hasEnter) {
                buffer.deleteCharAt(buffer.lastIndexOf("\n"));
                pkgs = buffer.toString().split("\n");
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "uninstallApks()", e);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, "uninstallApks() finally: ", e);
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, "uninstallApks() finally: ", e);
            }
        }

        if (null == pkgs || 0 == pkgs.length) {
            listener.onUnInstallApks(0, 0, true, null);
            return;
        }

        ArrayList<String> list = new ArrayList<>();
        for (String pkg : pkgs) {
            if (TextUtils.isEmpty(pkg)) {
                continue;
            }
            AppInfo appInfo = new AppInfo(BaseHelper.getContext());
            appInfo.setOtherPackageName(pkg);
            if (appInfo.isOtherApp()) {
                list.add(pkg);
                LogUtils.d(TAG, "has pkg: " + pkg);
            } else {
                LogUtils.d(TAG, "has not pkg: " + pkg);
            }
        }
        if (list.size() == 0) {
            listener.onUnInstallApks(0, 0, true, null);
            return;
        }
        uninstallApkSync(list, listener);
    }

    private static void uninstallApkSync(ArrayList<String> list, BusinessListener listener) {
        running = false;
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (null == intent) {
                    return;
                }
                if (ACTION_UNINSTALL_SUCCESS.equals(intent.getAction())) {
                    LogUtils.i(TAG, "Uninstall success"
                            + " name: " + intent.getStringExtra(EXTRA_NAME)
                            + " pkg: " + intent.getStringExtra(EXTRA_PACKAGE)
                            + " path: " + runningFlag);
                    listener.onUnInstallApks(list.size(), index, true, runningFlag);
                } else if (ACTION_UNINSTALL_FAILED.equals(intent.getAction())) {
                    LogUtils.i(TAG, "Uninstall failure"
                            + " name: " + intent.getStringExtra(EXTRA_NAME)
                            + " pkg: " + intent.getStringExtra(EXTRA_PACKAGE)
                            + " err: " + intent.getStringExtra(EXTRA_UNINSTALL_ERROR_RESON)
                            + " path: " + runningFlag);
                    listener.onUnInstallApks(list.size(), index, false, runningFlag);
                } else {
                    return;
                }
                BaseHelper.getContext().unregisterReceiver(this);
                running = false;
                runningFlag = null;
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UNINSTALL_SUCCESS);
        intentFilter.addAction(ACTION_UNINSTALL_FAILED);
        for (int i = 0; i < list.size(); i++) {
            index = i + 1;
            String pkg = list.get(i);
            LogUtils.i(TAG, "Uninstalling: " + pkg);
            running = true;
            runningFlag = pkg;
            BaseHelper.getContext().registerReceiver(receiver, intentFilter);
            InstallUtils.quitUninstall(BaseHelper.getContext(), pkg);
            long startT = SystemClock.elapsedRealtime();
            do {
                if (TIME_OUT <= SystemClock.elapsedRealtime() - startT) {
                    LogUtils.i(TAG, "Uninstalling timeout: " + runningFlag);
                    BaseHelper.getContext().unregisterReceiver(receiver);
                    listener.onUnInstallApks(list.size(), index, false, runningFlag);
                    running = false;
                    runningFlag = null;
                    break;
                }
                ThreadUtils.sleep(1000);
            } while (running);
        }
        index = 0;
    }

}
