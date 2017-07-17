package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

import java.util.List;

/**
 * Created by zhangpeng on 2016/11/30.
 */
@Data
public class EnvMonitorPojoExt extends Pojo {
    List<EnvMonitorInfoPojoExt> envsByType;
}
