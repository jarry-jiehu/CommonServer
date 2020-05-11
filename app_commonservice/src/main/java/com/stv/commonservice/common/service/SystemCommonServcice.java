
package com.stv.commonservice.common.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

import com.android.letvmanager.ISystemCommonServiceManager;
import com.stv.commonservice.R;
import com.stv.commonservice.common.EventDistributeManager;
import com.stv.commonservice.control.RemoteControlHandle;
import com.stv.commonservice.domain.DataStorageManager;
import com.stv.commonservice.domain.util.DomainUtil;
import com.stv.commonservice.util.ForegroundServiceUtils;
import com.stv.commonservice.util.InstallUtils;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.ServiceManagerProxy;
import com.stv.commonservice.util.StvHideApi;
import com.stv.commonservice.util.DynamicRegistReceiverUtils;

public class SystemCommonServcice extends Service {
    private LogUtils mLog = LogUtils.getInstance("Common",
            SystemCommonServcice.class.getSimpleName());

    private final IBinder mService = new ISystemCommonServiceManager.Stub() {
        @Override
        public String getDomain(String label) {
            String domain = DomainUtil.getDomain(getApplicationContext(), label);
            mLog.i("label : " + label + "  domain: " + domain);
            return domain;
        }

        @Override
        public String getLetvCarrierState() {
            String requestCode = DataStorageManager.getIntance(getApplicationContext())
                    .getAttestRequestCode();
            mLog.i("getLetvCarrierState requestCode: " + requestCode);
            return requestCode;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLog.i("SystemService Created ...");
        // 提高servier优先级
        persistentService();
        ServiceManagerProxy.AddService(this, StvHideApi.TV_SYSTEM_COMMONSERVICE, mService);
        // 注册网络变化广播
        DynamicRegistReceiverUtils.getInstance(this).registerSystemReceiver();
        DynamicRegistReceiverUtils.getInstance(this).registerAlarmReceiver();
        DynamicRegistReceiverUtils.getInstance(this).registerTestReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent) {
            return START_STICKY;
        }
        String host = StvHideApi.getDomain(this, "ota");
        mLog.i("host: " + host);
        String action = intent.getAction();
        mLog.i("SystemService  start,action :" + action);
        EventDistributeManager.getInstance(SystemCommonServcice.this).eventDistribute(intent);
        return START_STICKY;
    }

    private void persistentService() {
        ForegroundServiceUtils.setForegroundService(this,
                getPackageName() + ".channel", "commonservice", R.mipmap.ic_launcher_round,
                "commonservice", "commonservice service", "commonservice service", false, true, 1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DynamicRegistReceiverUtils.getInstance(this).unregisterSystemReceiver();
        DynamicRegistReceiverUtils.getInstance(this).unregisterAlarmReceiver();
        DynamicRegistReceiverUtils.getInstance(this).unregisterTestReceiver();
        RemoteControlHandle.getInstance(SystemCommonServcice.this).stop();
        stopForeground(true);
    }
}
