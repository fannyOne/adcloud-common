package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

import java.util.Map;

/**
 * Created by weif on 2016/7/14.
 */
@Data
public class SysBranchPojoExt extends Pojo {

    private String branchName;

    private String branchDesc;

    private String branchType;

    private String branchPath;

    private String triggerBranch;

    private SysStagePojoExt[] stages;

    private String branchId;

    private String jkId;

    private String pipelineId;

    private String originPath;

    private String buildFileType;

    private Map<String, Boolean> buildFileTypes;

    private SysEnvConfigExt envConfig;

}
