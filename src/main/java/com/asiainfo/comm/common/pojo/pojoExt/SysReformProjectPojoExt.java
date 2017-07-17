package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

/**
 * Created by dlyxn on 2017/3/14.
 */
@Data
public class SysReformProjectPojoExt extends Pojo {
    private String projectId;   //项目Id

    private SysReformBranchPojoExt[] obj; //分支

    private String compileTool; //编译工具

    private String compileToolVersion;  //编译工具版本

    private String buildTool;   //构建工具
}
