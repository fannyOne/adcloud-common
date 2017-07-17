package com.asiainfo.comm.common.pojo.pojoMaster;

import lombok.Data;

/**
 * Created by zhenghp on 2016/11/9.
 */
@Data
public class GroupReportPojo {
    private Long gorupId;
    private String groupName;
    private Long usedCount;
    private Long envCount;
    private Long branchCount;
    private String avgTime;
    private Long groupUserCount;
}
