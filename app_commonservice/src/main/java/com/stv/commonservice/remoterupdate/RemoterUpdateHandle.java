
package com.stv.commonservice.remoterupdate;

import android.content.Context;
import android.content.Intent;

import com.stv.commonservice.common.EventDistribute;
import com.stv.commonservice.remoterupdate.activity.UpdateProgressActivity;
import com.stv.commonservice.remoterupdate.manager.CheckUpdateStatusManager;
import com.stv.commonservice.remoterupdate.task.CheckLocalUpdateTask;
import com.stv.commonservice.remoterupdate.task.OldCheckLocalUpdateTask;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;
import com.stv.commonservice.util.SystemUtils;

/**
 * 遥控器升级
 */
public class RemoterUpdateHandle extends EventDistribute {

    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_REMOTERUPDATE, RemoterUpdateHandle.class.getSimpleName());
    private static RemoterUpdateHandle sInstance;
    private Context mContext;

    private DataPref mDataPref;

    private RemoterUpdateHandle(Context context) {
        // super(intent);
        mContext = context;
        mDataPref = DataPref.getInstance(mContext);
    }

    public static RemoterUpdateHandle getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new RemoterUpdateHandle(context);
        }
        return sInstance;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        mLog.i("******RemoterUpdate is start.*******" + "Thread id" + Thread.currentThread().getId());
        String action = intent.getAction();
        mLog.i("Receiver broadcast: " + action);

        // 是否正在检查更新
        boolean isCheckedUpdate = CheckUpdateStatusManager.getInstance(mContext).isCheckingRemoterUpdate();
        // 是否正在显示界面
        boolean isUpgrading = CheckUpdateStatusManager.getInstance(mContext).getmActivitysNum() > 0;

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Constants.TV_ACTION_ON.equals(action)) {
            mLog.i("boot receiver check update");
            // BSP 会在此接口中读取rom当中的固件版本号写到setting中，因此收到开机广播后需要调用该方法初始化版本号
            StvHideApi.isValidRemoteBin(null, mContext);
            startDelayCheckUpdate(mContext);
        } else if (Constants.ACTION_REMOTE_UPDATE_SETTING.equals(action) && !isUpgrading) { // 如果升级界面没有正在显示，则直接弹出界面进行升级
            mLog.i("check update from settings");
            // 检查升级来源为设置
            CheckUpdateStatusManager.getInstance(mContext).setRemoterUpdateSource(CheckUpdateStatusManager.SOURCE_REMOTER_FROM_SETTING);
            startUpdate(mContext);
        } else if (Constants.ACTION_REMOTE_UPDATE_WAKE_UP.equals(action) && !isCheckedUpdate && !isUpgrading) {// 如果正在检查更新，或者正在显示升级界面，都不再进行检查更新
            mLog.i("check update from wake up");
            // 检查升级来源为唤醒
            CheckUpdateStatusManager.getInstance(mContext).setRemoterUpdateSource(CheckUpdateStatusManager.SOURCE_REMOTER_FROM_WAKEUP);
            // SharedPreferences sp = mContext.getSharedPreferences(
            // Constants.Y_PREFERENCE_NAME, Context.MODE_PRIVATE);
            // testKey ??? 原逻辑未知，先保留
            int testKey = StvHideApi.getSystemPropertiesInt(mContext, Constants.PROP_TEST_KEY, 0);
            // 如果已经做过检查，则退出
            if (mDataPref.getRemoterDone() && testKey == 0) {
                mLog.i("maybe romoter is first awake or has checked");
                return;
            }
            startCheckUpdate(mContext);
        } else if (Constants.ACTION_PAIRING_END.equals(action) && !isCheckedUpdate && !isUpgrading) {
            mLog.i("check update from pairing end");
            // 检查升级来源为配对
            CheckUpdateStatusManager.getInstance(mContext).setRemoterUpdateSource(CheckUpdateStatusManager.SOURCE_REMOTER_FROM_PAIRING_END);
            if (!intent.getBooleanExtra("Pairing_timeout", false)) {
                startCheckUpdate(mContext);
            }
        } else if (Constants.ACTION_REMOTE_UPDATE_ALARM.equals(action) && !isCheckedUpdate && !isUpgrading) {
            // 定时器
            mLog.i("check update from alarm");
            startCheckUpdate(mContext);
        }
    }

    private void startDelayCheckUpdate(Context context) {
        if (mDataPref.getRemoterLater()) {
            mLog.d("later reset");
            // 初始化状态值
            mDataPref.setRemoterLater(false);
        }
        // 初始化状态值
        mDataPref.setRemoterLater(false);
        // 设置定时器
        SystemUtils.setAttestAmler(mContext, Constants.ALARM_ID_REMOTER, Constants.REMOTER_CHECK_UPDATE_DELAY, Constants.ACTION_REMOTE_UPDATE_ALARM);
    }

    /**
     * 检查遥控器更新
     *
     * @param context
     */
    private void startCheckUpdate(Context context) {
        mLog.i("start service to check remoter update");
        if (SystemUtils.isOldPlatform(context)) {
            OldCheckLocalUpdateTask oldTask = new OldCheckLocalUpdateTask(context);
            oldTask.execute();
        } else {
            CheckLocalUpdateTask newTask = new CheckLocalUpdateTask(context);
            newTask.execute();
        }

    }

    /**
     * 如果从设置来的升级，直接升级不用检查更新
     *
     * @param context
     */
    private void startUpdate(Context context) {
        Intent intent = new Intent(context, UpdateProgressActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
