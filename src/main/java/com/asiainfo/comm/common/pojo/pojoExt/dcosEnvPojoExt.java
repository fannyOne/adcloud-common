package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by zhangpeng on 2016/10/21.
 */
@Data
public class dcosEnvPojoExt {
    private long projectId;
    private Long infoId;
    private dcosEnvInfoPojoExt[] obj;
}
