
package com.stv.commonservice.control.protocol.bean;

/**
 * elper appCustomEvent 定制消息
 */
public class CustomBean {
    public static final String CONTROL = "control";// 远程控制标识
    public static final String REQUEST_PARAM = "request_param";// 询问功能是否支持标识
    public static final String REQUEST_MODULE = "request_module";// 请求执行模块功能，如：远程控制
    /** 消息类型：CONTROL REQUEST_PARAM RESPONSE_PARAM */
    private String type;
    /** 标记一条消息，用于问答形式 */
    private String session;
    /** json格式，消息内容 */
    private String data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CustomBean{" +
                "type='" + type + '\'' +
                ", session='" + session + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
