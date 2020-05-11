
package com.stv.commonservice.control;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.stv.commonservice.common.AppApplication;
import com.stv.commonservice.common.EventDistribute;
import com.stv.commonservice.control.activity.RemoteControlActivity;
import com.stv.commonservice.control.manager.CallEventManager;
import com.stv.commonservice.control.manager.RemoteControlManager;
import com.stv.commonservice.control.protocol.bean.CallBundle;
import com.stv.commonservice.control.protocol.listener.CallEventErrorListener;
import com.stv.commonservice.control.protocol.listener.CallRequestEventListener;
import com.stv.commonservice.control.protocol.listener.CallStopEventListener;
import com.stv.commonservice.control.service.FloatingWindowService;
import com.stv.commonservice.control.util.SdkManager;
import com.stv.commonservice.control.util.ThreadPoolManager;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.SystemUtils;
import com.stv.videochatsdk.api.Call;
import com.stv.videochatsdk.api.LetvCallManager;
import com.stv.videochatsdk.api.event.CallSignalStatus;
import com.stv.videochatsdk.api.event.CallStopEvent;
import com.stv.videochatsdk.util.BusProvider;

import org.json.JSONObject;

/**
 * 远程控制
 */
public class RemoteControlHandle extends EventDistribute
        implements CallStopEventListener, CallEventErrorListener, CallRequestEventListener {
    private final int ACTION_ACTIVE = 100;
    private final int START_ANSWER_ACTIVITY = 101;
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_REMOTE_CONTROL,
            RemoteControlHandle.class.getSimpleName());

    private static RemoteControlHandle sInstance;
    private Context mContext;
    private LetvCallManager mLetvCallManager;
    private Call mCall;
    private AlertDialog mDialog = null;
    private CallBundle bundle;
    // isCallIn 区分来电还是去电
    private boolean isCallIn;
    private AlertDialog mdlg = null;
    private RemoteControlManager mRemoteControlManager;
    private boolean mIsActive = false;
    private Handler mCallHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mLog.d("handleMessage what:" + msg.what);

            switch (msg.what) {
                case ACTION_ACTIVE:
                    if (SystemUtils.isUserUnlocked(mContext) && SystemUtils.isNetworkAvailable(mContext) && !mIsActive) {
                        initRemoteControl();// 激活
                        mIsActive = true;
                    }
                    break;
                case START_ANSWER_ACTIVITY:
                    CallBundle bundle = (CallBundle) msg.obj;
                    Intent intent = new Intent(AppApplication.getInstance(),
                            RemoteControlActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(FloatingWindowService.FLOATING_ACTION);
                    intent.putExtra(FloatingWindowService.EXTRA_ISCONTROL, true);
                    intent.putExtra(CallBundle.KEY, bundle);
                    AppApplication.getInstance().startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };

    private RemoteControlHandle(Context context) {
        mContext = context.getApplicationContext();
    }

    public static RemoteControlHandle getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new RemoteControlHandle(context);
        }
        return sInstance;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        mLog.d("******Activation is start.*******" + "Thread id" + Thread.currentThread().getId());
        // 如果系统已被激活则不再请求激活
        if (null == intent) {
            return;
        }
        String action = intent.getAction();
        mLog.i("Receiver broadcast: " + action);
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)
                || Constants.STV_ACTION_CONNECTIVITY_CHANGE.equals(action)) {

            mCallHandler.removeMessages(ACTION_ACTIVE);
            mCallHandler.sendEmptyMessage(ACTION_ACTIVE);

        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Constants.TV_ACTION_ON.equals(action)) {
            mCallHandler.removeMessages(ACTION_ACTIVE);
            mCallHandler.sendEmptyMessage(ACTION_ACTIVE);
        }
    }

    private void initRemoteControl() {
        mLog.i("***initRemoteControl***");
        SdkManager.getInstance().initImSdk(mContext, new SdkManager.SDKInit() {
            @Override
            public void onComplete() {
                mLog.d("imsdk init complete");
            }
        });
        // 注册消息总线
        BusProvider.getInstance().register(this);
        mLetvCallManager = LetvCallManager.getInstance();
        mRemoteControlManager = RemoteControlManager.getInstance();
        CallEventManager.getInstance().addCallStopEventListener(this);
        CallEventManager.getInstance().addCallEventErrorListener(this);
        CallEventManager.getInstance().addCallRequestEventListener(this);
    }

    // 远程控制逻辑将阻止部分情况二次来电
    private boolean isRemoteApplyCall(boolean isControl) {
        if (RemoteControlManager.getInstance().isShowing()) {
            // 正在远程控制中,不允许二次来电
            mLog.e("isRemoteApplyCall Remote working ,refuse call in !!!!");
            return false;
        }
        if (isControl) {
            boolean isRemoteActive = RemoteControlManager.getInstance().isShowing();
            if (isRemoteActive) {
                // 通话或控制中不允许远程控制
                mLog.e("isRemoteApplyCall Chatting ,refuse remote control call in!!!! isControl="
                        + isControl + " isCallActive=" + " isRemoteActive=" + isRemoteActive);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onStopEvent(CallStopEvent cse2) {
        // 挂断有可能是第二个电话
        mLog.d("---onStopEvent---callId=" + cse2.callId);
        mCallHandler.removeMessages(START_ANSWER_ACTIVITY);
    }

    @Override
    public void onCallEventError() {
        mCallHandler.removeMessages(START_ANSWER_ACTIVITY);
    }

    @Override
    public void onRequestEvent(final CallSignalStatus cre) {
        mLog.d("onComingCallEvent:" + " callId=" + cre.callId + " caller=" + cre.caller
                + " callerPhone=" + cre.callerPhone + " callerDevid=" + cre.callerDevid
                + " isVideoCall=" + cre.isVideoCall + " isControl=" + cre.isControl);
        ThreadPoolManager.getInstance().getUIThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                mLog.d("isRemoteApplyCall: " + isRemoteApplyCall(cre.isControl));
                if (isRemoteApplyCall(cre.isControl)) {
                    isCallIn = true;
                    bundle = new CallBundle();
                    bundle.callerName = cre.caller;
                    bundle.callId = cre.callId;
                    bundle.callerPhone = cre.callerPhone;
                    bundle.isVideoCall = cre.isVideoCall;
                    bundle.callerDevId = cre.callerDevid != null ? cre.callerDevid : "";
                    bundle.isControl = cre.isControl;
                    bundle.isController = cre.isController;
                    Message msg = new Message();
                    msg.what = START_ANSWER_ACTIVITY;
                    msg.obj = bundle;
                    mCallHandler.sendMessage(msg);
                } else {
                    // 如果忽略掉这个来电，告知sdk，让sdk去处理掉栈里的来电信息
                    String callId = cre.callId;
                    if (!TextUtils.isEmpty(callId)) {
                        if (null != mLetvCallManager) {
                            Call call = mLetvCallManager.getCallById(callId);
                            if (null != call) {
                                try {
                                    JSONObject inner = new JSONObject();
                                    inner.put("reason", "remoteControlling");
                                    inner.put("app", "LetvHelper");
                                    JSONObject json = new JSONObject();
                                    json.put("deny", inner);
                                    mLog.d("deny call reason:" + json.toString());
                                    call.denyCall(json.toString());
                                } catch (org.json.JSONException e) {
                                    mLog.e(e.toString());
                                    e.printStackTrace();
                                }
                                mLog.d("don't allow callin callid:" + callId);
                            } else {
                                mLog.d("mLeCallManager is null don't allow callin callid:"
                                        + callId);
                            }
                        }
                    } else {
                        if (null != mLetvCallManager) {
                            // 挂断之前的
                            Call call = mLetvCallManager.getCurrentCall();
                            if (null != call) {
                                call.reply(false);
                                mLog.d("don't allow callin when callid is null");
                            }
                        } else {
                            mLog.d("mLeCallManager is null don't allow callin when callid is null");
                        }
                    }
                }
            }
        });
    }

    public void stop() {
        BusProvider.getInstance().unregister(this);
        CallEventManager.getInstance().cleanCallEventErrorListener(this);
        CallEventManager.getInstance().cleanCallStopEventListener(this);
        CallEventManager.getInstance().cleanCallRequestEventListener(this);
        mCallHandler.removeCallbacksAndMessages(null);
        destoryAllowDialog();
    }

    private void destoryAllowDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

}
