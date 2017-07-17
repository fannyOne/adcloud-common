package com.asiainfo.comm.common.pojo.pojoExt;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

import java.util.List;

/**
 * Created by zhangpeng on 2016/8/24.
 */
@Data
public class ConstructionDurationExtPojo extends Pojo {
    private List<ConstructionDurationValueExtPojo> buildInfo;
}
