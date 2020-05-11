
package com.stv.commonservice.attestation.model;

public class Info {
    public String deviceId;
    public String resultCode;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public String toString() {
        return "[ deviceId ] : [ " + deviceId + " ] [ resultCode ] : [ " + resultCode + " ]";
    }
}
