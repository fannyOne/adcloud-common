package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.AutoTestLogPojoExt;
import lombok.Data;

import java.util.List;

/**
 * Created by Administrator on 2016/7/30.
 */
@Data
public class AutoTestLogPojo extends Pojo {
    private List<AutoTestLogPojoExt> logs;
    private int totalSize;
}
