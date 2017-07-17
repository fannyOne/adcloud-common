package com.asiainfo.comm.common.pojo.pojoMaster;

import com.asiainfo.comm.common.pojo.Pojo;
import lombok.Data;

/**
 * Created by YangRY
 * 2016/7/21 0021.
 */
@Data
public class UserPojo extends Pojo {
    String username;
    long roleId;
    String roleName;
    long roleLevel;
    String displayName;
    int id;
    String email;
}
