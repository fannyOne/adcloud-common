package com.asiainfo.comm.module.models;

import java.io.Serializable;

/**
 * Created by weif on 2016/6/30.
 */
public class RestartNotify implements Serializable {

    private String appId;

    public RestartNotify() {

    }

    public RestartNotify(String appId) {
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
