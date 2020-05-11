
package com.stv.commonservice.smarthome.model;

import java.util.List;

public class HouseInfoResponse {

    /**
     * status : 1 request : /api/house/query requestId : 8fc8cfc9cfd545012482be19daee19a1 result :
     * [{"houseId":"1562728964707230.633551","familyId":"1561520016.527940","name":"我的家","address":"","description":"","icon":"","state":"1"}]
     */

    private int status;
    private String request;
    private String requestId;
    private List<ResultBean> result;

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

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * houseId : 1562728964707230.633551 familyId : 1561520016.527940 name : 我的家 address : description : icon : state : 1
         */

        private String houseId;
        private String familyId;
        private String name;
        private String address;
        private String description;
        private String icon;
        private String state;

        public String getHouseId() {
            return houseId;
        }

        public void setHouseId(String houseId) {
            this.houseId = houseId;
        }

        public String getFamilyId() {
            return familyId;
        }

        public void setFamilyId(String familyId) {
            this.familyId = familyId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}
