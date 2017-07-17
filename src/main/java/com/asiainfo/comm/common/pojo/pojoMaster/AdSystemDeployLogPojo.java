package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.module.models.AdProjectDeployPackage;
import lombok.Data;

/**
 * Created by YangRY
 * 2016/10/20 0020.
 */
@Data
public class AdSystemDeployLogPojo extends Pojo {
    private String projectId;
    private String projectName;
    private String proTagId;
    private String remarks;
    private String tagCreateDate;
    private String tagName;
    private String commitId;
    private String runTime;
    private String envId;
    private String envName;
    private String env_id;
    private String deployType;
    private String deployTypeInt;
    private String version;
    private String[] appId;
    private AdProjectDeployPackage adProjectDeployPackage;
}
