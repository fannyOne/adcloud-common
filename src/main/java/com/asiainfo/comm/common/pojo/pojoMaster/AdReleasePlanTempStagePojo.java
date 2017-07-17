package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.pojoExt.AdDcosDeployDtlExt;
import lombok.Data;

import java.util.List;


/**
 * Created by SS on 2017/5/9.
 */
@Data
public class AdReleasePlanTempStagePojo {
    Long stageId;
    Long tempId;
    Long sequence; //模板节点顺序
    String projectName; //应用名称
    String operEnv;
    String packagePath;
    String packageName;
    Long projectId; //应用ID
    String commitId;
    Long type; //发布类型，1-全量，2-灰度，3-beta
    String version;
    Long proTagId; //发布包ID
    Long branchId; //流水线(包从哪条流水线打出来) id
    String env; //操作环境，格式为id_类型，类似于591_vm
    Long operType; //操作类型1、发布 2、重启
    String time; //发布时间
    String remarks; //备注
    Long auditor; //审核人ID
    String auditorName;
    List<AdDcosDeployDtlExt> appId;
    String packageVersion;
}
