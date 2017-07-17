package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoExt.RolePojoExt;
import lombok.Data;

import java.util.List;

/**
 * Created by YangRY
 * 2016/7/11 0011.
 */
@Data
public class UserRolesPojo extends Pojo {
    private long total;
    private List<RolePojoExt> roles;
}
