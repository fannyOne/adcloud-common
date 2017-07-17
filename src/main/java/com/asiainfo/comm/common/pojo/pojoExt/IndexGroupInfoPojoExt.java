package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2016/7/27.
 */
@Data
public class IndexGroupInfoPojoExt {
    private String sysName;
    private Integer sysType;
    private List<IndexGroupPojoExt> groups;
}
