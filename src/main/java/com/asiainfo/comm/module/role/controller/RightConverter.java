package com.asiainfo.comm.module.role.controller;

import com.asiainfo.comm.common.enums.Authorization;
import com.asiainfo.comm.common.pojo.pojoExt.AuthorizationPojo;
import com.asiainfo.comm.common.pojo.pojoExt.GroupAuthorizationPojo;
import com.asiainfo.comm.module.role.service.impl.RightManagerImpl;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zhenghp on 2016/9/9.
 */
@Component
public class RightConverter {
    @Autowired
    RightManagerImpl rightManager;

    List<GroupAuthorizationPojo> ConverterRight(List<Authorization> authorization, Long groupId) {
        List<GroupAuthorizationPojo> groupAuthorizationPojo = Lists.newArrayList();
        GroupAuthorizationPojo pojo = new GroupAuthorizationPojo();
        pojo.setGroupId(groupId);
        pojo.setAuthorizations(authorization);
        groupAuthorizationPojo.add(pojo);
        return groupAuthorizationPojo;
    }
}
