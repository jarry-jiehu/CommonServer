
package com.stv.commonservice.module.business.parsor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.SystemClock;

import com.stv.commonservice.library.base.BaseHelper;
import com.stv.commonservice.module.business.R;
import com.stv.commonservice.module.business.callback.BusinessListener;
import com.stv.commonservice.module.business.helper.BusinessAnimationHelper;
import com.stv.commonservice.module.business.utils.BusinessBroadcast;
import com.stv.library.common.util.BroadcastUtils;
import com.stv.library.common.util.FileUtils;
import com.stv.library.common.util.LogUtils;
import com.stv.library.common.util.MD5Utils;
import com.stv.library.common.util.ThreadUtils;

import java.util.ArrayList;

public class BusinessFilesParsor {
    private static final String TAG = "BusinessFilesParsor";

    private static volatile boolean running = false;
    private static volatile String runningFlag;
    private static final long TIME_OUT = 3 * 60 * 1000L;

    private static final String DEST_DIR_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/business/files";

    private static final String BOOT_LOGO = "/logo.jpg";
    private static final String BOOT_VIDEO = "/video.ts";
    private static final String SHUTDOWN = "/shutdown.jpg";
    private static final String BOOT_ANIMATION = "/bootanimation.png";
    private static final String SCREENSAVER_1 = "/screensaver1.jpg";
    private static final String SCREENSAVER_2 = "/screensaver2.jpg";
    private static final String SCREENSAVER_3 = "/screensaver3.jpg";
    private static final String SCREENSAVER_4 = "/screensaver4.jpg";
    private static final String SCREENSAVER_5 = "/screensaver5.jpg";
    private static final String DESKTOP_BACKGROUND = "/background.jpg";
    private static final String DTV_CMDB_0 = "/dtv_cmdb_0.bin";
    private static final String DTV_CMDB_1 = "/dtv_cmdb_1.bin";
    private static final String APP_AUTHORITY = "/appAuthority.txt";

    private static final String[] FILE_NAMES = {
            BOOT_ANIMATION,
            BOOT_LOGO,
            BOOT_VIDEO,
            SHUTDOWN,
            SCREENSAVER_1,
            SCREENSAVER_2,
            SCREENSAVER_3,
            SCREENSAVER_4,
            SCREENSAVER_5,
            DESKTOP_BACKGROUND,
            DTV_CMDB_0,
            DTV_CMDB_1,
            APP_AUTHORITY
    };

    public static void readFiles(String path, BusinessListener listener) {
        FileUtils.checkDirExists(DEST_DIR_PATH);
        ArrayList<String> fileList = new ArrayList<>();
        for (int i = 0; i < FILE_NAMES.length; i++) {
            String src = path + FILE_NAMES[i];
            if (!FileUtils.isExists(src)) {
                continue;
            }
            fileList.add(FILE_NAMES[i]);
        }

        if (0 == fileList.size()) {
            listener.onCopyFiles(0, 0, null);
            return;
        }

        int index = 0;
        for (String name : fileList) {
            index++;
            String msg;
            String src = path + name;
            String dest = DEST_DIR_PATH + name;
            if (FileUtils.isExists(dest)) {
                FileUtils.deleteDirOrFile(dest);
            }
            boolean copyed = FileUtils.copyFile(src, dest);
            if (copyed) {
                String srcMD5 = MD5Utils.getFileMD5(src);
                String destMD5 = MD5Utils.getFileMD5(dest);
                if (null != srcMD5 && srcMD5.equals(destMD5)) {
                    msg = BaseHelper.getContext().getString(R.string.file_text_copy_success, src);
                    listener.onCopyFiles(fileList.size(), index, msg);
                    broadcastSync(fileList.size(), index, name, dest, listener);
                } else {
                    msg = BaseHelper.getContext().getString(R.string.file_text_check_failure, src);
                    listener.onCopyFiles(fileList.size(), index, msg);
                }
            } else {
                msg = BaseHelper.getContext().getString(R.string.file_text_copy_failure, src);
                listener.onCopyFiles(fileList.size(), index, msg);
            }
        }
        FileUtils.deleteDirOrFile(DEST_DIR_PATH);
    }

