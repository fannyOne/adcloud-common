package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;

/**
 * Created by zhangpeng on 2016/7/14.
 */
@Data
public class BuildDeployExtPojo {
    private String projectName;
    private List<BuildSucPresExtPojo> buildSucPres;
    private List<DeploySucPresExtPojo> deploySucPres;
    private Double buildSucPre;
    private Double deploySucPre;
    private Double deploySucPreHis;
    private Double buildSucPreHis;
}
