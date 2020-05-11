
package com.stv.commonservice.module.business.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.stv.commonservice.library.base.BaseHelper;
import com.stv.commonservice.module.business.BusinessManager;
import com.stv.commonservice.module.business.utils.UsbUtils;
import com.stv.library.common.util.LogUtils;
import com.stv.library.letv.tv.LetvShutdownUtils;

public class BusinessReceiver extends BroadcastReceiver {
    private final String TAG = "BusinessReceiver";
    private final String ACTION_STR_ON = "com.letv.android.str.TV_ACTION_ON";
    private final String ACTION_STR_OFF = "com.letv.android.str.TV_ACTION_OFF";
    private final String ACTION_USB = "com.stv.commonservice.action.BUSINESS_USB";
    private final String ACTION_ADB = "com.stv.commonservice.action.BUSINESS_ADB";
    private final String ACTION_BACKGROUND = "com.stv.commonservice.action.BUSINESS_BACKGROUND";
    private final String ACTION_BUSINESS_SHUTDOWN_BY_SDK = "com.stv.commonservice.action.BUSINESS_SHUTDOWN_BY_SDK";

    @Override
    public void onReceive(Context context, Intent intent) {
        onReceive(intent);
    }

    private void onReceive(Intent intent) {
        if (!BusinessManager.getInstance().isBusinessDevice()) {
            return;
        }
        String action = intent.getAction();
        LogUtils.d(TAG, "action = " + action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || ACTION_STR_ON.equals(action)) {
            // LogUtils.d(TAG, "start: " + "开机搜索 U 盘");
            // BusinessIntentService.startActionCheck(BaseHelper.getContext(), null);
        } else if (ACTION_STR_OFF.equals(action)) {
            LogUtils.d(TAG, "start: " + "STR OFF");
            BusinessManager.getInstance().getProcessor().dismiss();
        } else if (ACTION_BUSINESS_SHUTDOWN_BY_SDK.equals(action)) {
            LogUtils.d(TAG, "start: " + "Shutdown by sdk");
            BusinessManager.getInstance().shutdown(intent.getBooleanExtra("reboot", false));
        } else if (ACTION_USB.equals(action)) {
            LogUtils.d(TAG, "start: " + "Search U Disk");
            BusinessManager.getInstance().start(null);
        } else if (ACTION_ADB.equals(action)) {
            LogUtils.d(TAG, "start: " + "Search SDCARD without dialog");
            String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            BusinessManager.getInstance().start(sdPath, true);
        } else if (ACTION_BACKGROUND.equals(action)) {
            LogUtils.d(TAG, "start: " + "Search SDCARD without dialog and activity");
            String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            BusinessManager.getInstance().start(sdPath, true, true);
        } else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            Uri data = intent.getData();
            if (null != data) {
                String path = data.getPath();
                if (!TextUtils.isEmpty(path)) {
                    boolean mounted = UsbUtils.isMounted(path);
                    LogUtils.d(TAG, "start: " + "U Disk mounted: " + mounted + ", path: " + path);
                    UsbUtils.getUName(BaseHelper.getContext());
                    if (mounted) {
                        BusinessManager.getInstance().start(path);
                    }
                }
            }
        } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)
                || Intent.ACTION_MEDIA_EJECT.equals(action)
                || Intent.ACTION_MEDIA_REMOVED.equals(action)) {
            LogUtils.d(TAG, "start: " + "U Disk removed");
            BusinessManager.getInstance().getProcessor().dismiss();
        }
    }

}
