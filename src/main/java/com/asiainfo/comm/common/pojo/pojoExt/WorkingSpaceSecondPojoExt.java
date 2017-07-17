package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;

/**
 * Created by YangRY on 2016/8/30.
 */
@Data
public class WorkingSpaceSecondPojoExt {
    private String text;
    private long id;
    private List<WorkingSpaceThirdPojoExt> children;
}
