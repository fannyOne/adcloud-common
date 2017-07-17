package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by HK on 2016/8/24.
 */
@Data
public class OnlineInfo {
    private int reqNum;
    private int doneNum;
    private int subReqNum;
    private int bugNum;
    private int fixBugNum;
    private String fixBugPre;
    private String doneReqPre;
    private String onlineDate;
}
