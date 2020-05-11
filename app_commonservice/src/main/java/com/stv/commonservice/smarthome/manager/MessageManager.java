
package com.stv.commonservice.smarthome.manager;

import android.content.Context;
import android.text.TextUtils;

import com.stv.commonservice.control.window.MotionEventUtils;
import com.stv.commonservice.smarthome.mqtt.EasyMqttService;
import com.stv.commonservice.smarthome.mqtt.IEasyMqttCallBack;
import com.stv.commonservice.smarthome.util.Constant;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.LogUtils;
import com.stv.library.common.util.SystemUtils;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import eui.tv.DesktopManager;

/**
 * 消息管理类
 */
public class MessageManager {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_SMART_HOME, MessageManager.class.getSimpleName());
    private static volatile MessageManager sInstance;
    private Context mContext;
    private EasyMqttService mService;
    private String mElinkId;

    public static MessageManager getInstance(Context context) {
        if (null == sInstance) {
            synchronized (MessageManager.class) {
                if (null == sInstance) {
                    sInstance = new MessageManager(context);
                }
            }
        }
        return sInstance;
    }

    private MessageManager(Context context) {
        this.mContext = context;
    }

    /**
     * 判断服务是否连接
     */
    public boolean isConnected() {
        return mService != null && mService.isConnected();
    }

    /**
     * 发布消息
     */
    private void publish(String msg, String topic, int qos, boolean retained) {
        if (mService != null)
            mService.publish(msg, topic, qos, retained);
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (mService != null)
            mService.disconnect();
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (mService != null)
            mService.close();
    }

    /**
     * 事件上报
     */
    private void eventReport(String msg) {
        mLog.d("Event Report: " + msg);
        if (TextUtils.isEmpty(msg))
            return;
        StringBuilder builder = new StringBuilder();
        builder.append("/sys/event/");
        builder.append(Constant.PRODUCT_KEY + "/");
        builder.append(mElinkId + "/upload");
        mLog.d("topics:" + builder.toString());
        publish(msg, builder.toString(), 0, false);
    }

    /**
     * 订阅主题
     */
    private void subscribe() {
        mLog.d("***subscribe***");
        StringBuilder builder = new StringBuilder();
        builder.append("/sys/property/");
        builder.append(Constant.PRODUCT_KEY + "/");
        builder.append(mElinkId + "/set");
        mLog.d("topics:" + builder.toString());
        mService.subscribe(builder.toString(), 0);
    }

    /**
     * 取消订阅主题
     */
    public void unsubscribe() {
        mLog.d("***unsubscribe***");
        StringBuilder builder = new StringBuilder();
        builder.append("/sys/property/");
        builder.append(Constant.PRODUCT_KEY + "/");
        builder.append(mElinkId + "/set");
        mLog.d("topics:" + builder.toString());
        mService.unsubscribe(builder.toString());
    }

    /**
     * 连接Mqtt服务器
     */
    public void connect() {
        buildEasyMqttService();
        mService.connect(new IEasyMqttCallBack() {
            @Override
            public void messageArrived(String topic, String message, int qos) {
                // 推送消息到达
                mLog.d("messageArrived:(topic:" + topic + "; message:" + message + "; qos:" + qos + ")");
                paramMessage(message);
            }

            @Override
            public void connectionLost(Throwable arg0) {
                // 连接断开
                mLog.w("**Connection Lost**");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken arg0) {
                // 消息提交成功
                mLog.w("Delivery Complete: " + arg0.getMessageId());
            }

            @Override
            public void connectSuccess(IMqttToken arg0) {
                // 连接成功
                mLog.i("Connect Success:(MQTT MessageId:" + arg0.getMessageId() + ")");
                subscribe();
            }

            @Override
            public void connectFailed(IMqttToken arg0, Throwable arg1) {
                // 连接失败
                mLog.w("Connect Failed:(MessageId:" + arg0.getMessageId() + "; Throwable:" + arg1.getMessage() + ")");
            }
        });
    }

    /**
     * 构建EasyMqttService对象
     */
    private void buildEasyMqttService() {
        mElinkId = DataPref.getInstance(mContext).getStringPreferences(Constant.KEY_ELINK_ID, "");
        String deviceSecret = DataPref.getInstance(mContext).getStringPreferences(Constant.KEY_DEVICE_SECRET, "");
        mLog.d("MQTT connect, ElinkId: " + mElinkId + " ;Device Secret: " + deviceSecret);
        mService = new EasyMqttService.Builder()
                .autoReconnect(true)// 设置自动重连
                .cleanSession(false) // 设置不清除回话session 可收到服务器之前发出的推送消息
                .clientId(mElinkId)
                .serverUrl(Constant.getMQTTUrl(mContext))
                .userName(mElinkId)
                .passWord(deviceSecret)
                .keepAliveInterval(20) // 心跳包默认的发送间隔
                .bulid(mContext.getApplicationContext());
    }

    /**
     * 解析服务器下发的指令，并且上报结果
     * @param msg
     */
    private void paramMessage(String msg) {
        mLog.d("paramMessage: " + msg);
        if (TextUtils.isEmpty(msg))
            return;
        try {
            JSONArray dataArray = new JSONArray();
            JSONObject dataObject;
            JSONObject json = new JSONObject(msg);
            JSONArray jsonArray = json.getJSONArray("params");
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    Iterator<String> it = object.keys();
                    while (it.hasNext()) {
                        // 获得key
                        String key = it.next();
                        String value = object.getString(key);
                        mLog.d("key: " + key + ";  value: " + value);
                        switch (key) {
                            case "action_key_event":// 发送按键指令
                                MotionEventUtils.getInstance().sendKeyEvent(Integer.parseInt(value));
                                break;
                            case "action_switch_signal":// 切换信号源
                                DesktopManager.switchToSignalDesktop(value);
                                break;
                            case "action_start_application":// 打开指定的应用
                                SystemUtils.startApplication(mContext, value);
                                break;
                        }
                        dataObject = new JSONObject();
                        dataObject.put("event", key);
                        dataObject.put("type", "smart_home");
                        dataObject.put("message", "MQTT Message");
                        dataObject.put("value", String.valueOf(value));
                        dataArray.put(dataObject);

                    }

                }
            }

            if (dataArray.length() > 0) {
                JSONObject reportObject = new JSONObject();
                reportObject.put("requestId", json.getString("requestId"));
                reportObject.put("elinkId", json.getString("elinkId"));
                reportObject.put("timestamp", json.getLong("timestamp"));
                reportObject.put("data", dataArray);
                eventReport(reportObject.toString());
            }

        } catch (Exception e) {
            mLog.e("param Message error:" + e.getMessage());
            e.printStackTrace();
        }

    }

}
