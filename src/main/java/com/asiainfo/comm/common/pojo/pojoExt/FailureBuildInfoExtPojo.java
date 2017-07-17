package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by YangRY
 * 2016/7/6 0006.
 */
@Data
public class FailureBuildInfoExtPojo {
    private String projectName;
    private String branchName;
    private Integer step;
    private String stepName;
    private Long projectId;
    private Long branchId;
    private String buildDate;
    private Long groupId;
    private String groupName;
}
