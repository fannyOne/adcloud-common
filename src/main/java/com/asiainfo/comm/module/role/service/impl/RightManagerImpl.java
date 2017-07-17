package com.asiainfo.comm.module.role.service.impl;

import com.asiainfo.comm.common.enums.Authorization;
import com.asiainfo.util.UserTypeTestRightUtil;
import com.asiainfo.comm.module.models.AdGroupUser;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zhenghp on 2016/9/9.
 */
@Component
public class RightManagerImpl {
    @Autowired
    VerifyRightImpl verifyRightImpl;
    @Autowired
    AdGroupUserImpl impl;

    /**
     * 获取用户角色的权限列表
     *
     * @param userName
     * @param groupId
     * @return
     */
    public List<Authorization> qryRight(String userName, long groupId) {
        if (verifyRightImpl.checkGroupRight(userName, groupId)) {
            return adminRight();
        }
        return qryRightByUserType(userName, groupId);
    }

    private List<Authorization> qryRightByUserType(String userName, long groupId) {
        List<Authorization> authorizations = Lists.newArrayList();
        List<AdGroupUser> groupUsers = impl.qryByGroupIdAndUserName(groupId, userName);
        if (CollectionUtils.isEmpty(groupUsers)) {
            return authorizations;
        }
        AdGroupUser groupUser = groupUsers.get(0);
        if (impl.isGroupPm(groupUser)) {
            return adminRight();
        }
        if (impl.isGroupDev(groupUser)) {
            addTDevRight();
        }
        if (impl.isGroupTest(groupUser)) {
            addTestRight(authorizations);
        }
        return authorizations;
    }

    private List<Authorization> adminRight() {
        return Lists.newArrayList(Authorization.ALL);
    }

    private void addTestRight(List<Authorization> authorizations) {
        authorizations.addAll(UserTypeTestRightUtil.getUserTypeAuthorization());
    }

    private void addTDevRight() {
        //TODO 开发暂无赋权
    }
}
