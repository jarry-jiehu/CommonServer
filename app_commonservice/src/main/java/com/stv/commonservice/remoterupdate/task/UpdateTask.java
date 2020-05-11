
package com.stv.commonservice.remoterupdate.task;

import android.content.Context;

import com.stv.commonservice.common.threadpool.AdvancedRunnable;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;

public class UpdateTask extends AdvancedRunnable {

    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_REMOTERUPDATE, UpdateTask.class.getSimpleName());
    private Context mContext = null;
    private UpdateCallBack mUpdateCallBack = null;
    private String lastProgress = "";
    private int repeatTime = 0;
    private boolean isUpdateComplete = false;

    public UpdateTask(Context aContext, UpdateCallBack aUpdateCallBack) {
        super();
        this.mContext = aContext;
        this.mUpdateCallBack = aUpdateCallBack;
    }

    @Override
    protected void onExecute() {
        // 升级操作是底层实现，这里调用底层接口，通知其开始升级，升级包底层会自动找到位置，因此参数传空字符串即可
        StvHideApi.startUpgrade("",mContext.getApplicationContext());
        mLog.i("remoter update start");
        try {
            int progressCount = 0;
            // 因为需要获取底层的升级状态，因此此处需要 重复 调用 底层 getProperty("progress") 来获取进度。
            while (true) {
                // 表示连续60次拿到的进度都一样，则表示升级失败，直接return;
                if (repeatTime >= 60) {
                    mLog.i("progress do not change in 1 minute  | remoter update failed");
                    return;
                }
                // 连续5次 进度一样则获取升级状态
                if (repeatTime >= 5) {
                    String status = StvHideApi.getProperty("status",mContext.getApplicationContext());
                    mLog.i("remoter update progress not change in 5s");
                    // 若返回-1 代表升级失败
                    if ("-1".equals(status)) {
                        mLog.i("remoter update failed");
                        return;
                    }
                }
                // 防止调用接口频繁， 线程睡眠1S
                Thread.sleep(Constants.Y_MAX_DELAY_TIME);
                String str_progress = StvHideApi.getProperty("progress",mContext.getApplicationContext());
                mLog.i("str progress=" + str_progress);
                // 调用底层接口获取参数为空
                if (str_progress == null || str_progress.equals("")) {
                    // 如果为 空，则重试 5次
                    if (progressCount++ >= 5) {
                        return;
                    }
                    continue;
                }
                // 获取进度不为空，则将重试次数置0
                progressCount = 0;
                // 判断此时拿到的进度 是否和前一秒的进度一样
                if (lastProgress.equals(str_progress)) {
                    repeatTime++;
                } else {
                    repeatTime = 0;
                }
                lastProgress = str_progress;
                float progress = Float.parseFloat(str_progress);
                mLog.i("progress = " + progress);
                if (mUpdateCallBack != null) {
                    mUpdateCallBack.onUpdateProgressChanged((int) (progress * 100));
                } else {
                    mLog.i("upgrading mUpdateCallBack is null");
                }
                // 当进度 等于1时跳出循环
                if (progress == 1) {
                    break;
                }
            }
            int statusCount = 0;
            // 升级进度 为1 之后，循环获取底层返回的升级状态
            while (true) {
                mLog.i("select update status");
                Thread.sleep(Constants.Y_MAX_DELAY_TIME);
                String status = StvHideApi.getProperty("status",mContext.getApplicationContext());
                if ("1".equals(status)) {
                    isUpdateComplete = true;
                    mLog.i("remoter update success");
                    break;
                }
                if ("-1".equals(status)) {
                    mLog.i("remoter update failed");
                    break;
                }
                // 5次拿不到视为升级成功
                if (statusCount >= 5) {
                    isUpdateComplete = true;
                    mLog.i("remoter update not get status!");
                    break;
                }
                statusCount++;
            }
        } catch (NumberFormatException e) {
            mLog.e(e.toString());
            e.printStackTrace();
        } catch (InterruptedException e) {
            mLog.e(e.toString());
            e.printStackTrace();
        }
    }

    @Override
    protected void afterExecute() {
        mLog.i("updatetask  finalTask");
        if (mUpdateCallBack != null) {
            mUpdateCallBack.onUpdateFinished(isUpdateComplete);
        }

    }

}
