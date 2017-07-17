package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Function:
 * Author: Norman
 * Date: 2017/3/31.
 */
@Data
public class NaCodePathPojoExt {
    private long id;
    private String listId;
    private String proName;        //应用名
    private String selPackage;     //包名
    private String planDate;       //预计上线日期
    private String modelName;      //模块名
    private String remark;
    private Long state;
    private Long result;
}
