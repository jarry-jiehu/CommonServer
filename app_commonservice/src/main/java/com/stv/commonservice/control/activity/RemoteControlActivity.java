
package com.stv.commonservice.control.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.squareup.otto.Subscribe;
import com.stv.commonservice.common.AppApplication;
import com.stv.commonservice.control.manager.RemoteControlManager;
import com.stv.commonservice.control.protocol.bean.CallBundle;
import com.stv.commonservice.control.service.FloatingWindowService;
import com.stv.commonservice.util.LogUtils;
import com.stv.videochatsdk.api.event.CallSingalErrorEvent;
import com.stv.videochatsdk.api.event.CallStopEvent;
import com.stv.videochatsdk.api.event.SignalTimeoutEvent;
import com.stv.videochatsdk.api.event.WebRTCConnectionEvent;
import com.stv.videochatsdk.util.BusProvider;

import java.io.Serializable;

public class RemoteControlActivity extends Activity {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_REMOTE_CONTROL,
            RemoteControlActivity.class.getSimpleName());
    private CallBundle mCallBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLog.d("==onCreate==");
        if (RemoteControlManager.getInstance().isShowing()) {
            RemoteControlManager.getInstance().removeView();
        }
        BusProvider.getInstance().register(RemoteControlActivity.this); // 注册消息总线
        Serializable extraBundle = getIntent().getSerializableExtra(CallBundle.KEY);
        if (null != extraBundle && extraBundle instanceof CallBundle) {
            mCallBundle = (CallBundle) extraBundle;
            Intent intent = new Intent(AppApplication.getInstance(),
                    FloatingWindowService.class);
            intent.setAction(FloatingWindowService.FLOATING_ACTION);
            intent.putExtra(FloatingWindowService.EXTRA_ISCONTROL, true);
            intent.putExtra(CallBundle.KEY, mCallBundle);
            AppApplication.getInstance().startService(intent);
        } else {
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        mLog.d("**onDestroy**");
        BusProvider.getInstance().unregister(RemoteControlActivity.this);
        super.onDestroy();
    }

    private void finishSelf() {
        synchronized (RemoteControlActivity.this) {
            if (!RemoteControlActivity.this.isFinishing()) {
                RemoteControlActivity.this.finish();
            }

        }

    }

    @Subscribe
    public void onStopEvent(CallStopEvent cse2) {
        mLog.d("onStopEvent cse2.callId=" + cse2.callId);
        finishSelf();
    }

    @Subscribe
    public void onSignalTimeoutEvent(SignalTimeoutEvent event) {
        mLog.d("onSignalTimeoutEvent cse2.callId=" + event.callId);
        finishSelf();
    }

    @Subscribe
    public void onCallSingalError(CallSingalErrorEvent cse) {
        mLog.d("onCallSingalError cse2.callId=" + cse.reason);
        finishSelf();
    }

    @Subscribe
    public void onWebRTCConnectionEvent(WebRTCConnectionEvent webRTCConnection) {
        mLog.i("==onWebRTCConnectionEvent====: " + webRTCConnection.connectionState);
        finishSelf();
    }

}
