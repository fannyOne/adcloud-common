package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

import java.util.Map;

/**
 * Created by dlyxn on 2017/3/14.
 */
@Data
public class SysReformBranchPojoExt extends Pojo {
    private String branchName;  //分支名称

    private String branchDesc;  //分支描述

    private String branchType;  //分支类型

    private String branchPath;  //分支路径

    private String triggerBranch;  //后续流水

    private String buildCron;  //定时构建Cron表达式

    private SysStagePojoExt[] stages;   //节点

    private String branchId;    //分支ID

    private String jkId;

    private String pipelineId;  //流水Id

    private String originPath;  //远端路径

    private String buildFileType;   //构建类型

    private Map<String, Boolean> buildFileTypes;    //构建文件类型

    private String envId;     //环境ID


}
