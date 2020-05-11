
package com.stv.commonservice.control.manager;

import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.stv.commonservice.R;
import com.stv.commonservice.common.AppApplication;
import com.stv.commonservice.control.protocol.listener.AppCustomEventListener;
import com.stv.commonservice.control.protocol.listener.CallEventErrorListener;
import com.stv.commonservice.control.protocol.listener.CallEventListener;
import com.stv.commonservice.control.protocol.listener.CallInternalErrorListen;
import com.stv.commonservice.control.protocol.listener.CallRequestEventListener;
import com.stv.commonservice.control.protocol.listener.CallStopEventListener;
import com.stv.commonservice.control.protocol.listener.DataChannelListener;
import com.stv.videochatsdk.api.event.AppCustomEvent;
import com.stv.videochatsdk.api.event.CallInternalErrorEvent;
import com.stv.videochatsdk.api.event.CallSignalStatus;
import com.stv.videochatsdk.api.event.CallSingalErrorEvent;
import com.stv.videochatsdk.api.event.CallStopEvent;
import com.stv.videochatsdk.api.event.DataChannelMessageEvent;
import com.stv.videochatsdk.api.event.SignalTimeoutEvent;
import com.stv.videochatsdk.util.BusProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件管理类，此类处理跟videochatSdk的事件
 */
public class CallEventManager {
    private final String TAG = CallEventManager.class.getSimpleName();
    private volatile static CallEventManager mCallEventManager = null;
    private List<CallEventListener> mCallEventListenerList;
    /**
     * 來電接听的监听的回调集合
     */
    private List<CallRequestEventListener> mCallRequestEventListenerList;
    private List<CallStopEventListener> mCallStopEventListenerList;
    private List<CallEventErrorListener> mCallEventErrorListenerList;
    /**
     * 收集所有停止通话事件的集合
     */
    private List<CallStopEvent> mCallStopEventList;
    /**
     * 内部错误上报
     */
    private List<CallInternalErrorListen> mCallInternalErrorListens;
    private List<AppCustomEventListener> mAppCustomEventListener;
    private List<DataChannelListener> mDataChannelListenerList;

    public List<CallStopEvent> getCallStopEventList() {
        return mCallStopEventList;
    }

    private CallEventManager() {
        mCallEventListenerList = new ArrayList<CallEventListener>();
        mCallRequestEventListenerList = new ArrayList<CallRequestEventListener>();
        mCallStopEventListenerList = new ArrayList<CallStopEventListener>();
        mCallEventErrorListenerList = new ArrayList<CallEventErrorListener>();
        mCallStopEventList = new ArrayList<CallStopEvent>();
        mCallInternalErrorListens = new ArrayList<CallInternalErrorListen>();
        mAppCustomEventListener = new ArrayList<AppCustomEventListener>();
        mDataChannelListenerList = new ArrayList<DataChannelListener>();
        regBusProvider();
    }

    public static CallEventManager getInstance() {
        if (null == mCallEventManager) {
            synchronized (CallEventManager.class) {
                if (null == mCallEventManager) {
                    mCallEventManager = new CallEventManager();
                }

            }
        }
        return mCallEventManager;
    }

    /**
     * 注册消息总线
     */
    private void regBusProvider() {
        Log.d(TAG, "==regBusProvider==");
        BusProvider.getInstance().register(CallEventManager.this); // 注册消息总线
    }

    public void addCallEventListener(CallEventListener callEventListener) {
        mCallEventListenerList.add(callEventListener);
    }

    public void cleanCallEventListener(CallEventListener callEventListener) {
        mCallEventListenerList.remove(callEventListener);
    }

    public void addCallRequestEventListener(CallRequestEventListener callRequestEventListener) {
        this.mCallRequestEventListenerList.add(callRequestEventListener);
    }

    public void cleanCallRequestEventListener(CallRequestEventListener callRequestEventListener) {
        mCallRequestEventListenerList.remove(callRequestEventListener);
    }

    public void addCallStopEventListener(CallStopEventListener callStopEventListener) {
        this.mCallStopEventListenerList.add(callStopEventListener);
    }

    public void cleanCallStopEventListener(CallStopEventListener callStopEventListener) {
        mCallStopEventListenerList.remove(callStopEventListener);
    }

    public void addCallEventErrorListener(CallEventErrorListener callEventErrorListener) {
        this.mCallEventErrorListenerList.add(callEventErrorListener);
    }

    public void cleanCallEventErrorListener(CallEventErrorListener callEventErrorListener) {
        mCallEventErrorListenerList.remove(callEventErrorListener);
    }

    public void addCallInternalErrorListener(CallInternalErrorListen CallInternalErrorListen) {
        mCallInternalErrorListens.add(CallInternalErrorListen);
    }

    public void cleanCallInternalErrorListener(CallInternalErrorListen CallInternalErrorListen) {
        mCallInternalErrorListens.remove(CallInternalErrorListen);
    }

    public void addAppCustomEventListener(AppCustomEventListener AppCustomEventListener) {
        mAppCustomEventListener.add(AppCustomEventListener);
    }

    public void cleanAppCustomEventListener(AppCustomEventListener AppCustomEventListener) {
        mAppCustomEventListener.remove(AppCustomEventListener);
    }

    public void addDataChannelListener(DataChannelListener DataChannelListener) {
        mDataChannelListenerList.add(DataChannelListener);
    }

