package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.PipelineExtPojo;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by yangry on 2016/6/15 0015.
 */
@Data
public class OperationNowPojo extends Pojo {
    private String name;
    private long projectId;
    private long groupId;
    private Date systemTime;
    private List<PipelineExtPojo> pipeline;
}
