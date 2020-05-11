
package com.stv.commonservice.control.window;

import android.app.Instrumentation;
import android.content.Context;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.stv.commonservice.common.AppApplication;
import com.stv.commonservice.control.protocol.bean.MotionEventBean;
import com.stv.commonservice.control.util.ThreadPoolManager;
import com.stv.videochatsdk.api.LetvCallManager;

public class MotionEventUtils {
    private static final String TAG = MotionEventUtils.class.getSimpleName();
    private static MotionEventUtils instance;
    public int mScreenWidth, mScreenHeight;

    private MotionEventUtils() {
        WindowManager wm = (WindowManager) AppApplication.getInstance()
                .getSystemService(Context.WINDOW_SERVICE);
        if (null != wm) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            mScreenHeight = displayMetrics.heightPixels;
            mScreenWidth = displayMetrics.widthPixels;
            Log.d(TAG, "mScreenHeight=" + mScreenHeight + " mScreenWidth=" + mScreenWidth);
        }
    }

    public static MotionEventUtils getInstance() {
        if (null == instance) {
            synchronized (MotionEventUtils.class) {
                if (null == instance) {
                    instance = new MotionEventUtils();
                }
            }
        }
        return instance;
    }

    public void sendKeyEvent(final int KeyCode) {
        ThreadPoolManager.getInstance().getRemoteControlThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "sendKeyEvent: " + KeyCode);
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyCode);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

    public void sendPointerEvent(final MotionEvent pointerEvent) {
        ThreadPoolManager.getInstance().getRemoteControlThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "sendPointerEvent: " + pointerEvent.toString() + " reslotion="
                            + LetvCallManager.getInstance().getVideoResolution().name());
                    Instrumentation inst = new Instrumentation();
                    inst.sendPointerSync(pointerEvent);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendTrackballEventSync(final MotionEvent trackballEvent) {
        ThreadPoolManager.getInstance().getRemoteControlThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "sendKeyEvent: " + trackballEvent.toString());
                    Instrumentation inst = new Instrumentation();
                    inst.sendTrackballEventSync(trackballEvent);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

    public MotionEvent createMotionEvent(MotionEventBean bean) {
        if (null != bean) {
            MotionEvent event = MotionEvent.obtain(bean.getDownTime(), bean.getEventTime(),
                    bean.getAction(), bean.getX(), bean.getY(), bean.getMetaState());
            return event;
        } else {
            return null;
        }
    }

    public void sendMotion(int action, float percentWidth, float percentHeight) {
        Log.d(TAG, "sendMotion action=" + action + " width=" + percentWidth + " height="
                + percentHeight);
        if (0 == mScreenWidth || 0 == mScreenHeight) {
            return;
        }
        MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), action, percentWidth * mScreenWidth,
                percentHeight * mScreenHeight, 0);
        sendPointerEvent(motionEvent);
    }

}
