
package com.stv.commonservice.remoterupdate.manager;

import android.content.Context;

/**
 * 保存 检查升级过程中所有的状态值
 */
public class CheckUpdateStatusManager {
    private static CheckUpdateStatusManager instance = null;
    /********************** 遥控器检查更新 ********************/
    // 开机广播触发
    public static final int SOURCE_REMOTER_FROM_BOOT_RECIVER = 1;
    // 来自设置的升级
    public static final int SOURCE_REMOTER_FROM_SETTING = 2;
    // 来自配对成功
    public static final int SOURCE_REMOTER_FROM_PAIRING_END = 3;
    // 遥控器唤醒
    public static final int SOURCE_REMOTER_FROM_WAKEUP = 4;

    private Context mContext;
    // 遥控器检查更新来源
    private int mRemoterSource = 0;
    // 是否正在检查遥控器升级
    private boolean isCheckingRemoterUpdate = false;
    // 保存当前显示的Activity 的个数
    private int mActivitysNum = 0;

    public CheckUpdateStatusManager(Context context) {
        this.mContext = context;
    }

    public static synchronized CheckUpdateStatusManager getInstance(Context context) {
        if (null == instance) {
            instance = new CheckUpdateStatusManager(context);
        }
        return instance;
    }

    public int getRemoterUpdateSource() {
        return mRemoterSource;
    }

    public void setRemoterUpdateSource(int source) {
        this.mRemoterSource = source;
    }

    public boolean isCheckingRemoterUpdate() {
        return isCheckingRemoterUpdate;
    }

    public void setIsCheckingUpdate(boolean mIsCheckingUpdate) {
        this.isCheckingRemoterUpdate = mIsCheckingUpdate;
    }

    public int getmActivitysNum() {
        return mActivitysNum;
    }

    /**
     * 当打开一个Activity mActivitysNum 加 1
     */
    public void increaseActivitysNum() {
        mActivitysNum += 1;
    }

    /**
     * 当finish 一个activity mActivitysNum 减 1
     */
    public void reduceActivitysNum() {
        mActivitysNum -= 1;
    }

}
