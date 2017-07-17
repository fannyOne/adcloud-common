package com.asiainfo.comm.common.pojo;

/**
 * Created by yangry on 2016/6/15 0015.
 */
public abstract class Pojo {
    private String retCode = "200";
    private String retMessage = "";

    public String getRetCode() {
        return retCode;
    }

    public void setRetCode(String retCode) {
        this.retCode = retCode;
    }

    public String getRetMessage() {
        return retMessage;
    }

    public void setRetMessage(String retMessage) {
        this.retMessage = retMessage;
    }
}
