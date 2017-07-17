package com.asiainfo.comm.common.pojo.dataModel;

import com.asiainfo.comm.module.models.AdStage;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by YangRY on 2016/12/13.
 */
@Getter
@Setter
public class JobNotifyModel {
    JobNotifyStageModel stage;
    private String jobName = "";
    private Integer buildNumber = 0;
    private String buildStatus = "";
    private String buildLog = "";
    private String buildPhase = "";
    private AdStage adStage;
}
