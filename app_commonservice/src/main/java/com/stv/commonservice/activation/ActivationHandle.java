
package com.stv.commonservice.activation;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.stv.commonservice.common.EventDistribute;
import com.stv.commonservice.domain.DataStorageManager;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.SystemUtils;

/**
 * 激活
 */
public class ActivationHandle extends EventDistribute {

    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_ACTIVATION, ActivationHandle.class.getSimpleName());

    private static ActivationHandle sInstance;
    private Context mContext;
    private DataPref mDataPref;

    private ActivationHandle(Context context) {
        mContext = context;
        mDataPref = DataPref.getInstance(mContext);
    }

    public static ActivationHandle getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new ActivationHandle(context);
        }
        return sInstance;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        mLog.d("******Activation is start.*******" + "Thread id" + Thread.currentThread().getId());
        // 如果系统已被激活则不再请求激活
        if (!SystemUtils.isUserUnlocked(mContext) || mDataPref.getACTIVE()) {
            mLog.d("system is actived or user is locked");
            return;
        }
        if (null == intent) {
            return;
        }
        String action = intent.getAction();
        mLog.i("Receiver broadcast: " + action);
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action) || Constants.STV_ACTION_CONNECTIVITY_CHANGE.equals(action)) {
            initActive(mContext);// 激活
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Constants.TV_ACTION_ON.equals(action)) {
            // 开机后初始化标记状态，主要用于str模式，因str模式应用不会死，只处于休眠状态，标记依然保留，故开机后重新初始化
            DataStorageManager.getIntance(mContext).isRequestedForActivation = false;
            initActive(mContext);// 激活
        } else if (Constants.ALARM_BROADCAST_FOR_ACTIVATION.equals(action)) {
            boolean first = intent.getBooleanExtra("first", false);
            new ActivationTask(mContext, first).execute();
        }
    }

    private void initActive(Context context) {
        // 如果本次开机之后未请求过，并有网络的情况下，进行激活请求
        mLog.i("initActive : [ isNetworkAvailable : " + SystemUtils.isNetworkAvailable(mContext) + " ] [ isRequestedForActivation : "
                + DataStorageManager.getIntance(mContext).isRequestedForActivation + " ]");
        if (SystemUtils.isNetworkAvailable(mContext) && !DataStorageManager.getIntance(mContext).isRequestedForActivation) {
            // 进行激活请求，true代表开机首次激活，false代表 进行半小时联网激活
            new ActivationTask(context, true).execute();
        }
    }
}
