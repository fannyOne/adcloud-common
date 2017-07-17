package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.WorkingSpacePojoExt;
import lombok.Data;

import java.util.List;

/**
 * Created by HK on 2016/8/30.
 */
@Data
public class WorkingSpacePojo extends Pojo {
    private List<WorkingSpacePojoExt> myBranch;
}
