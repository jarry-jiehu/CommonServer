
package com.stv.commonservice.smarthome.util;

import android.content.Context;
import android.content.Intent;

import com.stv.library.common.util.LogUtils;
import com.stv.library.common.util.ServiceHelper;
import com.stv.library.common.util.SyspropProxy;
import com.stv.library.common.util.ThreadManager;

import java.util.concurrent.TimeUnit;

import eui.tv.TvManager;

/**
 * 电视端被控操作工具类
 */
public class TvControlledUtil {
    private static final String TAG = TvControlledUtil.class.getSimpleName();
    public static final String ACTION_SHOW_SHUTDOWN_AD = "com.stv.admanager.action.SHOW_SHUTDOWN_AD";
    public static final String ACTION_SHUTDOWN = "phonewindowmanger.action.shutdown.by.admanager";
    public static final String KEY_SHUTDOWN_AD_SHOW_STATUS = "persist.sys.shutdown.ad.status";

    public static void shutdown(Context context, boolean isReboot) {
        LogUtils.i(TAG, "setShutdown StvUtils.getPlatform() " + TvManager.getPlatform());
        if (TvManager.getPlatform() == 15) {
            Intent intent = new Intent();
            intent.setAction(ACTION_SHOW_SHUTDOWN_AD);
            intent.setPackage(context.getPackageName());
            intent.putExtra("isReboot", isReboot);
            if (null != ServiceHelper.startService(context, intent)) {
                LogUtils.i(TAG, "shutdown() start service success.");
            } else {
                LogUtils.i(TAG, "shutdown() start service failed.");
            }
        } else {
            context.sendBroadcast(new Intent(ACTION_SHUTDOWN));
        }
        ThreadManager.getInstance().excuteScheduled(() -> {
            LogUtils.i(TAG, "shutdown() set props to 2.");
            SyspropProxy.set(context, KEY_SHUTDOWN_AD_SHOW_STATUS, "2");
        }, 10L, TimeUnit.SECONDS);
    }

}
