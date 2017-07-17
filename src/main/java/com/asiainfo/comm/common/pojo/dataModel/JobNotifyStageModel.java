package com.asiainfo.comm.common.pojo.dataModel;

import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdPipeLineState;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by YangRY on 2016/12/13.
 */
@Getter
@Setter
public class JobNotifyStageModel {
    private AdPipeLineState pipeLineState;
    private int isSpec = 0;
    private long stageId = 0;
    private int step = 0;
    private int dealResult;
    private AdBranch adBranch;
    private Long seqId = 0L;
    private Integer stageCode = 0;
    private String preCommitId = "";
    private long opId = 0;
    private String firstpreCommitId = "";
}
