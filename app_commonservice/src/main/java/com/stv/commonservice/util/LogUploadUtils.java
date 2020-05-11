
package com.stv.commonservice.util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

/**
 * util for upload log.
 **/
public class LogUploadUtils {
    private static LogUtils sLog = LogUtils.getInstance("Common", LogUploadUtils.class.getSimpleName());
    private static final String PACKAGE_NAME_FEEDBACK = "com.stv.feedback";
    private static final String ACTION_FEEDBACK = "com.letv.action.feedback_addon";

    /**
     * upload log by feedback.
     * @param context
     * @param phone phoneNum to identify log.
     * @param path the path need to upload, if null upload system log.
     * @param describe to describe this log.
     * @see ##http://wiki.letv.cn/pages/viewpage.action?pageId=66184448.
     **/
    public static void uploadLogs(Context context, String phone, String path, String describe) {
        Intent appIntent = new Intent(ACTION_FEEDBACK); // action不能修改

        if (Build.VERSION.SDK_INT >= 19) {
            appIntent.setPackage(PACKAGE_NAME_FEEDBACK);
        }

        // 来源，告诉问题反馈是哪个应用上报的，例如游戏中心是gamecenter，这个请作为必填项，后期会有上报来源统计
        appIntent.putExtra("source_ID", context.getPackageName());

        // 联系方式请自行添加，这个参数是必须有的，建议你们设置一个自己独有的号码，便于在服务端根据号码查询结果
        if (!TextUtils.isEmpty(phone)) {
            appIntent.putExtra("leapp_phone", phone);
        } else {
            appIntent.putExtra("leapp_phone", "18888888888");
        }

        if (!TextUtils.isEmpty(path)) {
            // 路径可以自行添加，没有这句代码会自动上传各系统默认日志。
            // 如果这里设了值，则只会上传这里设置的路径日志，不会上传系统默认日志。
            // 建议不设置这个参数，直接用系统的日志
            appIntent.putExtra("addon_path", path);
        }

        // 描述请自行添加，可以不设置这个参数
        appIntent.putExtra("leapp_descripe", "" + describe);

        // 捕获RuntimeException，防止context异常时DeadObjectException
        try {
            context.startService(appIntent);
            sLog.i("start feedback service success to upload logs");
        } catch (RuntimeException e) {
            sLog.e("RuntimeException" + e.getMessage());
        } catch (Exception e) {
            sLog.e("Exception" + e.getMessage());
        }
    }
}
