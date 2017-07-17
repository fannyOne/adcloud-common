package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

import java.util.List;


@Data
public class DevRmpListExtPojo extends Pojo {
    private List<DevRmpInfoExtPojo> rmpBuildInfo;
    private int devSum;
}
