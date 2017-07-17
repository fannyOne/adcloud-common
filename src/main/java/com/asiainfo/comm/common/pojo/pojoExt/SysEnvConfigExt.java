package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by weif on 2017/1/16.
 */
@Data
public class SysEnvConfigExt {

    private String envType;

    private SysVmDataExt vmData;

    private SysDcosDataExt[] dcosData;
}
