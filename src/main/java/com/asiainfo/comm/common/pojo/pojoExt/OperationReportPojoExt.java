package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by zhenghp on 2016/11/8.
 */
@Data
public class OperationReportPojoExt {
    Long userCount;
    Long lastMonthUserCount;
    Long groupCount;
    Long lastMonthGroupCount;
    Long branchCount;
    Long lastMonthBranchCount;
    Long envCount;
    Long lastMonthEnvCount;

}
