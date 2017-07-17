package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.LeangooProject;
import lombok.Data;

import java.util.List;

/**
 * Created by YangRY
 * 2016/10/10 0010.
 */
@Data
public class LeangooData extends Pojo {
    private List<LeangooProject> projects;
}
