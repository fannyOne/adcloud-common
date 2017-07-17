package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yangry on 2016/6/15 0015.
 */
@Data
public class JobExtPojo {
    private String name;
    private String state;
    private String stageId;
    private Integer stageCode;
    private Date startTime;
    private Long averageTime;
    private Long process;
    private String beginTime;
    private String durationTime;
    private List<JobDtlPojo> jobDtls;
    private List<DeployPackagesExtPojo> packages = new ArrayList<>();
}
