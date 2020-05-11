
package com.stv.commonservice.control.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.stv.commonservice.R;
import com.stv.commonservice.common.AppApplication;
import com.stv.commonservice.control.protocol.CustomManager;
import com.stv.commonservice.control.protocol.bean.CallBundle;
import com.stv.commonservice.control.protocol.listener.CallEventErrorListener;
import com.stv.commonservice.control.protocol.listener.CallEventListener;
import com.stv.commonservice.control.protocol.listener.CallRequestEventListener;
import com.stv.commonservice.control.protocol.listener.CallStopEventListener;
import com.stv.commonservice.control.protocol.receiver.StopRemoteReceiver;
import com.stv.commonservice.control.util.ThreadPoolManager;
import com.stv.commonservice.control.window.RemoteControlWindow;
import com.stv.videochatsdk.api.Call;
import com.stv.videochatsdk.api.CallType;
import com.stv.videochatsdk.api.LetvCallManager;
import com.stv.videochatsdk.api.event.CallSignalStatus;
import com.stv.videochatsdk.api.event.CallStopEvent;
import com.stv.videochatsdk.api.event.SignalTimeoutEvent;
import com.stv.videochatsdk.util.BusProvider;

import org.json.JSONObject;

/**
 * 功能: 处理FullScreenService事件 主动对Call操作,切换通话模式,二次来电接听 Custom拓传协议处理 浮层Window调用
 */
public class RemoteControlManager {
    private static final String TAG = RemoteControlManager.class.getSimpleName();
    private Call mCall;
    private LetvCallManager mLeCallManager;
    private static RemoteControlManager instance;
    private Context mContext;
    private final CustomManager mCustomManager;
    private final RemoteControlWindow mWindow;
    private CallBundle mCallBundle;
    private StopRemoteReceiver mStopRemoteReceiver;
    int mCallStatus = -1;

    private RemoteControlManager() {
        mLeCallManager = LetvCallManager.getInstance();
        mLeCallManager.setCallType(CallType.CALL);
        mLeCallManager.videoMute(true);
        mContext = AppApplication.getInstance();
        mCustomManager = CustomManager.getInstance();
        mWindow = new RemoteControlWindow();
        mStopRemoteReceiver = new StopRemoteReceiver();
    }

    public static RemoteControlManager getInstance() {
        if (null == instance) {
            synchronized (RemoteControlManager.class) {
                if (null == instance) {
                    instance = new RemoteControlManager();
                }
            }
        }
        return instance;
    }

    public boolean isShowing() {
        return mWindow.isShowing();
    }

    /**
     * 直接收到远程控制
     */
    public void onDirectControlEvent(CallBundle bundle) {
        Log.d(TAG, "onDirectControlEvent bundle=" + bundle);
        onSwitchControl(bundle);
        // 执行接听
        doQuestCall();
        displayWindow();
    }

    /**
     * 从通话中切换至远程控制
     */
    public void onSwitchControl(CallBundle bundle) {
        mCallBundle = bundle;
        removeListener();
        initListener();
    }

    private void displayWindow() {
        if (mCallStatus == 0) {
            mWindow.reset();
            mWindow.setCallBundle(mCallBundle);
            // 接听成功
            mWindow.show();
        } else {
            Log.d(TAG, "accept fail");
        }
    }

    public void removeView() {
        removeListener();
        mWindow.stop();
        // 如果没有可视activity，杀死进程,648项目节省内存
//        if (AppApplication.mVisible == 0) {
//            System.exit(0);
//        }
    }

    private void removeListener() {
        CallEventManager.getInstance().cleanCallStopEventListener(mCallStopEventListener);
        CallEventManager.getInstance().cleanCallEventListener(mCallEventListener);
        CallEventManager.getInstance().cleanCallEventErrorListener(mCallEventErrorListener);
        mCustomManager.removeTouchListener(mWindow.getTouchListener());
        mCustomManager.removeStopRemoteListener(mStopRemoteReceiver);
        mCustomManager.removeSwitchPositionListener(mWindow.getSwitchPositonListener());
        CallEventManager.getInstance().cleanCallRequestEventListener(mCallRequestEventListener);
    }

    private void initListener() {
        CallEventManager.getInstance().addCallStopEventListener(mCallStopEventListener);
        CallEventManager.getInstance().addCallEventListener(mCallEventListener);
        CallEventManager.getInstance().addCallEventErrorListener(mCallEventErrorListener);
        mCustomManager.addTouchListener(mWindow.getTouchListener());
        mCustomManager.addStopRemoteListener(mStopRemoteReceiver);
        mCustomManager.addSwitchPositionListener(mWindow.getSwitchPositonListener());
        CallEventManager.getInstance().addCallRequestEventListener(mCallRequestEventListener);
    }

    private void doQuestCall() {
        if (null != mCallBundle) {
            mCall = null;
            if (!TextUtils.isEmpty(mCallBundle.callId)) {
                mCall = mLeCallManager.getCallById(mCallBundle.callId);
                if (null != mCall) {
                    mCall.videoCallEnabled = true;//可以发送视频流
                    mCallStatus = mCall.reply(true);
                    Log.d(TAG, "reply(true)  status:" + mCallStatus + " isVideoCallEnable:" + mCall.isVideoCallEnable());
                }
            } else {
                mCall = mLeCallManager.getCurrentCall();
                if (null != mCall) {
                    mCall.videoCallEnabled = true;//可以发送视频流
                    mCallStatus = mCall.reply(true);
                    Log.d(TAG, "reply(true)  status:" + mCallStatus + " isVideoCallEnable:" + mCall.isVideoCallEnable());
                }
            }
        }
    }

