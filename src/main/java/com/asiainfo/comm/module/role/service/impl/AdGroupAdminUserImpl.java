package com.asiainfo.comm.module.role.service.impl;

import com.asiainfo.comm.module.build.dao.impl.AdGroupDAO;
import com.asiainfo.comm.module.build.dao.impl.AdUserDAO;
import com.asiainfo.comm.module.models.AdGroupAdminUser;
import com.asiainfo.comm.module.models.AdUser;
import com.asiainfo.comm.module.role.dao.impl.AdGroupAdminUserDAO;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhenghp on 2016/12/20.
 */
@Component
public class AdGroupAdminUserImpl {

    @Autowired
    AdGroupAdminUserDAO adGroupAdminUserDAO;
    @Autowired
    AdUserDAO adUserDAO;
    @Autowired
    AdGroupDAO adGroupDAO;


    public void del(AdGroupAdminUser adGroupAdminUser) {
        adGroupAdminUserDAO.del(adGroupAdminUser);
    }

    public void del(List<AdGroupAdminUser> adGroupAdminUsers) {
        if (CollectionUtils.isNotEmpty(adGroupAdminUsers)) {
            for (AdGroupAdminUser adGroupAdminUser : adGroupAdminUsers) {
                del(adGroupAdminUser);
            }
        }
    }


    public void update(String userName, String groupIds, long opId) {
        List<AdGroupAdminUser> exist = qryByUsername(userName);
        List<AdGroupAdminUser> delAdGroupUser = Lists.newArrayList();
        List<String> groupLists = Lists.newArrayList();
        if (StringUtils.isNotEmpty(groupIds)) {
            groupLists = new ArrayList(Arrays.asList(groupIds.split(",")));
        }
        if (null != exist && exist.size() > 0) {
            for (AdGroupAdminUser adGroupAdminUser : exist) {
                if (!isExistGroupUser(adGroupAdminUser, groupLists)) {
                    adGroupAdminUser.setOpId(opId);
                    delAdGroupUser.add(adGroupAdminUser);
                }
            }
        }
        del(delAdGroupUser);
        create(userName, groupLists, opId);
    }

    private void create(String userName, List<String> groupLists, long opId) {
        long userId = 0;
        List<AdUser> aduser = adUserDAO.qryUserByLoginName(userName);
        if (CollectionUtils.isNotEmpty(aduser)) {
            userId = aduser.get(0).getUserId();
        }
        if (CollectionUtils.isNotEmpty(groupLists)) {
            for (String groupId : groupLists) {
                create(userId, Long.parseLong(groupId), opId, userName);
            }
        }
    }

    private boolean isExistGroupUser(AdGroupAdminUser extAdGroupUser, List<String> groupIds) {
        if (CollectionUtils.isNotEmpty(groupIds)) {
            if (groupIds.contains(String.valueOf(extAdGroupUser.getAdGroup().getGroupId()))) {
                groupIds.remove(String.valueOf(extAdGroupUser.getAdGroup().getGroupId()));
                return true;
            }
        }
        return false;
    }

    public void create(long userId, long groupid, long opId, String userName) {
        AdGroupAdminUser adGroupAdminUser = new AdGroupAdminUser();
        adGroupAdminUser.setAdGroup(adGroupDAO.qryAdGroupById((int) groupid));
        adGroupAdminUser.setUserId(userId);
        adGroupAdminUser.setOpId(opId);
        adGroupAdminUser.setUserName(userName);
        adGroupAdminUserDAO.save(adGroupAdminUser);
    }

    public List<AdGroupAdminUser> qryByGroupIdAndUsername(long groupId, String userName) {
        return adGroupAdminUserDAO.qryByGroupIdAndUsername(groupId, userName);
    }

    public List<AdGroupAdminUser> qryByUsername(String userName) {
        return adGroupAdminUserDAO.qryByUsername(userName);
    }

    public List<AdGroupAdminUser> qryByGroupId(long groupId) {
        return adGroupAdminUserDAO.qryByGroupId(groupId);
    }
}
