
package com.stv.commonservice.util;

import android.content.Context;

import com.letv.android.lcm.LetvPushManager;
import com.letv.android.lcm.PushException;

public class PushManagerUtils {
    private static LogUtils mLog = LogUtils.getInstance("Common", PushManagerUtils.class.getSimpleName());
    private static boolean sIsRegisted = false;

    public static void registerPush(final Context context) {
        new Thread() {
            @Override
            public void run() {
                final LetvPushManager pushManager = LetvPushManager.getInstance(context);
                if (sIsRegisted) {
                    return;
                }
                registePushServer(pushManager);
            }
        }.start();
    }

    private static void registePushServer(final LetvPushManager pushManager) {
        int i = 0;
        while (!sIsRegisted) {
            i++;
            if (i > 14) { // 重试10分钟仍然未连接push服务，取消连接
                break;
            }
            try {
                Thread.sleep(i * 6 * 1000);
                pushManager.register();
                sIsRegisted = true;
                mLog.i("registed.");
            } catch (RuntimeException e) {
                mLog.d("registe failed: RuntimeException" + e.getMessage());
            } catch (PushException e) {
                sIsRegisted = false;
                mLog.d("PushException, code: " + e.getCode() + ", msg: " + e.getMessage());
                if (2020 == e.getCode() && i > 5) {
                    mLog.i("PushException, code: " + e.getCode() + ", msg: " + e.getMessage());
                    break;
                }
            } catch (Exception e) {
                mLog.d("registe failed: Exception" + e.getMessage());
            }
        }
    }
}
