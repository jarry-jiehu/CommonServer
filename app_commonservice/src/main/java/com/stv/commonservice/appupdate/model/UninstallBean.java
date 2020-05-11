
package com.stv.commonservice.appupdate.model;

import java.util.List;

/**
 * 请求需要卸载的应用
 */
public class UninstallBean {

    private int errno;
    private String errmsg;
    private List<AppInfo> data;

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public List<AppInfo> getData() {
        return data;
    }

    public void setData(List<AppInfo> data) {
        this.data = data;
    }

    public static class AppInfo {
        private String packageName;
        private String description;

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return "DataBean{" + "packageName='" + packageName + '\'' + ", description='"
                    + description + '\'' + '}';
        }
    }

    @Override
    public String toString() {
        return "UninstallBean{" + "errno=" + errno + ", errmsg='" + errmsg + '\'' + ", data=" + data
                + '}';
    }
}
