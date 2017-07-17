package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

import java.util.List;

/**
 * Created by YangRY on 2016/11/9.
 */
@Data
public class AdTreeDataPojoExt {
    private List<AdTreeDataPojoExt> children;
    private Long treeId;
    private String treeCode;
    private String treePara;
    private String treeName;
    private boolean isCheck;
}
