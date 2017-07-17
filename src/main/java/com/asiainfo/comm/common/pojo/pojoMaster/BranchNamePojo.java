package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

import java.util.List;

/**
 * Created by zhangpeng on 2016/10/20.
 */
@Data
public class BranchNamePojo extends Pojo {
    private List<BranchNameListPojo> branchName;
}
