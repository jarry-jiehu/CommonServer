
package com.stv.commonservice.control.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.stv.commonservice.common.AppApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Home键监听封装,避免多次注册抛出异常
 */
public class HomeWatcher extends BroadcastReceiver {
    private static final String TAG = HomeWatcher.class.getSimpleName();
    final String SYSTEM_DIALOG_REASON_KEY = "reason";
    final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
    final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    final String SYSTEN_DIALOG_REASON_SETTING_KEY = "settingkey";

    private static final List<OnHomePressedListener> mListener = new ArrayList<OnHomePressedListener>();

    private static HomeWatcher mInstance;

    private HomeWatcher() {
        AppApplication.getInstance().registerReceiver(this,
                new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @Override
    protected void finalize() throws Throwable {
        AppApplication.getInstance().unregisterReceiver(this);
        super.finalize();
    }

    public static HomeWatcher getInstance() {
        if (null == mInstance) {
            synchronized (HomeWatcher.class) {
                if (null == mInstance) {
                    mInstance = new HomeWatcher();
                }
            }
        }
        return mInstance;
    }

    public synchronized void addListener(OnHomePressedListener listener) {
        if (null != listener && !mListener.contains(listener)) {
            mListener.add(listener);
        }
    }

    public synchronized void removeListener(OnHomePressedListener listener) {
        if (null != listener && mListener.contains(listener)) {
            mListener.remove(listener);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "start action:" + action);
        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (reason != null) {
                Log.d(TAG, "receive reason = " + reason);
                List<OnHomePressedListener> cacheListener = new ArrayList<OnHomePressedListener>(
                        mListener);
                for (OnHomePressedListener listener : cacheListener) {
                    if (null != listener) {
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                            // 短按home键
                            listener.onHomePressed();
                        } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                            // 长按home键
                            listener.onHomeLongPressed();
                        } else if (reason.equals(SYSTEN_DIALOG_REASON_SETTING_KEY)) {
                            listener.onSettingPressed();
                        }
                    }
                }
            }
        }
    }

    // 回调接口
    public interface OnHomePressedListener {
        public void onHomePressed();

        public void onHomeLongPressed();

        public void onSettingPressed();
    }
}
