
package com.stv.commonservice.control.protocol.receiver;

import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.stv.commonservice.R;
import com.stv.commonservice.control.manager.RemoteControlManager;
import com.stv.commonservice.control.util.HomeWatcher;
import com.stv.commonservice.control.util.ToastUtil;
import com.stv.videochatsdk.api.Call;
import com.stv.videochatsdk.api.LetvCallManager;

public class StopRemoteReceiver implements HomeWatcher.OnHomePressedListener {
    private static final String TAG = StopRemoteReceiver.class.getSimpleName();
    // 防止远程遥控多次按下干扰
    private static final long WAIT_SYSTEM_DURATION = 1000;
    private static final long WAIT_LOCAL_DOUBLE_PRESSED_DURATION = 1000;
    private boolean isRemoteHomeKeyPressed = false;
    private long mLastRemotePressedTime = -1;
    private long mLastLocalPressedTime = -1;

    public void onRemoteKeyPressed(int keyCode) {
        Log.d(TAG, "onRemoteKeyPressed=" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            isRemoteHomeKeyPressed = true;
            mLastRemotePressedTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onHomePressed() {
    }

    @Override
    public void onSettingPressed() {
        Log.d(TAG, "onHomePressed time=" + System.currentTimeMillis());
        if (isRemoteHomeKeyPressed) {
            isRemoteHomeKeyPressed = false;
        } else {
            long nowTime = System.currentTimeMillis();
            if (nowTime - mLastRemotePressedTime >= WAIT_SYSTEM_DURATION) {
                // onStopRemote();
                if (nowTime - mLastLocalPressedTime > WAIT_LOCAL_DOUBLE_PRESSED_DURATION) {
                    mLastLocalPressedTime = nowTime;
                } else {
                    onStopRemote();
                    mLastLocalPressedTime = -1;
                }
            }
        }
    }

    public void onStopRemote() {
        Call call = LetvCallManager.getInstance().getCurrentCall();
        Log.d(TAG, "onStopRemote   call:" + call);
        if (null != call) {
            // 结束通话
            ToastUtil.getInstance().showInThread(R.string.remote_exit_stop, Toast.LENGTH_LONG);
            call.stop();
        }
        // FloatingWindowService.removeAllWindow();
        RemoteControlManager.getInstance().removeView();
    }

    @Override
    public void onHomeLongPressed() {
        Log.d(TAG, "onHomeLongPressed");
    }
}
