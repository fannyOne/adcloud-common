package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by zhangpeng on 2016/7/7.
 */
@Data
public class SonarReportExtPojo {
    private String envName; //环境名称
    private String date;
    private SonarValueExtPojo fileNum;
    private SonarValueExtPojo methodNum;
    private SonarValueExtPojo codeLine;
    private SonarValueExtPojo repeatRate;
    private SonarValueExtPojo methodComp;
    private SonarValueExtPojo totalComp;
    private SonarValueExtPojo blockPro;
    private SonarValueExtPojo seriousPro;
    private SonarValueExtPojo unitNum;
    private SonarValueExtPojo codeCover;
    private SonarValueExtPojo unitSuc;
    private SonarValueExtPojo unitTime;


}
