package com.stv.commonservice.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.stv.commonservice.common.receiver.AlarmReceiver;
import com.stv.commonservice.common.receiver.SystemReceiver;
import com.stv.commonservice.common.receiver.TestReceiver;

/**
 * Created by zhaoyiming on 19-4-9.
 */

public class DynamicRegistReceiverUtils {

    private static DynamicRegistReceiverUtils sInstance;
    private Context mContext;

    private BroadcastReceiver mDynamicSystemReceiver = new SystemReceiver();
    private BroadcastReceiver mDynamicAlarmReceiver = new AlarmReceiver();
    private BroadcastReceiver mTestReceiver = new TestReceiver();

    private DynamicRegistReceiverUtils(Context context) {
        mContext = context;
    }

    public synchronized static DynamicRegistReceiverUtils getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new DynamicRegistReceiverUtils(context);
        }
        return sInstance;
    }


    // SystemReceiver自定义广播改成动态注册
    public void registerSystemReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Constants.STV_ACTION_CONNECTIVITY_CHANGE);
        intentFilter.addAction(Constants.ACTION_INSTALL_FAILED);
        intentFilter.addAction(Constants.ACTION_INSTALL_SUCCESS);
        intentFilter.addAction(Constants.ACTION_UNINSTALL_FAILED);
        intentFilter.addAction(Constants.ACTION_UNINSTALL_SUCCESS);
        mContext.registerReceiver(mDynamicSystemReceiver, intentFilter);
    }

    public void unregisterSystemReceiver() {
        if (null != mDynamicSystemReceiver) {
            mContext.unregisterReceiver(mDynamicSystemReceiver);
        }
    }

    public void registerAlarmReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_CHECK_UPDATE);
        intentFilter.addAction(Constants.ALARM_BROADCAST_FOR_ACTIVATION);
        intentFilter.addAction(Constants.ALARM_BROADCAST_FOR_DOMAIN);
        intentFilter.addAction(Constants.ALARM_BROADCAST_FOR_ATTESTATION);
        intentFilter.addAction(Constants.ACTION_REMOTE_UPDATE_WAKE_UP);
        intentFilter.addAction(Constants.ACTION_PAIRING_END);
        intentFilter.addAction(Constants.ACTION_REMOTE_UPDATE_SETTING);
        intentFilter.addAction(Constants.ACTION_REMOTE_UPDATE_ALARM);
        intentFilter.addAction("android.test");
        mContext.registerReceiver(mDynamicAlarmReceiver, intentFilter);
    }

    public void unregisterAlarmReceiver() {
        if (null != mDynamicAlarmReceiver) {
            mContext.unregisterReceiver(mDynamicAlarmReceiver);
        }
    }

    public void registerTestReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.stv.activation.action.test");
        mContext.registerReceiver(mTestReceiver, intentFilter);
    }

    public void unregisterTestReceiver() {
        if (null != mTestReceiver) {
            mContext.unregisterReceiver(mTestReceiver);
        }
    }

}