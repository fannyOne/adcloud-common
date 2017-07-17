package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.SonarReportExtPojo;
import lombok.Data;

import java.util.List;

/**
 * Created by zhangpeng on 2016/7/7.
 */

@Data

public class AdSonarReportPojo extends Pojo {
    private List<SonarReportExtPojo> data;
    private String dates;
}
