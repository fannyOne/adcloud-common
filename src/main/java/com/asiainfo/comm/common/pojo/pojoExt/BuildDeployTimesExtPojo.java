package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;

/**
 * Created by zhangpeng on 2016/7/14.
 */
@Data
public class BuildDeployTimesExtPojo {
    private String projectName;
    private Double buildTimes;
    private Double deployTimes;
    private Double buildTimesHis;
    private Double deployTimesHis;
    private List<BuildDeployTimesValueExtPojo> build;
    private List<BuildDeployTimesValueExtPojo> deploy;
}
