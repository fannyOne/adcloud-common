package com.asiainfo.comm.module.models;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by weif on 2016/6/30.
 */
@Data
public class
StatusNotify implements Serializable {

    private String appId;

    public StatusNotify() {
    }

    public StatusNotify(String appId) {
        this.appId = appId;
    }
}
