package com.asiainfo.comm.module.models;

import java.io.Serializable;

/**
 * Created by weif on 2016/7/1.
 */
public class CommonNotifyResult implements Serializable {


    private String appId;

    private String returnCode;

    private String returnMsg;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }
}
