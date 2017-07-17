package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by weif on 2016/7/14.
 */
@Data
public class AdDcosDeployDtlExt {

    private Long branchId;
    private String packageName;
    private String appid;
    private Integer priorityNum;
    private boolean check;
}