    private void doStopCall() {
        Log.d(TAG, "doStopCall");
        removeView();
        if (null != mCall) {
            mCall.stop();
        }
    }

    private CallStopEventListener mCallStopEventListener = new CallStopEventListener() {
        @Override
        public void onStopEvent(CallStopEvent cse2) {
            Log.d(TAG, "onStopEvent mCall.callId=" + mCall.callId + " cse2.callId=" + cse2.callId);
            if (null != mCall && !TextUtils.isEmpty(mCall.callId)
                    && mCall.callId.equals(cse2.callId)) {
                // 保证关掉的是来电的那个对应的来电提示框
                switch (cse2.stopReason) {
                    case TIMEOUT:
                        Log.d(TAG, "TIMEOUT");
                        break;
                    case BREAKE:
                        // 对方断线
                        Toast.makeText(AppApplication.getInstance(), R.string.toast_connect_break,
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "BREAKE");
                        break;
                    case HUNGUP:
                        // 对方挂断
                        Toast.makeText(AppApplication.getInstance(), R.string.toast_respone_hungup,
                                Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "HUNGUP");
                        break;
                    case CANCEL:
                        // 已被应答
                        Log.d(TAG, "CANCEL");
                        Toast.makeText(AppApplication.getInstance(),
                                R.string.toast_respone_dealcall, Toast.LENGTH_SHORT).show();
                        break;
                    case CLOSE:
                        // 自己挂断
                        Log.d(TAG, "CLOSE");
                        break;
                    case STAT_ERROR:
                        // 呼叫失败
                        Log.d(TAG, "STAT_ERROR");
                        break;
                    default:
                        break;
                }
                doStopCall();
            }
            BusProvider.getInstance().unregister(this);
        }
    };

    private CallEventListener mCallEventListener = new CallEventListener() {
        @Override
        public void onSignalTimeoutEvent(SignalTimeoutEvent event) {
            Toast.makeText(AppApplication.getInstance(), R.string.toast_connect_break,
                    Toast.LENGTH_SHORT).show();
            if (null != mCall && !TextUtils.isEmpty(mCall.callId)
                    && mCall.callId.equals(event.callId)) {
                doStopCall();
            }
        }
    };

    private CallEventErrorListener mCallEventErrorListener = new CallEventErrorListener() {
        @Override
        public void onCallEventError() {
            doStopCall();
        }
    };

    private CallRequestEventListener mCallRequestEventListener = new CallRequestEventListener() {
        @Override
        public void onRequestEvent(final CallSignalStatus cre) {
            // boolean isBackground =
            // ActivityManager.getInstance().isApplicationBroughtToBackground(mContext);
            // boolean isActive =
            // ActivityManager.getInstance().isActive(CustomerSupportActivity.class);
            boolean isBackground = false;
            boolean isActive = true;
            Log.d(TAG, "isBackground:" + isBackground + ",isActive:" + isActive);
            if (isBackground || (!isBackground && !isActive)) {
                Log.d(TAG, "onComingCallEvent:" + " callId=" + cre.callId + " caller=" + cre.caller
                        + " callerPhone=" + cre.callerPhone + " callerDevid=" + cre.callerDevid
                        + " isVideoCall=" + cre.isVideoCall + " isControl=" + cre.isControl);
                ThreadPoolManager.getInstance().getUIThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (!isRemoteApplyCall(cre.isControl)) {
                            // 如果忽略掉这个来电，告知sdk，让sdk去处理掉栈里的来电信息
                            String callId = cre.callId;
                            if (!TextUtils.isEmpty(callId)) {
                                if (null != LetvCallManager.getInstance()) {
                                    Call call = LetvCallManager.getInstance().getCallById(callId);
                                    if (null != call) {
                                        try {
                                            JSONObject inner = new JSONObject();
                                            inner.put("reason", "remoteControlling");
                                            inner.put("app", "LetvHelper");
                                            JSONObject json = new JSONObject();
                                            json.put("deny", inner);
                                            Log.d(TAG, "deny call reason:" + json.toString());
                                            call.denyCall(json.toString());
                                        } catch (org.json.JSONException e) {
                                            Log.e(TAG, e.toString());
                                            e.printStackTrace();
                                        }
                                        Log.d(TAG, "don't allow callin callid:" + callId);
                                    } else {
                                        Log.d(TAG,
                                                "mLeCallManager is null don't allow callin callid:"
                                                        + callId);
                                    }
                                }
                            } else {
                                if (null != LetvCallManager.getInstance()) {
                                    // 挂断之前的
                                    Call call = LetvCallManager.getInstance().getCurrentCall();
                                    if (null != call) {
                                        call.reply(false);
                                        Log.d(TAG, "don't allow callin when callid is null");
                                    }
                                } else {
                                    Log.d(TAG,
                                            "mLeCallManager is null don't allow callin when callid is null");
                                }
                            }
                        }
                    }
                });
            }
        }
    };

    // 远程控制逻辑将阻止部分情况二次来电
    private boolean isRemoteApplyCall(boolean isControl) {
        if (RemoteControlManager.getInstance().isShowing()) {
            // 正在远程控制中,不允许二次来电
            Log.e(TAG, "isRemoteApplyCall Remote working ,refuse call in !!!!");
            return false;
        }
        if (isControl) {
            boolean isRemoteActive = RemoteControlManager.getInstance().isShowing();
            if (isRemoteActive) {
                // 通话或控制中不允许远程控制
                Log.e(TAG,
                        "isRemoteApplyCall Chatting ,refuse remote control call in!!!! isControl="
                                + isControl + " isCallActive=" + " isRemoteActive="
                                + isRemoteActive);
                return false;
            }
        }
        return true;
    }

    public RemoteControlWindow getRemoteControlWindow() {
        return mWindow;
    }
}
