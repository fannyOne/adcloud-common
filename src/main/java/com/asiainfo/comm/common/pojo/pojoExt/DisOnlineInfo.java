package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by HK on 2016/8/24.
 */
@Data
public class DisOnlineInfo {
    private int reqNum;
    private int subTaskNum;
    private int subTaskDoneNum;
    private int subTaskDoingNum;
    private int bugNum;
    private int bugDoneNum;
    private int bugDoingNum;
    private String subDoneReqPre;
    private String onlineDate;
}
