
package com.stv.commonservice.remoterupdate.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.customproxy.IWindowManagerProxy;
import com.stv.commonservice.R;
import com.stv.commonservice.remoterupdate.ReportLog;
import com.stv.commonservice.remoterupdate.manager.CheckUpdateStatusManager;
import com.stv.commonservice.remoterupdate.task.UpdateCallBack;
import com.stv.commonservice.remoterupdate.task.UpdateTask;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;
import com.stv.commonservice.util.SystemUtils;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateProgressActivity extends Activity implements UpdateCallBack {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_REMOTERUPDATE, UpdateProgressActivity.class.getSimpleName());
    private TextView mTextViewTips = null;
    private TextView mTvDoing = null;
    private LinearLayout mLayoutComplete = null;
    private ProgressBar mProgressBar = null;
    private Timer mTimer = new Timer();
    private FinishTask mFinishTask = null;
    private WakeLock mWklk;
    private DataPref mDataPref;
    private int mProgress = 0;
    private boolean mIsUpdateSuccess = false;
    private static final int UPDATE_START = 0;
    private static final int UPDATE_PROGRESS = 1;
    private static final int UPDATE_FINISH = 2;

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_START:
                case UPDATE_PROGRESS:
                    progressChange(mProgress);
                    break;
                case UPDATE_FINISH:
                    // 如果升级来自设置，则通知设置升级完毕
                    if (CheckUpdateStatusManager.SOURCE_REMOTER_FROM_SETTING == CheckUpdateStatusManager.getInstance(UpdateProgressActivity.this).getRemoterUpdateSource()) {
                        sendBroadcast(new Intent(Constants.ACTION_RESTART_SETTING));
                        mLog.i("send result to  settings ");
                    }
                    if (mIsUpdateSuccess) {
                        mTextViewTips.setVisibility(View.GONE);
                        mLayoutComplete.setVisibility(View.VISIBLE);
                    } else {
                        mTextViewTips.setText(getString(R.string.update_progress_error));
                        setLaterUpdate();
                    }
                    reportAgnes(mIsUpdateSuccess);
                    report(mIsUpdateSuccess);
                    // 5S 后退出界面
                    mTimer.schedule(mFinishTask, 5 * 1000);
                    break;
                default:
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 记录创建一个activity
        CheckUpdateStatusManager.getInstance(UpdateProgressActivity.this).increaseActivitysNum();
        setContentView(R.layout.layout_update_progress);
        init();
        startUpdate();
        // 禁止遥控器的使用
        IWindowManagerProxy.shieldHome(this, true);
    }

    private void startUpdate() {
        UpdateTask updateTask = new UpdateTask(this, this);
        updateTask.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 记录finish 一个Activity
        CheckUpdateStatusManager.getInstance(UpdateProgressActivity.this).reduceActivitysNum();
        // 激活遥控器使用
        IWindowManagerProxy.shieldHome(this, false);
        // 释放屏保
        if (mWklk != null)
            mWklk.release();
    }

    private void init() {
        mDataPref = DataPref.getInstance(UpdateProgressActivity.this);
        mTextViewTips = (TextView) findViewById(R.id.progress_tips);
        mTvDoing = (TextView) findViewById(R.id.progress_doing);
        mLayoutComplete = (LinearLayout) findViewById(R.id.progress_complete);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mFinishTask = new FinishTask();
        // 升级过程禁止弹出屏保
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWklk = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE, "StvRemoterupdate-remoter");
        mWklk.acquire();
    }

    @Override
    public void onUpdateStart(int progress) {
        mProgress = progress;
        mHandler.sendEmptyMessage(UPDATE_START);

    }

    @Override
    public void onUpdateProgressChanged(int progress) {
        mProgress = progress;
        mHandler.sendEmptyMessage(UPDATE_PROGRESS);
    }

    @Override
    public void onUpdateFinished(boolean successed) {
        mIsUpdateSuccess = successed;
        mHandler.sendEmptyMessage(UPDATE_FINISH);
    }

    private void progressChange(int progress) {
        mTvDoing.setText(getString(R.string.update_progress_doing).replace("progress",
                String.valueOf(progress)));
        mProgressBar.setProgress(progress);
    }

    class FinishTask extends TimerTask {

        @Override
        public void run() {
            UpdateProgressActivity.this.finish();
        }

    }

    /**
     * 升级失败 将稍后升级的标志置true
     */
    private void setLaterUpdate() {
        // SharedPreferences sp =
        // getSharedPreferences(Constants.Y_PREFERENCE_NAME,
        // MODE_PRIVATE);
        // sp.edit().putBoolean(Constants.Y_PREFERENCE_UPDATE_LATER,
        // true).commit();
        mDataPref.setRemoterLater(true);
    }


    @Override
    public void onBackPressed() {
        return;
    }

    private void report(boolean successed) {
        ReportLog log = new ReportLog();
        String postmsg = log.spellPostMsg("ctrlUpgResult", "success", successed ? "1" : "0");
        StvHideApi.onReportLog(UpdateProgressActivity.this, "tvaction", postmsg);
    }

    /**
     * Sdk上报
     * 升级结果： 0-失败 1-成功 type: 遥控器类型（ 0-39键 1-超遥1 2-超遥2 3-超遥3 4-蓝牙遥控器 ）hwVersion: 固件版本号
     */
    private void reportAgnes(final boolean successed) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // LETV-D-R161031_V01
                String fullversion = "";
                if (SystemUtils.isOldPlatform(UpdateProgressActivity.this.getApplicationContext())) {
                    fullversion = StvHideApi.getProperty("fullversion", UpdateProgressActivity.this.getApplicationContext());
                } else {
                    fullversion = StvHideApi.getVersionAfterUpgrade("fullversion", successed, UpdateProgressActivity.this);
                }
                mLog.i("fullversion: " + fullversion);
                String type = "-1";
                if (!TextUtils.isEmpty(fullversion)) {
                    try {
                        String model = fullversion.substring(fullversion.indexOf("-") + 1, fullversion.lastIndexOf("-"));
                        // D:超3 E:超3(atv用) G:超3(938 soundbar) H:蓝牙
                        if ("D".equals(model) || "E".equals(model) || "G".equals(model)) {
                            type = "3";
                        } else if ("H".equals(model)) {
                            type = "4";
                        }
                    } catch (Exception e) {
                        mLog.e("reportAgnes exception : " + e.toString());
                    }
                }
                ReportLog reportLog = new ReportLog();
                HashMap<String, String> eventPropsMap = new HashMap<String, String>();
                eventPropsMap.put("result", successed ? "1" : "0");
                eventPropsMap.put("type", type);
                eventPropsMap.put("hwVersion", fullversion);
                reportLog.agnesReport(UpdateProgressActivity.this.getApplicationContext(), "TVRemoteControl", "",
                        "update", eventPropsMap);
            }
        }).start();

    }
}
