package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

/**
 * Created by dlyxn on 2017/4/25.
 */
@Data
public class AdEnvQryConditionPojoExt extends Pojo {

    private Long groupId;
    private Long envType;
    private Integer state;
}
