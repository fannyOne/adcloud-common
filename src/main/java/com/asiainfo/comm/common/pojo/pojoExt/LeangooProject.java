package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;

/**
 * Created by YangRY
 * 2016/10/10 0010.
 */
@Data
public class LeangooProject {
    private long projectId;
    private String projectName;
    private List<LeangooBoard> boards;
}
