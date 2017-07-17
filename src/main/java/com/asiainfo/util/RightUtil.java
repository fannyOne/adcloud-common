package com.asiainfo.util;

import com.asiainfo.comm.common.enums.Authorization;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by zhenghp on 2016/9/9.
 */
public class RightUtil {

    public static Set<Authorization> getAuthorization() {
        return getCommonAuthorization();
    }

    private static Set<Authorization> getCommonAuthorization() {
        return Sets.newHashSet();
    }

}
