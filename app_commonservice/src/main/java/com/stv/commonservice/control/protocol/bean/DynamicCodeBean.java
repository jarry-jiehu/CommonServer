
package com.stv.commonservice.control.protocol.bean;

public class DynamicCodeBean {
    private int ErrNo;
    private String ErrMsg;
    private String DynamicCode;

    public int getErrNo() {
        return this.ErrNo;
    }

    public void setErrNo(int errNo) {
        this.ErrNo = errNo;
    }

    public String getErrMsg() {
        return this.ErrMsg;
    }

    public void setErrMsg(String errMsg) {
        this.ErrMsg = errMsg;
    }

    public String getDynamicCode() {
        return this.DynamicCode;
    }

    public void setDynamicCode(String dynamicCode) {
        this.DynamicCode = dynamicCode;
    }

    @Override
    public String toString() {
        return "DynamicCodeBean{" +
                "ErrNo='" + ErrNo + '\'' +
                ", ErrMsg='" + ErrMsg + '\'' +
                ", DynamicCode='" + DynamicCode + '\'' +
                '}';
    }
}
