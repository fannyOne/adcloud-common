package com.asiainfo.comm.common.pojo.pojoExt;

import lombok.Data;

/**
 * Created by YangRY
 * 2016/7/13 0013.
 */
@Data
public class GitUserPojoExt {
    String username;
    String displayName;
    long id;
    String email;
    long roleId;
    String roleName;
    long roleLevel;
    long userId;
}
