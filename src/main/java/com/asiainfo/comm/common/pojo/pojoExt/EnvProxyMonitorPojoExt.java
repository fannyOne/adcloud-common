package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by weif on 2017/4/25.
 */
@Data
public class EnvProxyMonitorPojoExt {

    int protocolType;//1、http 2、csf

    String[] csfParam;

    EnvHttpMonitorPojoExt httpParam;

}
