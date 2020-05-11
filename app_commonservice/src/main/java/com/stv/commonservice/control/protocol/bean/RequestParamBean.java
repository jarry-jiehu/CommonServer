
package com.stv.commonservice.control.protocol.bean;

import java.util.Map;

/**
 * 用于请求/答复支持的功能
 */
public class RequestParamBean {
    /**
     * 支持视频通话
     */
    public static final String KEY_CHAT = "chat";
    /**
     * 支持控制对方
     */
    public static final String KEY_CONTROL = "control";
    /**
     * 支持被控制
     */
    public static final String KEY_BE_CONTROLLED = "controlled";
    /**
     * 支持IM
     */
    public static final String KEY_IM = "im";
    /**
     * 标识请求（true）/答复(false)
     */
    private boolean isRequest;
    /**
     * 支持字段，默认false
     */
    private Map<String, Boolean> param;

    public boolean isRequest() {
        return isRequest;
    }

    public void setRequest(boolean request) {
        isRequest = request;
    }

    public Map<String, Boolean> getParam() {
        return param;
    }

    public void setParam(Map<String, Boolean> param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "RequestParamBean{" +
                "isRequest=" + isRequest +
                ", param=" + param +
                '}';
    }
}
