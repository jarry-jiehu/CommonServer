
package com.stv.commonservice.smarthome;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.stv.commonservice.common.EventDistribute;
import com.stv.commonservice.smarthome.manager.AccountInfoManager;
import com.stv.commonservice.smarthome.manager.DeviceManager;
import com.stv.commonservice.smarthome.manager.MessageManager;
import com.stv.commonservice.smarthome.util.Constant;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.SystemUtils;

/**
 * 智能家居受控模块
 */
public class SmartHomeHandle extends EventDistribute implements DeviceManager.DeviceCallback {

    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_SMART_HOME, SmartHomeHandle.class.getSimpleName());
    private static volatile SmartHomeHandle sInstance;
    private Context mContext;
    private DeviceManager mDeviceManager;// 设备管理类
    private MessageManager mMessageManager;// MQTT消息管理类

    private SmartHomeHandle(Context context) {
        mContext = context;
        mDeviceManager = DeviceManager.getInstance(mContext);
        mDeviceManager.setDeviceCallback(this);
        mMessageManager = MessageManager.getInstance(mContext);
    }

    public static SmartHomeHandle getInstance(Context context) {
        if (null == sInstance) {
            synchronized (SmartHomeHandle.class) {
                if (null == sInstance) {
                    sInstance = new SmartHomeHandle(context);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        mLog.d("******SmartHomeHandle is start.*******" + "Thread id" + Thread.currentThread().getId());
        String action = intent.getAction();
        mLog.i("Receiver broadcast: " + action);
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action) || Constants.STV_ACTION_CONNECTIVITY_CHANGE.equals(action)) {
            // 网络断开后断开MQTT，重新连接后重新打开
            if (SystemUtils.isNetworkAvailable(mContext)) {

            }

        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Constants.TV_ACTION_ON.equals(action)) {
            startMqtt();
        } else if (AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION.equals(action)) {

            String uid = AccountInfoManager.getInstance().getAccountInfo(mContext,
                    Constant.COLUMN_UID);
            String token = AccountInfoManager.getInstance().getAccountInfo(mContext,
                    Constant.COLUMN_TOKEN);
            mLog.d("isLoginAccount uid=" + uid + " token=" + token);
            if (TextUtils.isEmpty(token)) {
                mDeviceManager.queryHouseInfo(Constant.getHouseQueryUrl(mContext), false);
            } else {
                mDeviceManager.login(Constant.getLoginUrl(mContext));
            }

        }
    }

    private void startMqtt() {
        if (!SystemUtils.isUserUnlocked(mContext) || !SystemUtils.isNetworkAvailable(mContext))
            return;
        // 1.登陆
        String leToken = DataPref.getInstance(mContext).getStringPreferences(Constant.KEY_LE_TOKEN, "");
        mLog.i("leToken: " + leToken);
        leToken = "";
        if (TextUtils.isEmpty(leToken)) {
            mDeviceManager.login(Constant.getLoginUrl(mContext));
            return;
        }
        // 2.注册
        String elinkId = DataPref.getInstance(mContext).getStringPreferences(Constant.KEY_ELINK_ID, "");
        mLog.i("elink Id: " + elinkId);
        if (TextUtils.isEmpty(elinkId)) {
            mDeviceManager.register(Constant.getRegisterUrl(mContext));
            return;
        }
        // 3.查询
        String houseID = DataPref.getInstance(mContext).getStringPreferences(Constant.KEY_HOUSE_ID, "");
        mLog.i("house Id: " + houseID);
        if (TextUtils.isEmpty(houseID)) {
            mDeviceManager.queryHouseInfo(Constant.getHouseQueryUrl(mContext), true);
            return;
        }
        // 4.绑定(在查询到houseID后自动绑定)
        // mDeviceManager.bindDevice(Constant.getDeviceBindUrl());
        // 5.连接MQTT
        mMessageManager.connect();
    }

    @Override
    public void deviceBind() {
        mLog.i("***deviceBind***");
        mMessageManager.connect();
    }

    @Override
    public void deviceUnbind() {
        mLog.i("***deviceUnbind***");
        DataPref.getInstance(mContext).setStringPreferences(Constant.KEY_LE_TOKEN, "");
        DataPref.getInstance(mContext).setStringPreferences(Constant.KEY_ELINK_ID, "");
        DataPref.getInstance(mContext).setStringPreferences(Constant.KEY_DEVICE_SECRET, "");
        DataPref.getInstance(mContext).setStringPreferences(Constant.KEY_HOUSE_ID, "");
        if (mMessageManager.isConnected()) {
            mMessageManager.disconnect();
        }

    }
}
