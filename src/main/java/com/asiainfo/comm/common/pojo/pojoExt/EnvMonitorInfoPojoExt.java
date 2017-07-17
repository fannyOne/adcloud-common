package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;

/**
 * Created by zhangpeng on 2016/11/30.
 */
@Data
public class EnvMonitorInfoPojoExt {
    String envDesc;
    String envType;
    List<EnvMonitorDtlPojoExt> env;
}
