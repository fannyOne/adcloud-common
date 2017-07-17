package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.FailureBuildInfoExtPojo;
import lombok.Data;

import java.util.List;

/**
 * Created by YangRY
 * 2016/7/6 0006.
 */
@Data
public class PersonallyWorkbench extends Pojo {
    private List<FailureBuildInfoExtPojo> message;
}
