package com.asiainfo.util;

import com.asiainfo.comm.common.enums.Authorization;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by zhenghp on 2016/9/9.
 */
public class UserTypeTestRightUtil extends RightUtil {
    static private Set<Authorization> authorization = Sets.newHashSet();

    static {
        authorization.add(Authorization.PIPE_TEST_OPER);
        //11-22取消测试的发布权限
        //authorization.add(Authorization.RELEASE_ROLLBACK_OPER);
        //authorization.add(Authorization.RELEASE_PLANJAR_OPER);
    }

    public static Set<Authorization> getUserTypeAuthorization() {
        //获取公共权限
        authorization.addAll(getAuthorization());
        return authorization;
    }
}
