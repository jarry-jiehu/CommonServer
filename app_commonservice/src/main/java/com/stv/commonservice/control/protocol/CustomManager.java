
package com.stv.commonservice.control.protocol;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.stv.commonservice.control.manager.CallEventManager;
import com.stv.commonservice.control.protocol.bean.ControlInfoBean;
import com.stv.commonservice.control.protocol.bean.CustomBean;
import com.stv.commonservice.control.protocol.bean.MotionEventBean;
import com.stv.commonservice.control.protocol.bean.RequestParamBean;
import com.stv.commonservice.control.protocol.bean.SwitchEventBean;
import com.stv.commonservice.control.protocol.listener.AppCustomEventListener;
import com.stv.commonservice.control.protocol.listener.BrashSwitchListener;
import com.stv.commonservice.control.protocol.listener.DataChannelListener;
import com.stv.commonservice.control.protocol.listener.MotionListener;
import com.stv.commonservice.control.protocol.listener.PcModeChangedListener;
import com.stv.commonservice.control.protocol.listener.RequestModuleListener;
import com.stv.commonservice.control.protocol.listener.RequestResultListener;
import com.stv.commonservice.control.protocol.listener.SwitchPositonListener;
import com.stv.commonservice.control.protocol.receiver.StopRemoteReceiver;
import com.stv.commonservice.control.util.HomeWatcher;
import com.stv.commonservice.control.window.MotionEventUtils;
import com.stv.videochatsdk.api.Call;
import com.stv.videochatsdk.api.LetvCallManager;
import com.stv.videochatsdk.api.event.AppCustomEvent;
import com.stv.videochatsdk.api.event.DataChannelMessageEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 负责接收Custom消息生成对应事件
 */
public class CustomManager implements AppCustomEventListener, DataChannelListener {
    private static final String TAG = CustomManager.class.getSimpleName();
    private List<MotionListener> mTouchEventList;
    private List<BrashSwitchListener> mBrashSwitchList;
    private List<RequestModuleListener> mRequestModuleList;
    private List<StopRemoteReceiver> mStopRemoteList;
    private List<PcModeChangedListener> mPcModeList;
    private List<SwitchPositonListener> mSwitchPositonList;
    private static CustomManager instance;
    private final Map<String, Boolean> mEnableMap;
    private ShowRequestModeListener mShowRequestModeListener;

    public static CustomManager getInstance() {
        if (null == instance) {
            synchronized (CustomManager.class) {
                if (null == instance) {
                    instance = new CustomManager();
                }
            }
        }
        return instance;
    }

    private CustomManager() {
        CallEventManager.getInstance().cleanAppCustomEventListener(this);
        CallEventManager.getInstance().addAppCustomEventListener(this);
        CallEventManager.getInstance().cleanDataChannelListener(this);
        CallEventManager.getInstance().addDataChannelListener(this);
        mTouchEventList = Collections.synchronizedList(new ArrayList<MotionListener>());
        // mBrashSwitchList = Collections.synchronizedList(new
        // ArrayList<BrashSwitchListener>());
        // mRequestModuleList = Collections.synchronizedList(new
        // ArrayList<RequestModuleListener>());
        mStopRemoteList = Collections.synchronizedList(new ArrayList<StopRemoteReceiver>());
        mSwitchPositonList = Collections.synchronizedList(new ArrayList<SwitchPositonListener>());
        // mPcModeList = Collections.synchronizedList(new
        // ArrayList<PcModeChangedListener>());
        mEnableMap = new HashMap<String, Boolean>();
        initMap();
    }

    private void initMap() {
        // 不支持多路解码的,不支持远程控制
        mEnableMap.put(RequestParamBean.KEY_BE_CONTROLLED, true);
        mEnableMap.put(RequestParamBean.KEY_CHAT, true);
        mEnableMap.put(RequestParamBean.KEY_CONTROL, false);
        mEnableMap.put(RequestParamBean.KEY_IM, false);
    }

