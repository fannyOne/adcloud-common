package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

/**
 * Created by zhangpeng on 2016/10/19.
 */
@Data
public class vmEvnPojoExt extends Pojo {
    private long projectId;
    private long virtualId;
    private vmEnvInfoPojoExt[] obj;
}
