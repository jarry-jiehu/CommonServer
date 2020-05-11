
package com.stv.commonservice.domain;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.stv.commonservice.common.EventDistribute;
import com.stv.commonservice.domain.util.DomainUtil;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.SystemUtils;

/**
 * 域名下发
 */
public class DoMainHandle extends EventDistribute {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_DOMAIN, DoMainHandle.class.getSimpleName());
    private static DoMainHandle sInstance;
    private Context mContext;

    private DoMainHandle(Context context) {
        mContext = context;
    }

    public static DoMainHandle getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new DoMainHandle(context);
        }
        return sInstance;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        mLog.v("******DoMain is start.*******" + "Thread id" + Thread.currentThread().getId());
        String action = intent.getAction();
        mLog.i("Receiver broadcast: " + action);
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action) || Constants.STV_ACTION_CONNECTIVITY_CHANGE.equals(action)) {
            requestDomain(mContext);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Constants.TV_ACTION_ON.equals(action)) {
            // 2016-11-10 去掉对老域名下发的兼容 ,故注释掉属性保存域名
            // 收到开机广播进行保存老域名
            DomainUtil.saveDefaultDomain(mContext);
            // 开机后初始化各标记状态，主要用于str模式，因str模式应用不会死，只处于休眠状态，标记依然保留，故开机后重新初始化
            DataStorageManager.getIntance(mContext).isRequestedForDomain = false;
            requestDomain(mContext);// 域名下发
        } else if (Constants.ALARM_BROADCAST_FOR_DOMAIN.equals(action)
                && SystemUtils.isNetworkAvailable(mContext)) {
            mLog.i("domain get AlarmReceiver received start to requesting");
            // 受到24小时后刷新的定时器广播，将首次请求的标记置为false
            DataStorageManager.getIntance(mContext).isRequestedForDomain = false;
            new DomainRequestTask(mContext).execute();
        }
    }

    private void requestDomain(Context context) {
        if (!SystemUtils.isUserUnlocked(mContext) || !SystemUtils.isNetworkAvailable(mContext))
            return;
        if (DataStorageManager.getIntance(mContext).isCanRequestDomain(context)) {
            new DomainRequestTask(context).execute();// get domain list
        }
    }
}
