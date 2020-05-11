
package com.stv.commonservice.common.receiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.letv.android.lcm.LetvPushWakefulReceiver;
import com.stv.commonservice.common.service.LetvPushIntentService;
import com.stv.commonservice.util.LogUtils;

public class PushMessageReceiver extends LetvPushWakefulReceiver {
    private LogUtils mLog = LogUtils.getInstance("Common", PushMessageReceiver.class.getSimpleName());

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mLog.d("onHandleIntent:action=" + action);
        // Explicitly specify that LetvPushIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                LetvPushIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
    }

}
