package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by zhangpeng on 2016/7/14.
 */
@Data
public class ExcelCellHeadExtPojo {
    private String cellValue;
    private boolean mergeRow = false;
    private boolean mergeColumn = false;
    private int mergeRowNum = 1;
    private int mergeColumnNum = 1;
}