    private static void broadcastSync(int total, int index, String name, String path, BusinessListener listener) {
        running = false;
        boolean isOwn = false;
        String pkg = null;
        String className = null;
        if (BOOT_ANIMATION.equals(name)) {
            isOwn = true;
        } else if (BOOT_LOGO.equals(name)
                || BOOT_VIDEO.equals(name)
                || SHUTDOWN.equals(name)) {
            pkg = "com.stv.bootadmanager";
            className = "com.stv.bootadmanager.module.business.receiver.BusinessReceiver";
        } else if (SCREENSAVER_1.equals(name)
                || SCREENSAVER_2.equals(name)
                || SCREENSAVER_3.equals(name)
                || SCREENSAVER_4.equals(name)
                || SCREENSAVER_5.equals(name)) {
            pkg = "com.stv.screenflyter";
            className = "com.stv.screenflyter.receiver.BusinessReceiver";
        } else if (DESKTOP_BACKGROUND.equals(name)) {
            pkg = "com.stv.deskplatform";
            className = "com.stv.deskplatform.receiver.BusinessReceiver";
        } else if (DTV_CMDB_0.equals(name)
                || DTV_CMDB_1.equals(name)) {
            pkg = "com.stv.signalsourcemanager";
            className = "com.stv.signalsourcemanager.receivers.SignalGlobalReceiver";
        } else if (APP_AUTHORITY.equals(name)) {
            pkg = "com.stv.helper.service";
            className = "com.stv.helper.receiver.AuthorityDataReceiver";
        } else {
            listener.onCopyFiles(total, index, BaseHelper.getContext().getString(R.string.file_text_not_support_file, name));
            return;
        }

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (null == intent) {
                    return;
                }
                if (BusinessBroadcast.ACTION_NOTIFICATION_RESULT.equals(intent.getAction())) {
                    String msg = intent.getStringExtra(BusinessBroadcast.EXTRA_MSG);
                    String path = intent.getStringExtra(BusinessBroadcast.EXTRA_PATH);
                    LogUtils.i(TAG, "onReceive() Receive msg: "
                            + " msg: " + msg
                            + " path: " + path
                            + " running: " + runningFlag);
                    if (null != runningFlag && runningFlag.equals(path)) {
                        listener.onCopyFiles(total, index, msg);
                        BaseHelper.getContext().unregisterReceiver(this);
                        running = false;
                        runningFlag = null;
                    } else {
                        LogUtils.i(TAG, "onReceive() Msg is too old.");
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BusinessBroadcast.ACTION_NOTIFICATION_RESULT);
        running = true;
        runningFlag = path;
        BaseHelper.getContext().registerReceiver(receiver, intentFilter);
        if (isOwn) {
            String msg;
            if (BusinessAnimationHelper.isOwnFile(path)) {
                if (BusinessAnimationHelper.customAnimation(path)) {
                    msg = BaseHelper.getContext().getString(R.string.file_text_setting_animation_success);
                } else {
                    msg = BaseHelper.getContext().getString(R.string.file_text_setting_animation_failure);
                }
            } else {
                msg = BaseHelper.getContext().getString(R.string.file_text_code_err);
            }
            BusinessBroadcast.sendNotificationResult(path, msg);
        } else {
            String msg;
            long startT = SystemClock.elapsedRealtime();
            BusinessBroadcast.sendNotification(pkg, className, path);
            do {
                if (TIME_OUT <= SystemClock.elapsedRealtime() - startT) {
                    msg = BaseHelper.getContext().getString(R.string.file_text_timeout, runningFlag);
                    BaseHelper.getContext().unregisterReceiver(receiver);
                    running = false;
                    runningFlag = null;
                    listener.onCopyFiles(total, index, msg);
                    break;
                }
                ThreadUtils.sleep(1000);
            } while (running);
        }
    }
}
