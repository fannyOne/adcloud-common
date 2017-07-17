package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

/**
 * Created by dlyxn on 2017/4/24.
 */
@Data
public class AdGroupInfoPojoExt extends Pojo {
    private Long envId;
    private String prjName;
    private String envName;
    private String state;
    private Long groupId;
}
