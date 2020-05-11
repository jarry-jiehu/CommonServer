
package com.stv.commonservice.module.business.utils;

import android.content.ComponentName;
import android.content.Intent;

import com.stv.commonservice.library.base.BaseHelper;
import com.stv.commonservice.module.business.R;
import com.stv.library.common.util.BroadcastUtils;

public class BusinessBroadcast {
    public static final String ACTION_NOTIFICATION_NEED_RESULT = "com.letv.business.action.NOTIFICATION_NEED_RESULT";
    public static final String ACTION_NOTIFICATION_RESULT = "com.letv.business.action.NOTIFICATION_RESULT";
    public static final String EXTRA_PATH = "path";
    public static final String EXTRA_MSG = "msg";

    public static void sendNotification(final String pkg, final String className, final String path) {
        Intent intent = new Intent(ACTION_NOTIFICATION_NEED_RESULT);
        intent.putExtra(EXTRA_PATH, path);
        intent.setPackage(pkg);
        if (null != pkg && null != className) {
            intent.setComponent(new ComponentName(pkg, className));
        }
        if (BroadcastUtils.isRegisteBroadcast(BaseHelper.getContext(), intent)) {
            BaseHelper.getContext().sendBroadcast(intent);
        } else {
            sendNotificationResult(path, BaseHelper.getContext().getString(R.string.file_text_not_found_receiver));
        }
    }

    public static void sendNotificationResult(final String path, final String msg) {
        Intent intent = new Intent(ACTION_NOTIFICATION_RESULT);
        intent.putExtra(EXTRA_MSG, msg);
        intent.putExtra(EXTRA_PATH, path);
        intent.setPackage("com.stv.commonservice");
        BaseHelper.getContext().sendBroadcast(intent);
    }
}
