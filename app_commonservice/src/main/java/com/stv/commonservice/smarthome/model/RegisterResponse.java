
package com.stv.commonservice.smarthome.model;

/**
 * 注册返回Response
 */
public class RegisterResponse {

    /**
     * status : 1 request : /api/familydevice/register requestId : result :
     * {"deviceId":"X450SP-b01bd257f418-","deviceSecret":"ds.c48968e8713646462e40","elinkId":"el.868f7d2543632f1ffed6102d63eae669","productKey":"pk.34e26a8ba85"}
     */

    private int status;
    private String request;
    private String requestId;
    private ResultBean result;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * deviceId : X450SP-b01bd257f418- deviceSecret : ds.c48968e8713646462e40 elinkId : el.868f7d2543632f1ffed6102d63eae669 productKey : pk.34e26a8ba85
         */

        private String deviceId;
        private String deviceSecret;
        private String elinkId;
        private String productKey;

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceSecret() {
            return deviceSecret;
        }

        public void setDeviceSecret(String deviceSecret) {
            this.deviceSecret = deviceSecret;
        }

        public String getElinkId() {
            return elinkId;
        }

        public void setElinkId(String elinkId) {
            this.elinkId = elinkId;
        }

        public String getProductKey() {
            return productKey;
        }

        public void setProductKey(String productKey) {
            this.productKey = productKey;
        }
    }
}
