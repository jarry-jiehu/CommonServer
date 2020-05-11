
package com.stv.commonservice.smarthome.model;

/**
 * 登陆返回Response
 */
public class LoginResponse {

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

        private String familyId;
        private String token;

        public String getFamilyId() {
            return familyId;
        }

        public void setFamilyId(String familyId) {
            this.familyId = familyId;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        @Override
        public String toString() {
            return "ResultBean{" +
                    "familyId='" + familyId + '\'' +
                    ", token='" + token + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "status=" + status +
                ", request='" + request + '\'' +
                ", requestId='" + requestId + '\'' +
                ", result=" + result.toString() +
                '}';
    }
}