    public void release() {
        CallEventManager.getInstance().cleanAppCustomEventListener(this);
        CallEventManager.getInstance().cleanDataChannelListener(this);
    }

    private void doDecodeCustom(String data, String id) {
        if (TextUtils.isEmpty(data)) {
            return;
        }
        Gson gson = new Gson();
        CustomBean customBean = gson.fromJson(data, CustomBean.class);
        if (null != customBean && !TextUtils.isEmpty(customBean.getData())) {
            Log.d(TAG, "doDecodeCustom type=" + customBean.getType());
            if (CustomBean.CONTROL.equals(customBean.getType())) {
                // 远程控制信息
                ControlInfoBean controlInfoBean = gson.fromJson(customBean.getData(),
                        ControlInfoBean.class);
                if (null != controlInfoBean) {
                    onControlCommand(controlInfoBean);
                }
            } else if (CustomBean.REQUEST_PARAM.equals(customBean.getType())) {
                RequestParamBean requestBean = gson.fromJson(customBean.getData(),
                        RequestParamBean.class);
                if (requestBean.isRequest()) {
                    // 功能询问信息
                    onRequestParam(customBean, requestBean, id);
                } else {
                    // 答复功能询问
                }
            } else if (CustomBean.REQUEST_MODULE.equals(customBean.getType())) {
                RequestParamBean requestBean = gson.fromJson(customBean.getData(),
                        RequestParamBean.class);
                if (requestBean.isRequest()) {
                    // 请求对应功能,例如请求进行协助
                    if (requestBean.isRequest()) {
                        // 对方的请求
                        onRequestModule(customBean, requestBean, id);
                    } else {
                        // 己方请求,对方的回复
                    }
                } else {
                    // 答复是否同意
                }
            }
        }
    }

