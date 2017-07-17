package com.asiainfo.comm.module.models;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by weif on 2016/6/30.
 */
@Data
public class DeployNotify implements Serializable {


    private String appId;

    private String version;

    private String remark;

    private String username;

    public DeployNotify() {

    }

    public DeployNotify(String appId, String version, String remark, String username) {
        this.appId = appId;
        this.version = version;
        this.remark = remark;
        this.username = username;
    }
}
