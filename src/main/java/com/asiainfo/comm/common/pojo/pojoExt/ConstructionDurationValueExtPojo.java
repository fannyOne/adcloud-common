package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by zhangpeng on 2016/8/24.
 */
@Data
public class ConstructionDurationValueExtPojo {
    private String startDate; //构建时间点
    private int flag;         //flag 1-成功；2-失败
    private double time;
}