    public void cleanDataChannelListener(DataChannelListener DataChannelListener) {
        mDataChannelListenerList.remove(DataChannelListener);
    }

    @Subscribe
    public void onRequestEvent(CallSignalStatus cre) {
        synchronized (TAG) {
            Log.d(TAG, "onRequestEvent,  caller: " + cre.caller + ", id: " + cre.callId + ", phone: " +
                    cre.callerPhone + ", device: "
                    + cre.callerDevid);
            if (mCallRequestEventListenerList != null && mCallRequestEventListenerList.size() > 0) {
                ArrayList<CallRequestEventListener> list = new ArrayList<CallRequestEventListener>();
                list.addAll(mCallRequestEventListenerList);
                for (CallRequestEventListener listener : list)
                    listener.onRequestEvent(cre);
            }
        }
    }

    @Subscribe
    public void onStopEvent(CallStopEvent cse2) {
        synchronized (TAG) {
            Log.d(TAG, "onStopEvent->" + cse2.callId);
            mCallStopEventList.add(cse2);
            if (mCallStopEventListenerList != null && mCallStopEventListenerList.size() > 0) {
                ArrayList<CallStopEventListener> list = new ArrayList<CallStopEventListener>();
                list.addAll(mCallStopEventListenerList);
                for (CallStopEventListener listener : list)
                    listener.onStopEvent(cse2);
            }
        }
    }

    @Subscribe
    public void onSignalTimeoutEvent(SignalTimeoutEvent event) {
        Log.d(TAG, "onSignalTimeoutEvent->" + "signal time out" + event.callId);
        Toast.makeText(AppApplication.getInstance(), R.string.toast_network_disconnect, Toast.LENGTH_LONG).show();
        if (mCallEventErrorListenerList != null && mCallEventErrorListenerList.size() > 0) {
            ArrayList<CallEventErrorListener> list = new ArrayList<CallEventErrorListener>();
            list.addAll(mCallEventErrorListenerList);
            for (CallEventErrorListener listener : list)
                listener.onCallEventError();
        }
    }

    @Subscribe
    public void onCallSingalError(CallSingalErrorEvent cse) {
        Log.d(TAG, "==CallSingalErrorEvent==" + cse.reason);
        Toast.makeText(AppApplication.getInstance(), R.string.toast_network_disconnect, Toast.LENGTH_LONG).show();
        if (mCallEventErrorListenerList != null && mCallEventErrorListenerList.size() > 0) {
            ArrayList<CallEventErrorListener> list = new ArrayList<CallEventErrorListener>();
            list.addAll(mCallEventErrorListenerList);
            for (CallEventErrorListener listener : list)
                listener.onCallEventError();
        }
    }

    @Subscribe
    public void onCallResultEvent(CallSignalStatus cre) {
        Log.d(TAG, "==onCallResultEvent==" + cre.toString() + " ;Result Type: " + cre.getType());
        if (CallSignalStatus.Type.CALL_RESULT_EVENT == cre.getType()) {
            if (mCallEventErrorListenerList != null && mCallEventErrorListenerList.size() > 0) {
                ArrayList<CallEventErrorListener> list = new ArrayList<CallEventErrorListener>();
                list.addAll(mCallEventErrorListenerList);
                for (CallEventErrorListener listener : mCallEventErrorListenerList)
                    listener.onCallEventError();
            }
        }
    }

    @Subscribe
    public void SignalTimeoutEvent(SignalTimeoutEvent event) {
        Log.i(TAG, "==SignalTimeoutEvent==" + event.toString());
        if (mCallEventListenerList != null && mCallEventListenerList.size() > 0) {
            ArrayList<CallEventListener> list = new ArrayList<CallEventListener>();
            list.addAll(mCallEventListenerList);
            for (CallEventListener listener : list)
                listener.onSignalTimeoutEvent(event);
        }
    }

    @Subscribe
    public void onCallInternalErrorEvent(CallInternalErrorEvent event) {
        if (mCallInternalErrorListens != null && mCallInternalErrorListens.size() > 0) {
            ArrayList<CallInternalErrorListen> list = new ArrayList<CallInternalErrorListen>();
            list.addAll(mCallInternalErrorListens);
            for (CallInternalErrorListen listener : list)
                listener.onCallInternalErrorEvent(event);
        }
    }

    @Subscribe
    public void onAppCustomEvent(AppCustomEvent event) {
        Log.d(TAG, "onAppCustomEvent    event=" + event.toString());
        if (mAppCustomEventListener != null && mAppCustomEventListener.size() > 0) {
            ArrayList<AppCustomEventListener> list = new ArrayList<AppCustomEventListener>();
            list.addAll(mAppCustomEventListener);
            for (AppCustomEventListener listener : list) {
                listener.onAppCustomEvent(event);
            }
        }
    }

    @Subscribe
    public void onDataChannelEvent(DataChannelMessageEvent event) {
        Log.d(TAG, "onDataChannelEvent    event=" + event.toString());
        if (mDataChannelListenerList != null && mDataChannelListenerList.size() > 0) {
            ArrayList<DataChannelListener> list = new ArrayList<DataChannelListener>();
            list.addAll(mDataChannelListenerList);
            for (DataChannelListener listener : list) {
                listener.onDataChannelEvent(event);
            }
        }
    }
}
