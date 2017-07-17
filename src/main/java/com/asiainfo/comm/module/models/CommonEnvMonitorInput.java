package com.asiainfo.comm.module.models;

import java.io.Serializable;

/**
 * Created by weif on 2017/4/7.
 */
public class CommonEnvMonitorInput implements Serializable {

    private String moduleName;

    public CommonEnvMonitorInput(String mName) {
        this.moduleName = mName;
    }

}
