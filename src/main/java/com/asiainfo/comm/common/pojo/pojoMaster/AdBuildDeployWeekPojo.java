package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.BuildDeployDateExtPojo;
import com.asiainfo.comm.common.pojo.pojoExt.BuildDeployWeekDateExtPojo;
import lombok.Data;

import java.util.List;

/**
 * Created by zhangpeng on 2016/7/14.
 */
@Data
public class AdBuildDeployWeekPojo extends Pojo {
    private List<BuildDeployDateExtPojo> dates;
    private List<BuildDeployWeekDateExtPojo> reports;

}
