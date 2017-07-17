package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by YangRY
 * 2016/7/12 0012.
 */
@Data
public class ProjectInputExtPojo {
    private int groupId;
    private String projectName;
    private String codeStore;
    private long roleId;
    private long opId;
    private String compileTool;
    private String compileToolVersion;
    private String buildTool;
    private int gitProjectid;

}
