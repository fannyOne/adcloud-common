package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by zhangpeng on 2016/7/7.
 */
@Data
public class SonarValueExtPojo {
    private double value;
    private double change;

    public SonarValueExtPojo(double value, double change) {
        setChange(change);
        setValue(value);
    }
}
