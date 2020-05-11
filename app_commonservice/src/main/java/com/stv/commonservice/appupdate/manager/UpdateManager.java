
package com.stv.commonservice.appupdate.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.stv.commonservice.appupdate.model.UpdateInfo;
import com.stv.commonservice.appupdate.net.ApkDownloadTask;
import com.stv.commonservice.appupdate.net.UpdateInfoRequester;
import com.stv.commonservice.common.threadpool.DownloadApkExecutor;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.InstallUtils;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.SystemUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class UpdateManager {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_APPUPDATE, UpdateManager.class.getSimpleName());
    private static UpdateManager sInstance;
    private Context mContext;
    private AlarmManager mAlarmManager;
    private DefaultHandler mHandler;
    private DataPref mDataPref;
    private DownloadApkExecutor mExecutor;

    private List<UpdateInfo> mUpdateInfos;

    private static int sReqUpdateListCount = 0;
    private static int sReqOneUpdateListCount = 0;
    private ArrayList<PackageInfo> mExistPackageInfos;

    private UpdateManager(Context context) {
        mContext = context.getApplicationContext();
        mDataPref = DataPref.getInstance(context);
        mExecutor = DownloadApkExecutor.getInstance();
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        HandlerThread handlerThread = new HandlerThread("appupdate");
        handlerThread.start();
        mHandler = new DefaultHandler(this, handlerThread.getLooper());
    }

    public synchronized static UpdateManager getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new UpdateManager(context);
        }
        return sInstance;
    }

    /**
     * 在延迟指定时间后检测更新
     * @param delayMillis
     */
    public void checkUpdateDelay(int delayMillis) {
        Intent intent = new Intent(Constants.ACTION_CHECK_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext.getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + delayMillis,
                pendingIntent);
    }

    /**
     * 检测更新
     */
    public void checkUpdate() {
        UpdateInfoRequester.getInstance(mContext).getAllUpdateINfo(mHandler);
        // UpdateInfoRequester.getInstance(mContext).getUpdateInfo(mHandler);
    }

    public void checkOneUpdate(String packageName) {
        UpdateInfoRequester.getInstance(mContext).getOneUpdateInfo(packageName, mHandler);
    }

    public void install(String packageName) {
        if (SystemUtils.isRunningForeground(mContext, packageName)) {
            mLog.i("App running foreground, delay 5 min to install.  " + packageName);
            installDelay(packageName, 5 * 60 * 1000);
        }
        // TODO 一些app的特殊策略
        else if (cannotInstall()) {
            installDelay(packageName, 5 * 60 * 1000);
        } else {
            InstallUtils.quietInstall(mContext, packageName);
        }
    }

    /**
     * 不可升级，特殊的策略
     * @return
     */
    private boolean cannotInstall() {
        // TODO 特殊的策略
        return false;
    }

    private void installDelay(String packageName, int delayMillis) {
        Intent intent = new Intent(Constants.ACTION_INSTALL);
        intent.putExtra("pkg", packageName);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext.getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + delayMillis,
                pendingIntent);
    }

    public void downloadApk(UpdateInfo updateInfo) {
        if (updateInfo == null) {
            mLog.i("Download apk info is null.");
            return;
        }
        ApkDownloadTask apkRequester = new ApkDownloadTask(mContext, updateInfo, this);
        mExecutor.execute(apkRequester);
    }

    /**
     * handler处理类
     */
    public static class DefaultHandler extends Handler {
        private final WeakReference<UpdateManager> mOuterClassRef;

        public DefaultHandler(UpdateManager outerClass, Looper looper) {
            super(looper);
            mOuterClassRef = new WeakReference<UpdateManager>(outerClass);
        }

        @Override
        public void handleMessage(Message msg) {
            UpdateManager outerClass = mOuterClassRef.get();
            outerClass.onHandleMessage(msg);
        }
    }

    public void onHandleMessage(Message msg) {
        switch (msg.what) {
            case Constants.GET_UPDATE_LIST_SUCCESS:
                mLog.i("Update Handle :request update list success !");
                // mLog.i("Update Handle : 1 hours after the acquisition update list.");
                // checkUpdateDelay(1 * 60 * 60 * 1000);

                List<UpdateInfo> updateInfos = (List<UpdateInfo>) msg.obj;
                if (updateInfos != null && updateInfos.size() > 0) {
                    UpdateInfoManager.setUpdateInfos(updateInfos);
                    downloadApks(updateInfos);
                } else {
                    mLog.i("Update Handle : No data to be updated.");
                }
                break;
            case Constants.GET_UPDATE_LIST_ERROR:
                String errorString = (String) msg.obj;
                mLog.i("Update Handle : get update list error :" + errorString);
                if (sReqUpdateListCount < 3) {
                    mDataPref.setCheckTimeStamp(0);
                    sReqUpdateListCount++;
                    int delayMillis = 60 * 1000 * (int) Math.pow(2, sReqUpdateListCount);
                    mLog.i("Update Handle : next request time is : " + delayMillis);
                    checkUpdateDelay(delayMillis);
                } else {
                    sReqUpdateListCount = 0;
                }
                break;
            case Constants.GET_UPDATE_ONE_SUCCESS:
                List<UpdateInfo> updateInfo = (List<UpdateInfo>) msg.obj;
                if (updateInfo != null && updateInfo.size() > 0) {
                    List<UpdateInfo> updateInfosList = UpdateInfoManager.getUpdateInfos();
                    if (null == updateInfosList) {
                        updateInfosList = new ArrayList<UpdateInfo>();
                    }
                    updateInfosList.add(updateInfo.get(0));
                    UpdateInfoManager.setUpdateInfos(updateInfosList);
                    downloadApk(updateInfo.get(0));
                } else {
                    mLog.i("Apk does not need to update");
                }
                break;
            case Constants.GET_UPDATE_ONE_ERROR:
                String error = (String) msg.obj;
                mLog.i("Push Update Handle : get update one error :" + error);
                if (sReqOneUpdateListCount < 3) {
                    mDataPref.setCheckTimeStamp(0);
                    sReqOneUpdateListCount++;
                    int delayMillis = 60 * 1000 * (int) Math.pow(2, sReqUpdateListCount);
                    mLog.i("Push Update Handle : next request time is : " + delayMillis);
                    checkUpdateDelay(delayMillis);
                } else {
                    sReqOneUpdateListCount = 0;
                }
                break;
        }
    }

    public void downloadApks(List<UpdateInfo> updateInfoList) {
        if (updateInfoList != null && updateInfoList.size() > 0) {
            for (int i = 0; i < updateInfoList.size(); i++) {
                UpdateInfo nextUpdateInfo = updateInfoList.get(i);
                if (nextUpdateInfo != null) {
                    mLog.i("Start download APK , PackageName is :" + nextUpdateInfo.getPackageName());
                    UpdateManager.getInstance(mContext).downloadApk(nextUpdateInfo);
                }
            }
        } else {
            mLog.i("Full upgrade success.");
        }
    }
}
