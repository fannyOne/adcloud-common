package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by SS on 2017/5/22.
 */
@Data
public class BranchCopyPojoExt {
    long srcBranchId;
    String branchName;
    String branchDesc;
    String branchPath;
    String branchType;
    String triggerBranch;
}
