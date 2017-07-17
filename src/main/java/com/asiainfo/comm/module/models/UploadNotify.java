package com.asiainfo.comm.module.models;

import java.io.Serializable;

/**
 * Created by weif on 2016/6/30.
 */
public class UploadNotify implements Serializable {

    private String appId;

    private String version;

    private String packagePath;

    private String packageName;

    private String md5;

    private String remark;

    public UploadNotify() {

    }

    public UploadNotify(String appId, String version, String packagePath, String packageName, String md5, String remark) {
        this.appId = appId;
        this.version = version;
        this.packagePath = packagePath;
        this.packageName = packageName;
        this.md5 = md5;
        this.remark = remark;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

}
