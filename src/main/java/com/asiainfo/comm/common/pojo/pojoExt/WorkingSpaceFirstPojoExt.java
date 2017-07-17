package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;

/**
 * Created by HK on 2016/8/30.
 */
@Data
public class WorkingSpaceFirstPojoExt {
    private long id;
    private String text;
    private List<WorkingSpaceSecondPojoExt> children;
}
