package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;

/**
 * Created by zhangpeng on 2016/10/21.
 */
@Data
public class dcosEnvInfoPojoExt {
    private List<AppIdinfoPojoExt> appids;
    private String dcosFtpUrl;
    private String dcosFtpUsername;
    private String dcosFtpPassword;
    private String dcosFtpPort;
    private String docsServerUrl;
    private String docsUserName;
    private String docsUserPassword;
    private String dcosFtpPath;
    private String branchDesc;
    private Long deployInfoId;
    private Long projectId;
    private String projectName;
    private Long dcosBranchId;
    private String m;
    private String branchIds;
    private String envName;
    private Integer region;     //环境归属域
    private List<AdBranchCheckPojoExt> branchCheck;
    private String visitSource; // 页面入口： cmp 或 ADcloud
}
