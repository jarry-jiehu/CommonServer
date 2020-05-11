
package com.stv.commonservice.appupdate;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.stv.commonservice.appupdate.manager.UpdateInfoManager;
import com.stv.commonservice.appupdate.manager.UpdateManager;
import com.stv.commonservice.appupdate.model.UpdateInfo;
import com.stv.commonservice.appupdate.net.ApkUninstallRequester;
import com.stv.commonservice.appupdate.net.UpdateReporter;
import com.stv.commonservice.appupdate.net.UpdateReporter.OperatingResults;
import com.stv.commonservice.common.EventDistribute;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.FileUtils;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StorageUtils;
import com.stv.commonservice.util.SystemUtils;

import java.util.HashMap;

/**
 * 静默升级
 */
public class AppUpdateHandle extends EventDistribute {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_APPUPDATE,
            AppUpdateHandle.class.getSimpleName());

    private static AppUpdateHandle sInstance;
    private Context mContext;
    private HashMap<String, Integer> installCountHashMap;

    private AppUpdateHandle(Context context) {
        mContext = context;
        installCountHashMap = new HashMap<String, Integer>();
    }

    public static AppUpdateHandle getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new AppUpdateHandle(context);
        }
        return sInstance;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        mLog.d("******AppUpdate is start.*******" + "Thread id" + Thread.currentThread().getId());
        if (null == intent) {
            return;
        }
        String action = intent.getAction();
        String packageName = intent.getStringExtra(Constants.EXTRA_PACKAGE);
        mLog.i("Service receive " + (null == packageName ? "" : ("from " + packageName))
                + " intent, action: " + action);
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)
                || Constants.STV_ACTION_CONNECTIVITY_CHANGE.equals(action)) {
            prepareCheckUpdate();
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Constants.TV_ACTION_ON.equals(action)) {
            DataPref dataPref = DataPref.getInstance(mContext);
            dataPref.setCheckTimeStamp(0);
            prepareCheckUpdate();
        } else if (Constants.ACTION_INSTALL_SUCCESS.equals(action)) { // 静默安装成功
            mLog.i(packageName + " Install Success");
            boolean delete = FileUtils.deleteFile(StorageUtils.getApkDir(),
                    packageName + Constants.Y_APK_SUFFIX);
            mLog.d(packageName + " Delete " + (delete ? " Success" : " Failure"));

            UpdateInfo updateInfo = UpdateInfoManager.getUpdateInfo(packageName);
            String otherdata = null;
            if (null != updateInfo) {
                otherdata = updateInfo.getOtherdata();
                UpdateReporter.getInstance(mContext).reportupdate(otherdata,
                        OperatingResults.SUCCESS);
            }

            if (UpdateInfoManager.isExistInfo(packageName)) {
                UpdateInfoManager.remove(packageName);
            }
        } else if (Constants.ACTION_INSTALL_FAILED.equals(action)) { // 静默安装失败

            Integer integer = installCountHashMap.get(packageName);
            if (null == integer) {
                installCountHashMap.put(packageName, 0);
                integer = installCountHashMap.get(packageName);
            }

            int intValue = integer.intValue();
            boolean deleteFile = FileUtils.deleteFile(StorageUtils.getApkDir(),
                    packageName + Constants.Y_APK_SUFFIX);
            mLog.i(packageName + " fail count : " + intValue + " Delete apk"
                    + (deleteFile ? " Success" : " Failure") + "   Install Failed for reason: "
                    + intent.getStringExtra(Constants.EXTRA_STORAGE_ERROR_REASON));
            if (intValue < 3) {
                installCountHashMap.put(packageName, ++intValue);
                UpdateInfo updateInfo = UpdateInfoManager.getUpdateInfo(packageName);
                UpdateManager.getInstance(mContext).downloadApk(updateInfo);
            } else {
                installCountHashMap.remove(packageName);
                UpdateInfoManager.remove(packageName);
            }
        } else if (Constants.ACTION_CHECK_UPDATE.equals(action)) {
            DataPref dataPref = DataPref.getInstance(mContext);
            if (!SystemUtils.isNetworkAvailable(mContext)) {
                mLog.i("Network is unavailable .");
                return;
            }
            long time = System.currentTimeMillis() - dataPref.getCheckTimeStamp();
            if (time < 24 * 60 * 60 * 1000 || dataPref.getCheckTimeStamp() != 0) {
                mLog.i("Not to update time.");
                return;
            }
            dataPref.setCheckTimeStamp(System.currentTimeMillis());
            UpdateManager.getInstance(mContext).checkUpdate();
            ApkUninstallRequester.getInstance(mContext).checkApkUninstall();// 获取需要卸载的应用信息
        } else if (Constants.ACTION_INSTALL.equals(action)) {
            String pkg = intent.getStringExtra("pkg");
            if (null != pkg) {
                UpdateManager.getInstance(mContext).install(pkg);
            }
        } else if (Constants.ACTION_UNINSTALL_SUCCESS.equals(action)) {
            mLog.i(packageName + " Uninstall Success");
        } else if (Constants.ACTION_UNINSTALL_FAILED.equals(action)) {
            mLog.i(packageName + " Uninstall failed");
        }
    }

    private void prepareCheckUpdate() {
        if (SystemUtils.isNetworkAvailable(mContext)) {
            mLog.i("Two minutes after the request to update the list !");
            UpdateManager.getInstance(mContext).checkUpdateDelay(2 * 60 * 1000);
        } else {
            mLog.i("Network is unavailable .");
        }
    }
}
