package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.AdFastenSignPojoExt;
import lombok.Data;

import java.util.List;

/**
 * Created by YangRY
 * 2016/10/27 0027.
 */
@Data
public class AdFastenSignPojo extends Pojo {
    private List<AdFastenSignPojoExt> data;
}
