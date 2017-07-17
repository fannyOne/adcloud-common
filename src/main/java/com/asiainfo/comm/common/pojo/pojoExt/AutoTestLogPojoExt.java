package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by Administrator on 2016/7/30.
 */
@Data
public class AutoTestLogPojoExt {
    private String logInfo;
    private String state;//状态
    private Long branchId;
    private Long stageId;
    private Long seqId;
    private String startDate;
    private String endDate;
    private Long testId;
    private Long sucNum;
    private Long failNum;
    private Long totalNum;
    private String sucPre;
}