    public void onRequestModule(CustomBean customBean, RequestParamBean bean, String callId) {
        if (null != customBean && null != bean && null != bean.getParam()) {
            Map<String, Boolean> tempMap = bean.getParam();
            Set<Map.Entry<String, Boolean>> set = tempMap.entrySet();
            ArrayList<RequestModuleListener> list = new ArrayList<RequestModuleListener>(
                    mRequestModuleList);
            Log.d(TAG, "map size=" + tempMap.size() + " list size=" + list.size());
            for (Map.Entry<String, Boolean> entry : set) {
                Log.d(TAG, "entry=" + entry.getKey());
                // true为请求,false为取消
                boolean isCancel = !entry.getValue();
                if (entry.getKey().equals(RequestParamBean.KEY_CONTROL)) {
                    mShowRequestModeListener = new ShowRequestModeListener(customBean, callId);
                    for (RequestModuleListener listener : list) {
                        if (null != listener) {
                            listener.onRequestControl(mShowRequestModeListener, isCancel);
                        }
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void onDataChannelEvent(DataChannelMessageEvent event) {
        if (null != event) {
            Log.d(TAG, "onDataChannelEvent even=" + event.data);
            doDecodeCustom(event.data, event.callId);
        } else {
            Log.d(TAG, "onDataChannelEvent even=" + event);
        }
    }

    private class ShowRequestModeListener implements RequestResultListener {
        private CustomBean mCustomBean;
        private String mCallId;

        public ShowRequestModeListener(CustomBean customBean, String callId) {
            this.mCustomBean = customBean;
            this.mCallId = callId;
        }

        @Override
        public void onResponse(boolean isAllow) {
            Log.d(TAG, "ShowRequestModeListener onResponse isAllow=" + isAllow);
            if (!TextUtils.isEmpty(mCallId)) {
                Call call = LetvCallManager.getInstance().getCallById(mCallId);
                if (null != call && null != mCustomBean) {
                    Gson gson = new Gson();
                    RequestParamBean requestBean = gson.fromJson(mCustomBean.getData(),
                            RequestParamBean.class);
                    if (null != requestBean && null != requestBean.getParam()) {
                        requestBean.getParam().put(RequestParamBean.KEY_CONTROL, isAllow);
                        requestBean.setRequest(false);
                        mCustomBean.setData(gson.toJson(requestBean));
                        String data = gson.toJson(mCustomBean);
                        Log.d(TAG, "data=" + data);
                        call.sendAppCustom(data);
                    }
                }
            }
        }
    }

    public void onRequestParam(CustomBean customBean, RequestParamBean bean, String mCallId) {
        if (null != bean && null != bean.getParam()) {
            Gson gson = new Gson();
            Set<Map.Entry<String, Boolean>> set = bean.getParam().entrySet();
            for (Map.Entry<String, Boolean> entry : set) {
                if (mEnableMap.containsKey(entry.getKey())) {
                    bean.getParam().put(entry.getKey(), mEnableMap.get(entry.getKey()));
                } else {
                    bean.getParam().put(entry.getKey(), false);
                }
            }
            bean.setRequest(false);
            customBean.setData(gson.toJson(bean));
            Call call;
            if (!TextUtils.isEmpty(mCallId)) {
                call = LetvCallManager.getInstance().getCallById(mCallId);
                if (null != call) {
                    String data = gson.toJson(customBean);
                    Log.d(TAG, "onRequestParam sendAppCustom=" + data);
                    call.sendAppCustom(data);
                }
            }

        }

    }

    private void onControlCommand(ControlInfoBean bean) {
        Log.d(TAG, "onControlCommand type=" + bean.getType());
        if (ControlInfoBean.ACTION_KEY_EVENT.equals(bean.getType())) {
            // 按键事件
            onKeyEvent(bean);
        } else if (ControlInfoBean.ACTION_TOUCH_EVENT.equals(bean.getType())) {
            // 触屏事件
            onTouchEvent(bean);
        } else if (ControlInfoBean.ACTION_BRUSH_SWITCH.equals(bean.getType())) {
            // 画笔模式切换开关
            onChangeMode(bean);
        } else if (ControlInfoBean.ACTION_BRUSH_EVENT.equals(bean.getType())) {
            // 画笔触屏事件处理
            onTouchEvent(bean);
        } else if (ControlInfoBean.ACTION_PC_MODE.equals(bean.getType())) {
            // PC控制模式切换
            onPCModeChanged(bean);
        } else if (ControlInfoBean.ACTION_SWITCH_EVENT.equals(bean.getType())) {
            onSwitchPositionEvent(bean);
        }
    }

    private void onPCModeChanged(ControlInfoBean bean) {
        if (null != bean) {
            try {
                Log.d(TAG, "onPCModeChanged isPcMode=" + bean.getEventStr());
                boolean isPcMode = Boolean.parseBoolean(bean.getEventStr());
                ArrayList<PcModeChangedListener> list = new ArrayList<PcModeChangedListener>(
                        mPcModeList);
                for (PcModeChangedListener listener : list) {
                    if (null != listener) {
                        Log.d(TAG, "onPCModeChanged!!! isPcMode=" + isPcMode);
                        listener.onPcModeChanged(isPcMode);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    private void onChangeMode(ControlInfoBean bean) {
        if (null != bean) {
            ArrayList<BrashSwitchListener> list = new ArrayList<BrashSwitchListener>(
                    mBrashSwitchList);
            for (BrashSwitchListener listener : list) {
                if (null != listener) {
                    listener.changBrashType(bean.isBrush());
                }
            }
        }
    }

    private void onKeyEvent(ControlInfoBean bean) {
        if (null != bean) {
            try {
                int keyCode = Integer.parseInt(bean.getEventStr());
                MotionEventUtils.getInstance().sendKeyEvent(keyCode);
                for (StopRemoteReceiver receiver : mStopRemoteList) {
                    if (null != receiver) {
                        receiver.onRemoteKeyPressed(keyCode);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    private void onTouchEvent(ControlInfoBean bean) {
        try {
            Gson gson = new Gson();
            MotionEventBean event = gson.fromJson(bean.getEventStr(), MotionEventBean.class);
            float x = event.getX() / bean.getWidth() * MotionEventUtils.getInstance().mScreenWidth;
            float y = event.getY() / bean.getHeight()
                    * MotionEventUtils.getInstance().mScreenHeight;
            event.setX(x);
            event.setY(y);
            Log.d(TAG, " x:" + x + ",y:" + y);
            ArrayList<MotionListener> list = new ArrayList<MotionListener>(mTouchEventList);
            for (MotionListener listener : list) {
                if (null != listener) {
                    listener.onTouch(event);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    private void onSwitchPositionEvent(ControlInfoBean bean) {
        try {
            Gson gson = new Gson();
            SwitchEventBean eventBean = gson.fromJson(bean.getEventStr(), SwitchEventBean.class);
            ArrayList<SwitchPositonListener> list = new ArrayList<SwitchPositonListener>(
                    mSwitchPositonList);
            for (SwitchPositonListener listener : list) {
                if (null != listener) {
                    listener.onSwitch(eventBean);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onAppCustomEvent(AppCustomEvent event) {
        Log.d(TAG, "onAppCustomEvent even=" + event.data);
        if (null != event) {
            doDecodeCustom(event.data, event.callId);
        }
    }

    public void addTouchListener(MotionListener listener) {
        if (null != listener && !mTouchEventList.contains(listener)) {
            mTouchEventList.add(listener);
        }
    }

    public void removeTouchListener(MotionListener listener) {
        if (null != listener && mTouchEventList.contains(listener)) {
            mTouchEventList.remove(listener);
        }
    }

    public void addBrashSwitchListener(BrashSwitchListener listener) {
        if (null != listener && !mBrashSwitchList.contains(listener)) {
            mBrashSwitchList.add(listener);
        }
    }

    public void removeBrashSwitchListener(BrashSwitchListener listener) {
        if (null != listener && mBrashSwitchList.contains(listener)) {
            mBrashSwitchList.remove(listener);
        }
    }

    public void addRequestModuleListener(RequestModuleListener listener) {
        if (null != listener && !mRequestModuleList.contains(listener)) {
            mRequestModuleList.add(listener);
        }
    }

    public void removeRequestModuleListener(RequestModuleListener listener) {
        if (null != listener && mRequestModuleList.contains(listener)) {
            mRequestModuleList.remove(listener);
        }
    }

    public void addStopRemoteListener(StopRemoteReceiver listener) {
        if (null != listener && !mStopRemoteList.contains(listener)) {
            HomeWatcher.getInstance().addListener(listener);
            mStopRemoteList.add(listener);
        }
    }

    public void removeStopRemoteListener(StopRemoteReceiver listener) {
        if (null != listener && mStopRemoteList.contains(listener)) {
            HomeWatcher.getInstance().removeListener(listener);
            mStopRemoteList.remove(listener);
        }
    }

    public void addSwitchPositionListener(SwitchPositonListener listener) {
        if (null != listener && !mSwitchPositonList.contains(listener)) {
            mSwitchPositonList.add(listener);
        }
    }

    public void removeSwitchPositionListener(SwitchPositonListener listener) {
        if (null != listener && mSwitchPositonList.contains(listener)) {
            mSwitchPositonList.remove(listener);
        }
    }

    public void addPcModeChangedListener(PcModeChangedListener listener) {
        if (null != listener && !mPcModeList.contains(listener)) {
            mPcModeList.add(listener);
        }
    }

    public void removePcModeChangedListener(PcModeChangedListener listener) {
        if (null != listener && mPcModeList.contains(listener)) {
            mPcModeList.remove(listener);
        }
    }
}
