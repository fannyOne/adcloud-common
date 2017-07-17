package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.WorkingSpaceFirstPojoExt;
import lombok.Data;

import java.util.List;

/**
 * Created by HK on 2016/8/30.
 */
@Data
public class WorkingSpaceSelect extends Pojo {
    private List<WorkingSpaceFirstPojoExt> prjBranchOfAuth;
}