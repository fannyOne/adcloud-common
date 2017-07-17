package com.asiainfo.comm.module.models.functionModels;

import lombok.Data;

import java.util.Date;

/**
 * Created by YangRY
 * 2016/7/3 0003.
 */
@Data
public class QMAdBugSubTask {
    String testSubTaskCode;
    String testSubTaskStatus;
    String tester;
    String deveTaskCode;
    String bugSubTaskCode;
    String bugSubTaskName;
    String bugSubTaskStatus;
    String developer;
    String codeDetail;
    Date onlineDate;
    Date createTime;
    Date submitTime;
    Date syncTime;
    Integer taskSign;
}
