package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;
import lombok.ToString;

/**
 * Created by dlyxn on 2017/4/19.
 */
@Data
@ToString
public class AdEnvDetailPojoExt extends Pojo {
    private Long monitorId;
    private String monitorName;
    private Integer state;
    private String moduleGroup;
    private Long envId;
    private Integer ext;
    private Integer envType;
    private String jenkinsJobName;
    private String jenkinsLog;
    private Long xZuobiao;
    private Long yZuobiao;
    private String jenkinsJobContent; //脚本内容
}
