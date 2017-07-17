package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by yangry on 2017/1/4.
 */
@Getter
@Setter
public class NewBusinessScrumPojo extends Pojo {
    int[] monthList;
    int[] developNumList;
    double[] developPreList;
    double[] scrumInfoList;
}
