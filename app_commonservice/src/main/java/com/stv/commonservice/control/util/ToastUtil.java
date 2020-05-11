
package com.stv.commonservice.control.util;

import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.stv.commonservice.common.AppApplication;

import java.lang.reflect.Field;

public class ToastUtil {
    private static final String TAG = ToastUtil.class.getSimpleName();

    private static ToastUtil instance = null;
    private static Toast mToast;
    private static Handler mHandler;
    private boolean hasAlertPermission;

    private ToastUtil() {
        // 直接new可能创建在非UI线程
        mHandler = AppApplication.getInstance().getHandler();
        // hasAlertPermission =
        // PermissionUtil.hasAlterPermission(HelperApplication.getInstance());
        Log.d(TAG, "hasAlertPermission=" + hasAlertPermission);
    }

    public static ToastUtil getInstance() {
        if (null == instance) {
            synchronized (ToastUtil.class) {
                if (null == instance) {
                    instance = new ToastUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 非UI线程中,或回调中避免上下文丢失的Toast方法
     * @param id
     * @param time
     */
    public void showInThread(final int id, final int time) {
        showInThread(AppApplication.getInstance().getString(id), time);
    }

    public void showInThread(final String toast, final int time) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                showToast(toast, time);
            }
        });
    }

    private synchronized void showToast(String text, int time) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(AppApplication.getInstance(), text, time);
        if (hasAlertPermission) {
            // 测试耗时约4ms可以接受
            // logUtils.d("start=" + System.currentTimeMillis());
            setParams();
            // logUtils.d("end= " + System.currentTimeMillis());
        }
        mToast.show();
    }

    // 避免toast被dialog遮盖
    private void setParams() {
        try {
            Class c = mToast.getClass();
            Field mTN = c.getDeclaredField("mTN");
            mTN.setAccessible(true);
            Class TN = mTN.get(mToast).getClass();
            Field mParams = TN.getDeclaredField("mParams");
            mParams.setAccessible(true);
            WindowManager.LayoutParams mParam = (WindowManager.LayoutParams) mParams
                    .get(mTN.get(mToast));
            mParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            // 一旦出问题,则默认不支持,避免每次判断,节省时间
            hasAlertPermission = false;
            Log.d(TAG, "set hasAlertPermission=" + hasAlertPermission);
        }
    }
}
