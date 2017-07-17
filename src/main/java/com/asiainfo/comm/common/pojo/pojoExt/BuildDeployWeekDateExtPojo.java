package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;

/**
 * Created by zhangpeng on 2016/7/14.
 */
@Data
public class BuildDeployWeekDateExtPojo {
    private String projectName;
    private List<BuildWeekSUcPresExtPojo> build;
    private List<BuildWeekSUcPresExtPojo> deploy;
    private Double buildSucPre;
    private Double deploySucPre;
    private Double buildBuildAve;
    private Double buildDeployAve;
}
