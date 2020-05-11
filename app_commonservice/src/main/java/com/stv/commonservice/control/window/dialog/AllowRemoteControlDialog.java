
package com.stv.commonservice.control.window.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.stv.commonservice.R;
import com.stv.commonservice.common.AppApplication;
import com.stv.commonservice.control.manager.CallEventManager;
import com.stv.commonservice.control.protocol.bean.CallBundle;
import com.stv.commonservice.control.protocol.listener.CallEventListener;
import com.stv.commonservice.control.protocol.listener.CallStopEventListener;
import com.stv.commonservice.control.service.FloatingWindowService;
import com.stv.commonservice.control.util.ToastUtil;
import com.stv.videochatsdk.api.Call;
import com.stv.videochatsdk.api.LetvCallManager;
import com.stv.videochatsdk.api.event.CallStopEvent;
import com.stv.videochatsdk.api.event.SignalTimeoutEvent;

public class AllowRemoteControlDialog extends AlertDialog {
    private static final String TAG = AllowRemoteControlDialog.class.getSimpleName();
    static final int DISMISS_TIMEOUT = 1000 * 60 * 1;
    private Context mContext;
    private LetvCallManager mLeCallManager = null;
    private String callId = null;
    private String callerPhone = null;
    private boolean isVideoCall = false;
    private String callerDevid = null;
    private boolean isPhoneCall = false;
    private CallBundle mCallBundle;
    private Call mCall = null;
    private boolean isControl = false;

    public AllowRemoteControlDialog(Context context, CallBundle bundle) {
        super(context);
        mContext = context;
        mCallBundle = bundle;
        String text = getTitle(bundle);
        setTitle(text);
        View view = getLayoutInflater().inflate(R.layout.view_dialog, null);
        // 把自定义的布局设置到dialog中，注意，布局设置一定要在show之前。从第二个参数分别填充内容与边框之间左、上、右、下、的像素
        setView(view, 0, 0, 0, 0);
        // 一定要先show出来再设置dialog的参数，不然就不会改变dialog的大小了
        // 得到当前显示设备的宽度，单位是像素
        int width = 1920;
        // 得到这个dialog界面的参数对象
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        // 设置dialog的界面宽度
        params.width = width - (width / 6);
        // 设置dialog高度为包裹内容
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        // 设置dialog的重心
        params.gravity = Gravity.CENTER;
        // dialog.getWindow().setLayout(width-(width/6), LayoutParams.WRAP_CONTENT);
        // 用这个方法设置dialog大小也可以，但是这个方法不能设置重心之类的参数，推荐用Attributes设置
        // 最后把这个参数对象设置进去，即与dialog绑定
        this.getWindow().setAttributes(params);
        Button leftButton = (Button) view.findViewById(R.id.splash_dialog_left);
        Button rightButton = (Button) view.findViewById(R.id.splash_dialog_right);
        TextView warnMessage = (TextView) view.findViewById(R.id.warnmessage);
        warnMessage.setText(context.getResources().getString(R.string.request_dialog_message));
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerCall(v);
                dismiss();
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hangup();
                cancel();
            }
        });
        initListener(bundle);
    }

    @Override
    public void cancel() {
        super.cancel();
        removeListener();
    }

    private String getTitle(CallBundle callBundle) {
        if (null == callBundle) {
            return "";
        } else {
            String name = null;
            name = AppApplication.getInstance().getResources()
                    .getString(R.string.request_dialog_title);
            return name;
        }
    }

    private void initListener(CallBundle bundle) {
        mLeCallManager = LetvCallManager.getInstance();
        processBundleExtra(bundle);
        mCall = mLeCallManager.getCallById(callId);
        CallEventManager.getInstance().addCallStopEventListener(mCallStopEventListener);
        CallEventManager.getInstance().addCallEventListener(mCallEventListener);
    }

    private void answerCall(View v) {
        FloatingWindowService.setControlCallType(isControl);
        if (isControl) {
            startControl(v);
        }
        cancel();
    }

    private void startControl(View v) {
        Intent intent = new Intent(AppApplication.getInstance(), FloatingWindowService.class);
        intent.setAction(FloatingWindowService.FLOATING_ACTION);
        intent.putExtra(FloatingWindowService.EXTRA_ISCONTROL, true);
        intent.putExtra(CallBundle.KEY, mCallBundle);
        AppApplication.getInstance().startService(intent);
        // RemoteControlManager.getInstance().onDirectControlEvent(mCallBundle);
    }

    private void hangup() {
        if (!TextUtils.isEmpty(callId)) {
            mCall = mLeCallManager.getCallById(callId);
            if (null != mCall) {
                mCall.reply(false);
            }
        } else {
            // 挂断之前的
            mCall = mLeCallManager.getCurrentCall();
            if (null != mCall) {
                mCall.reply(false);
            }
        }
        cancel();
    }

    private void processBundleExtra(CallBundle bundle) {
        if (null != bundle) {
            callId = bundle.callId;
            callerPhone = bundle.callerPhone;
            isVideoCall = bundle.isVideoCall;
            callerDevid = bundle.callerDevId;
            isPhoneCall = bundle.isPhoneCall;
            isControl = bundle.isControl;
            mCall = mLeCallManager.getCallById(callId);
            Log.d(TAG,
                    "requestCallInfo==>" + "callId:" + callId + ",callerPhone:" + callerPhone
                            + ",isVideoCall:" + isVideoCall + ",callerDevid:" + callerDevid
                            + ",isPhoneCall:" + isPhoneCall);
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
                        ToastUtil.getInstance().showInThread(R.string.toast_connect_break,
                                Toast.LENGTH_SHORT);
                        Log.d(TAG, "BREAKE");
                        break;
                    case HUNGUP:
                        // 对方挂断
                        ToastUtil.getInstance().showInThread(R.string.toast_respone_hungup,
                                Toast.LENGTH_SHORT);
                        Log.d(TAG, "HUNGUP");
                        break;
                    case CANCEL:
                        // 已被应答
                        Log.d(TAG, "CANCEL");
                        ToastUtil.getInstance().showInThread(R.string.toast_respone_dealcall,
                                Toast.LENGTH_SHORT);
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
                cancel();
            }
        }
    };

    private CallEventListener mCallEventListener = new CallEventListener() {
        @Override
        public void onSignalTimeoutEvent(SignalTimeoutEvent event) {
            ToastUtil.getInstance().showInThread(R.string.toast_connect_break, Toast.LENGTH_SHORT);
            if (null != mCall && !TextUtils.isEmpty(mCall.callId)
                    && mCall.callId.equals(event.callId)) {
                cancel();
            }
        }
    };

    private void removeListener() {
        CallEventManager.getInstance().cleanCallStopEventListener(mCallStopEventListener);
        CallEventManager.getInstance().cleanCallEventListener(mCallEventListener);
    }
}
