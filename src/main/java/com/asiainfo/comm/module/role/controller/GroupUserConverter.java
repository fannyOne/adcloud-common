package com.asiainfo.comm.module.role.controller;

import com.asiainfo.comm.common.enums.UserType;
import com.asiainfo.comm.common.pojo.pojoExt.GroupUserExtPojo;
import com.asiainfo.comm.common.pojo.pojoExt.GroupUserMemberPojo;
import com.asiainfo.comm.module.models.AdGroupUser;
import com.asiainfo.comm.module.role.service.impl.AdGroupUserImpl;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zhenghp on 2016/8/26.
 */
@Component
public class GroupUserConverter {
    @Autowired
    AdGroupUserImpl impl;

    /**
     * 将对象转化为Pojo
     *
     * @param groupUsers
     * @return
     */
    public List<GroupUserMemberPojo> converter(List<AdGroupUser> groupUsers) {
        List<GroupUserMemberPojo> groupUserMembers = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(groupUsers)) {
            for (AdGroupUser groupUser : groupUsers) {
                GroupUserMemberPojo groupUserMenber = new GroupUserMemberPojo();
                groupUserMenber.setUserId(groupUser.getUserId());
                groupUserMenber.setUserName(groupUser.getUserName());
                converterUserType(groupUserMenber, groupUser.getUserType());
                groupUserMembers.add(groupUserMenber);
            }
        }
        return groupUserMembers;
    }

    /**
     * pojo转化为对象
     *
     * @param groupUserExtPojo
     * @return
     */
    public List<AdGroupUser> converterToAdGroupUser(GroupUserExtPojo groupUserExtPojo) {
        List<AdGroupUser> adGroupUser = Lists.newArrayList();
        long groupId = groupUserExtPojo.getGroupId();
        List<GroupUserMemberPojo> members = groupUserExtPojo.getMembers();
        if (CollectionUtils.isNotEmpty(members)) {
            for (GroupUserMemberPojo member : members) {
                AdGroupUser groupUser = new AdGroupUser();
                groupUser.setUserName(member.getUserName());
                groupUser.setUserType(converterToUserType(member));
                groupUser.setGroupId(groupId);
                adGroupUser.add(groupUser);
            }
        }
        return adGroupUser;
    }

    /**
     * 将Pojo转化为对象
     *
     * @param member
     * @return
     */
    public AdGroupUser converterToAdGroupUser(GroupUserMemberPojo member) {
        AdGroupUser groupUser = new AdGroupUser();
        groupUser.setUserName(member.getUserName());
        groupUser.setUserType(converterToUserType(member));
        return groupUser;
    }

    private void converterUserType(GroupUserMemberPojo groupUserMenber, String userType) {
        if (StringUtils.isNotEmpty(userType)) {
            groupUserMenber.setPm(impl.isUserType(userType, UserType.PM));
            groupUserMenber.setTest(impl.isUserType(userType, UserType.TEST));
            groupUserMenber.setDev(impl.isUserType(userType, UserType.DEV));
            groupUserMenber.setDeploy(impl.isUserType(userType, UserType.DEPLOY));
        }
    }

    private String converterToUserType(GroupUserMemberPojo groupUserMenber) {
        StringBuffer rightBuffer = new StringBuffer();
        rightBuffer.append(isUserType(groupUserMenber.getPm())).append(isUserType(groupUserMenber.getTest())).append(isUserType(groupUserMenber.getDev())).append(isUserType(groupUserMenber.getDeploy()));
        return rightBuffer.toString();
    }

    private String isUserType(boolean flag) {
        return flag ? "1" : "0";
    }

}
