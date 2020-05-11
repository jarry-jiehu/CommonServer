
package com.stv.commonservice.common;

import android.content.Context;
import android.content.Intent;

import com.stv.commonservice.activation.ActivationHandle;
import com.stv.commonservice.appupdate.AppUpdateHandle;
import com.stv.commonservice.attestation.AttestationHandle;
import com.stv.commonservice.common.threadpool.AdvancedExecutor;
import com.stv.commonservice.control.RemoteControlHandle;
import com.stv.commonservice.domain.DoMainHandle;
import com.stv.commonservice.remoterupdate.RemoterUpdateHandle;
import com.stv.commonservice.smarthome.SmartHomeHandle;

public class EventDistributeManager {
    private static EventDistributeManager sInstance;
    public Context mContext;

    private EventDistributeManager(Context context) {
        mContext = context;
    }

    public synchronized static EventDistributeManager getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new EventDistributeManager(context);
        }
        return sInstance;
    }

    public void eventDistribute(Intent intent) {
        // 域名下发服务
        AdvancedExecutor.getInstance()
                .execute(DoMainHandle.getInstance(mContext).setIntent(intent));
        // 播控认证
        AdvancedExecutor.getInstance()
                .execute(AttestationHandle.getInstance(mContext).setIntent(intent));
        // 激活服务
        AdvancedExecutor.getInstance()
                .execute(ActivationHandle.getInstance(mContext).setIntent(intent));
        // 应用静默升级
        AdvancedExecutor.getInstance()
                .execute(AppUpdateHandle.getInstance(mContext).setIntent(intent));
        // 遥控器升级
        AdvancedExecutor.getInstance()
                .execute(RemoterUpdateHandle.getInstance(mContext).setIntent(intent));
        // 远程控制
        AdvancedExecutor.getInstance()
                .execute(RemoteControlHandle.getInstance(mContext).setIntent(intent));
        // 智能被控
         AdvancedExecutor.getInstance().execute(SmartHomeHandle.getInstance(mContext).setIntent(intent));

    }
}
