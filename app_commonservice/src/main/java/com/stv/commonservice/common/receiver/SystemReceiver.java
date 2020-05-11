
package com.stv.commonservice.common.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.stv.commonservice.common.service.SystemCommonServcice;
import com.stv.commonservice.util.LogUtils;

public class SystemReceiver extends BroadcastReceiver {
    private LogUtils mLog = LogUtils.getInstance("Common", SystemReceiver.class.getSimpleName());

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mLog.v("Receiver Broadcast : " + action);
        intent.setComponent(new ComponentName(context, SystemCommonServcice.class));
        context.startService(intent);
    }

}
