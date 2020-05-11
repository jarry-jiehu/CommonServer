
package com.stv.commonservice.appupdate.model;

/**
 * 更新信息
 */
public class UpdateInfo {
    private String packageName;
    private String version;
    // 下载地址
    private String fileUrl;
    // 更新日志
    private String description;
    // apk保存地址
    private String path;
    // 升级标识 1:可选升级 (用户可以取消升级，下次不再提示升级) 2:推荐升级 (用户可以取消升级，下次会提示升级) 3:强制升级（用户不能取消）
    private int upgradeType;
    // 最新版本号
    private String apkVersion;

    private String fileMd5;
    // 服务器需要的上报数据
    private String otherdata;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return fileUrl;
    }

    public void setUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getNote() {
        return description;
    }

    public void setNote(String description) {
        this.description = description;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUpgradeType() {
        return upgradeType;
    }

    public void setUpgradeType(int upgradeType) {
        this.upgradeType = upgradeType;
    }

    public String getApkVersion() {
        return apkVersion;
    }

    public void setApkVersion(String apkVersion) {
        this.apkVersion = apkVersion;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getOtherdata() {
        return otherdata;
    }

    public void setOtherdata(String otherdata) {
        this.otherdata = otherdata;
    }

    @Override
    public String toString() {
        return "UpdateInfo [packageName=" + packageName + ", version=" + version + ", fileUrl=" + fileUrl + ", description=" + description + ", path=" + path + ", upgradeType=" + upgradeType
                + ", apkVersion=" + apkVersion + ", fileMd5=" + fileMd5 + ", otherdata=" + otherdata + "]";
    }

}
