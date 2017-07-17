package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by weif on 2016/7/14.
 */
@Data
public class ReqStagePojo {

    StagePojoExt[] obj;

    private String pipelineid;

    private String branchid;

    private String type;

    private String originPath;  // 部署包路径

    private String buildFileType;


}
