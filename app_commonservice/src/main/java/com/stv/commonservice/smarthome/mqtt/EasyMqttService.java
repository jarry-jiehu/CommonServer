
package com.stv.commonservice.smarthome.mqtt;

import android.content.Context;

import com.stv.commonservice.util.LogUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Mqtt服务
 */
public class EasyMqttService {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_SMART_HOME, EasyMqttService.class.getSimpleName());
    private boolean canDoConnect = true;

    private MqttAndroidClient client;
    private MqttConnectOptions conOpt;

    private Context context;
    private String serverUrl = "";
    private String userName = "admin";
    private String passWord = "password";
    private String clientId = "";
    private int timeOut = 10;
    private int keepAliveInterval = 20;
    private boolean retained = false;
    private boolean cleanSession = false;
    private boolean autoReconnect = true;
    private IEasyMqttCallBack starMQTTCallBack;

    /**
     * builder设计模式
     * @param builder
     */
    private EasyMqttService(Builder builder) {
        this.context = builder.context;
        this.serverUrl = builder.serverUrl;
        this.userName = builder.userName;
        this.passWord = builder.passWord;
        this.clientId = builder.clientId;
        this.timeOut = builder.timeOut;
        this.keepAliveInterval = builder.keepAliveInterval;
        this.retained = builder.retained;
        this.cleanSession = builder.cleanSession;
        this.autoReconnect = builder.autoReconnect;

        init();
    }

    /**
     * Builder 构造类
     */
    public static final class Builder {

        private Context context;
        private String serverUrl;
        private String userName = "admin";
        private String passWord = "password";
        private String clientId;
        private int timeOut = 10;
        private int keepAliveInterval = 20;
        private boolean retained = false;
        private boolean cleanSession = false;
        private boolean autoReconnect = false;

        public Builder serverUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder passWord(String passWord) {
            this.passWord = passWord;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder timeOut(int timeOut) {
            this.timeOut = timeOut;
            return this;
        }

        public Builder keepAliveInterval(int keepAliveInterval) {
            this.keepAliveInterval = keepAliveInterval;
            return this;
        }

        public Builder retained(boolean retained) {
            this.retained = retained;
            return this;
        }

        public Builder autoReconnect(boolean autoReconnect) {
            this.autoReconnect = autoReconnect;
            return this;
        }

        public Builder cleanSession(boolean cleanSession) {
            this.cleanSession = cleanSession;
            return this;
        }

        public EasyMqttService bulid(Context context) {
            this.context = context;
            return new EasyMqttService(this);
        }
    }

    /**
     * 发布消息
     * @param msg
     * @param topic
     * @param qos
     * @param retained
     */
    public void publish(String msg, String topic, int qos, boolean retained) {
        try {
            client.publish(topic, msg.getBytes(), qos, retained);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        // 服务器地址（协议+地址+端口号）
        client = new MqttAndroidClient(context, serverUrl, clientId);
        // 设置MQTT监听并且接受消息
        client.setCallback(mqttCallback);

        conOpt = new MqttConnectOptions();
        // 清除缓存
        conOpt.setCleanSession(cleanSession);
        // 设置超时时间，单位：秒
        conOpt.setConnectionTimeout(timeOut);
        // 心跳包发送间隔，单位：秒
        conOpt.setKeepAliveInterval(keepAliveInterval);
        // 用户名
        conOpt.setUserName(userName);
        // 密码
        conOpt.setPassword(passWord.toCharArray());
        conOpt.setAutomaticReconnect(autoReconnect);
    }

    /**
     * 关闭客户端
     */
    public void close() {
        mLog.d("==close=");
        try {
            client.close();
        } catch (Exception e) {
            mLog.e(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 连接MQTT服务器
     */
    public void connect(IEasyMqttCallBack starMQTTCallBack) {
        this.starMQTTCallBack = starMQTTCallBack;
        if (canDoConnect && !client.isConnected()) {
            try {
                client.connect(conOpt, null, iMqttActionListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 订阅主题
     * @param topics 主题
     * @param qos 策略
     */
    public void subscribe(String topics, int qos) {
        try {
            // 订阅topic话题
            client.subscribe(topics, qos);
        } catch (Exception e) {
            mLog.e(e.toString());
        }
    }

    /**
     * 取消订阅主题
     * @param topics 主题
     */
    public void unsubscribe(String topics) {
        try {
            client.unsubscribe(topics, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mLog.d("==unsubscribe onSuccess=");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    mLog.d("==unsubscribe onFailure=");
                }
            });
        } catch (Exception e) {
            mLog.e(e.toString());
        }
    }

    /**
     * 订阅主题
     * @param topics 主题
     * @param qos 策略
     */
    public void subscribe(String[] topics, int[] qos) {
        try {
            // 订阅topic话题
            mLog.i("execute subscribe -- qos = " + qos.toString());
            client.subscribe(topics, qos);
        } catch (Exception e) {
            mLog.e(e.toString());
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        mLog.d("==disconnect=");
        try {
            client.disconnect();
        } catch (Exception e) {
            mLog.e(e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 判断连接是否断开
     */
    public boolean isConnected() {
        try {
            return client.isConnected();
        } catch (Exception e) {
            mLog.e(e.toString());
        }
        return false;
    }

    /**
     * MQTT是否连接成功
     */
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            mLog.i("mqtt connect success ");
            if (starMQTTCallBack != null) {
                starMQTTCallBack.connectSuccess(arg0);
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            mLog.i("mqtt connect failed ");
            if (starMQTTCallBack != null) {
                starMQTTCallBack.connectFailed(arg0, arg1);
            }
        }
    };

    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            String msgContent = new String(message.getPayload());
            String detailLog = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
            mLog.i("messageArrived:" + msgContent);
            mLog.i(detailLog);
            if (starMQTTCallBack != null) {
                starMQTTCallBack.messageArrived(topic, msgContent, message.getQos());
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            if (starMQTTCallBack != null) {
                starMQTTCallBack.deliveryComplete(arg0);
            }
            mLog.i("deliveryComplete");
        }

        @Override
        public void connectionLost(Throwable arg0) {
            if (starMQTTCallBack != null) {
                starMQTTCallBack.connectionLost(arg0);
            }
            mLog.i("connectionLost");
            // 失去连接，重连
        }
    };

}
