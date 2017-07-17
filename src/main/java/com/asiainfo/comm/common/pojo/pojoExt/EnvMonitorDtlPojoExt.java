package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;

/**
 * Created by zhangpeng on 2016/11/30.
 */
@Data
public class EnvMonitorDtlPojoExt {
    String serverName;
    String serverState;
    List<EnvSubMonitorPoJoExt> subServer;
}

