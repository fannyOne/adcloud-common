package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

/**
 * Created by weif on 2016/7/14.
 */
@Data
public class SysProjectPojoExt extends Pojo {

    private String projectId;

    private SysBranchPojoExt[] obj;

    private String compileTool;

    private String compileToolVersion;

    private String buildTool;

}
