package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.BuildDeployDateExtPojo;
import com.asiainfo.comm.common.pojo.pojoExt.BuildDeployTimesExtPojo;
import lombok.Data;

import java.util.List;

/**
 * Created by zhangpeng on 2016/7/14.
 */
@Data
public class AdBuildDeployTimesPojo extends Pojo {
    private List<BuildDeployTimesExtPojo> reports;
    private List<BuildDeployDateExtPojo> dates;
}
