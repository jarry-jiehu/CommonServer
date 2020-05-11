package com.stv.commonservice.remoterupdate.task;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;

import com.stv.commonservice.common.threadpool.AdvancedRunnable;
import com.stv.commonservice.remoterupdate.ReportLog;
import com.stv.commonservice.remoterupdate.activity.UpdateActivity;
import com.stv.commonservice.remoterupdate.manager.CheckUpdateStatusManager;
import com.stv.commonservice.remoterupdate.proxy.SettingsProxy;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;
import com.stv.commonservice.util.SystemUtils;

import java.io.File;


/**
 * Created by zhaoyiming on 19-3-16.
 */

public class OldCheckLocalUpdateTask extends AdvancedRunnable {

    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_REMOTERUPDATE, OldCheckLocalUpdateTask.class.getSimpleName());

    private Context mContext;
    // 升级固件版本
    private String mNewVersion = null;
    // 当前遥控器的版本号
    private String mRemoterVersion = null;
    // 当前遥控器的model
    private String mRemoterModel = null;
    // ?? 原逻辑中的，作用未知先保留
    private int mNoCheckVersion = 0;
    // 读取当前遥控器版本号重试次数
    private int readRemoterTimes = 3;
    private DataPref mDataPref;
    private String mFileName = null;
    private String mPath = "/system/bin";
    private String mNewModel = null;


    public OldCheckLocalUpdateTask(Context context) {
        super();
        mContext = context;
        mDataPref = DataPref.getInstance(mContext);
        CheckUpdateStatusManager.getInstance(mContext).setIsCheckingUpdate(true);
    }

    @Override
    protected void onExecute() {
        mLog.i("CheckLocalUpdateTask do InBackground");
        File filePath = new File(mPath);
        File[] files = filePath.listFiles();
        for (File file : files) {
            if (file.getName().matches("remote-LETV.*R.*V.*.bin")) {
                mFileName = file.getName();
            }
        }
        if (null == mFileName) {
            mLog.i("mFileName is nulll");
            return;
        }
        // isValidRemoteBin 该方法在remotermanager 中读取了 对应的 model 遥控器的升级固件版本号 ，因此一定要先调用该方法判断是否可以升级（相当于 初始化的作用）
        if (!StvHideApi.isValidRemoteBin(mPath + "/" + mFileName, mContext.getApplicationContext())) {
            mLog.i("it's not valid remote bin");
            Settings.System.putString(mContext.getContentResolver(),
                    SettingsProxy.getControlerVersion(), "");
            return;
        }
        // 读取版本号失败
        if (!readRemoterData()) {
            mLog.i("read remoter data fail");
            return;
        }
        Settings.System.putString(mContext.getContentResolver(),
                SettingsProxy.getControlerVersion(), mNewVersion);
        // 上报当前遥控器的版本
        reportNewVersion(mRemoterVersion);
        // 保存标志此次已经检查过更新
        mDataPref.setRemoterDone(true);
        // mSPreferences.edit().putBoolean(Constants.Y_PREFERENCE_UPDATE_DONE, true).commit();
        // 是否可以升级
        if (canUpdate()) {
            startUpdate();
        }
    }

    @Override
    protected void afterExecute() {
        CheckUpdateStatusManager.getInstance(mContext).setIsCheckingUpdate(false);
    }

    private void startUpdate() {
        mLog.i("start Activity");
        if (CheckUpdateStatusManager.getInstance(mContext).getmActivitysNum() > 0) {
            // 如果升级界面正在显示，则不再弹出，防止重复弹出界面。
            mLog.i("may be remoter is upgrading");
            return;
        }
        Intent intent = new Intent(mContext, UpdateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * 对比版本号 以及判断是否在开机引导
     * @return
     */
    private boolean canUpdate() {
        if (!mNewModel.equals(mRemoterModel)) {
            mLog.i("this remoter bin is not matching the remoter ");
            return false;
        }
        // 原逻辑中的一个判断，作用未知，先保留，可能是一个测试开关
        mNoCheckVersion = StvHideApi.getSystemPropertiesInt(mContext,Constants.PROP_TEST_KEY, 0);
        if (mNoCheckVersion != 0 || mNewVersion.compareTo(mRemoterVersion) != 0) {
            int completeGuider;
            try {
                completeGuider = Settings.Secure.getInt(mContext.getContentResolver(),
                        SettingsProxy.getDeviceGuided());
                mLog.i("completeGuider : " + completeGuider);
                // 当前处在开机引导界面，则定时1分钟之后升级
                if (completeGuider <= 0) {
                    SystemUtils.setAttestAmler(mContext, Constants.ALARM_ID_REMOTER, Constants.REMOTER_CHECK_UPDATE_DELAY, Constants.ACTION_REMOTE_UPDATE_ALARM);
                    return false;
                }
            } catch (SettingNotFoundException e) {
                mLog.e("SettingNotFoundException : " + e.toString());
            }
            return true;
        }
        return false;

    }

    /**
     * 读取当前遥控器的版本，调用底层RemoteManager 提供的接口 以及固件版本
     * @return true : 读取 false : 读取失败
     */
    private boolean readRemoterData() {

        if (mFileName != null) {
            // remote-LETV-D-R151222_V01.bin
            int model = mFileName.indexOf("V");
            mNewModel = mFileName.substring(model + 2, model + 3);
            int version = mFileName.indexOf("R");
            mNewVersion = mFileName.substring(version + 1, version + 7);
            mLog.i("NewModel===" + mNewModel + "===NewVersion===" + mNewVersion);
            if (mNewModel == null || mNewVersion == null) {
                return false;
            }
        }
        mRemoterVersion = StvHideApi.getProperty("time",mContext);
        mRemoterModel = StvHideApi.getProperty("version",mContext);
        mLog.i("remoterVersion : " + mRemoterVersion + " remoterModel : " + mRemoterModel + " mNewVersion : " + mNewVersion);
        // 判断获取到的 版本号和model是否为null ，做出相应操作
        if (TextUtils.isEmpty(mRemoterVersion) || TextUtils.isEmpty(mRemoterModel)) {
            mLog.i("get remoter params error");
            // 如果是检查更新是来自遥控器配对成功触发，则 15S 之后重新获取，最多获取三次
            if (CheckUpdateStatusManager.SOURCE_REMOTER_FROM_PAIRING_END == CheckUpdateStatusManager.getInstance(mContext).getRemoterUpdateSource()) {
                mLog.i("will retry after 15s");
                try {
                    readRemoterTimes--;
                    Thread.sleep(15 * 1000);
                    if (readRemoterTimes > 0) {
                        readRemoterData();
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    mLog.e(e.toString());
                }
            }
            mDataPref.setRemoterDone(false);
            // mSPreferences.edit().putBoolean(Constants.Y_PREFERENCE_UPDATE_DONE, false).commit();
            return false;
        }
        return true;

    }

    /**
     * 上报当前遥控器的版本号
     * @param remoterVersion
     */
    private void reportNewVersion(String remoterVersion) {
        if (!TextUtils.isEmpty(remoterVersion)) {
            ReportLog log = new ReportLog();
            String postmsg = log.spellPostMsg("ctrlWakeup", "ctrlVer", remoterVersion);
            StvHideApi.onReportLog(mContext, "tvaction", postmsg);
        }
    }

}
