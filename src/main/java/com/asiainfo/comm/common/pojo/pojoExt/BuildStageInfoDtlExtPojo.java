package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class BuildStageInfoDtlExtPojo {
    private Long stageId;
    private String beginTime;
    private String durationTime;
    private String fileNum;
    private List<DeployPackagesExtPojo> packages = new ArrayList<>();
    private String failRate;
    private List<StageKeyValue> keyValues;
}
