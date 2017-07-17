package com.asiainfo.comm.module.models;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by zhangpeng on 2017/5/18.
 */
@Data
public class StartNotify implements Serializable {
    private String appId;

    public StartNotify() {

    }

    public StartNotify(String appId) {
        this.appId = appId;
    }

}
