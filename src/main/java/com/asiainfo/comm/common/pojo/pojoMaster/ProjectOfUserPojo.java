package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.GroupPojoExt;
import lombok.Data;

import java.util.List;

/**
 * Created by YangRY
 * 2016/7/12 0012.
 * 首页查询用户下的项目工程
 */
@Data
public class ProjectOfUserPojo extends Pojo {
    private List<GroupPojoExt> group;
}
