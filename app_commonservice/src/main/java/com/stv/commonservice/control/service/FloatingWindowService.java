
package com.stv.commonservice.control.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.stv.commonservice.control.manager.CallEventManager;
import com.stv.commonservice.control.manager.RemoteControlManager;
import com.stv.commonservice.control.protocol.bean.CallBundle;
import com.stv.commonservice.control.protocol.listener.CallStopEventListener;
import com.stv.videochatsdk.api.Call;
import com.stv.videochatsdk.api.LetvCallManager;
import com.stv.videochatsdk.api.event.CallStopEvent;
import com.stv.videochatsdk.util.BusProvider;

import java.io.Serializable;

/**
 * 小窗口控制的服务
 */
public class FloatingWindowService extends Service implements CallStopEventListener {
    private final String TAG = FloatingWindowService.class.getSimpleName();
    private static boolean isControlCallType = false;
    private static CallBundle mCallBundle;
    public static final String FLOATING_ACTION = "startbind.floatingwindowservice";
    public static final String EXTRA_ISCONTROL = "type";

    @Override
    public void onStopEvent(CallStopEvent cse2) {
        Log.d(TAG, "onStopEvent  callId:" + cse2.callId);
        Call call = LetvCallManager.getInstance().getCallById(cse2.callId);
        if (mCallBundle != null && !TextUtils.isEmpty(mCallBundle.callId)
                && call != null && mCallBundle.callId.equals(call.callId)) {
            call.stop();
        }
        removeAllWindow();
        stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "start floating window sevice");
        try {
            Notification notification = new Notification();
            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
            startForeground(1, notification);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
        CallEventManager.getInstance().addCallStopEventListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (null != intent) {
            if (intent.getBooleanExtra(EXTRA_ISCONTROL, false)) {
                // 启动远程控制
                isControlCallType = true;
                Serializable extraBundle = intent.getSerializableExtra(CallBundle.KEY);
                if (null != extraBundle && extraBundle instanceof CallBundle) {
                    mCallBundle = (CallBundle) extraBundle;
                }
                RemoteControlManager.getInstance().onDirectControlEvent(mCallBundle);
            }
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new FloatingWindowServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        removeAllWindow();
        BusProvider.getInstance().unregister(this);
        CallEventManager.getInstance().cleanCallStopEventListener(this);
        try {
            stopForeground(true);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    public static void setControlCallType(boolean isControl) {
        isControlCallType = isControl;
    }

    public class FloatingWindowServiceBinder extends Binder {
        public FloatingWindowService getService() {
            return FloatingWindowService.this;
        }
    }

    public static void removeAllWindow() {
        RemoteControlManager.getInstance().removeView();
    }
}
