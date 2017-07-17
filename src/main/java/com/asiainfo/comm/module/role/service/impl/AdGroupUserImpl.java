package com.asiainfo.comm.module.role.service.impl;

import com.asiainfo.comm.common.enums.UserType;
import com.asiainfo.comm.common.pojo.pojoExt.GitUserPojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.UsersPojo;
import com.asiainfo.comm.module.models.AdGroupUser;
import com.asiainfo.comm.module.models.functionModels.QLAdGroupUser;
import com.asiainfo.comm.module.role.dao.impl.AdGroupUserDAO;
import com.avaje.ebean.SqlRow;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhenghp on 2016/8/21.
 */
@Component
public class AdGroupUserImpl {
    @Autowired
    AdGroupUserDAO adGroupUserDAO;

    /**
     * 根据项目编号和用户名称查询用户权限信息
     *
     * @param groupId
     * @param userName
     * @return
     */
    public List<AdGroupUser> qryByGroupIdAndUserName(long groupId, String userName) {
        return adGroupUserDAO.qryByGroupIdAndUserName(groupId, userName);
    }

    /**
     * 根据项目编号查询羡慕下的成员
     *
     * @param groupId
     * @return
     */
    public List<AdGroupUser> qryByGroupId(long groupId) {
        return adGroupUserDAO.qryByGroupId(groupId);
    }

    /**
     * 根据项目编号和用户名称删除用户与项目关系
     *
     * @param groupId
     * @param userName
     * @return
     */
    public boolean delete(long groupId, String userName) {
        List<AdGroupUser> adGroupUser = qryByGroupIdAndUserName(groupId, userName);
        if (CollectionUtils.isNotEmpty(adGroupUser)) {
            delete(adGroupUser);
        }
        return false;
    }

    /**
     * 根据项目编号和用户名称删除用户与项目关系
     *
     * @param groupId
     * @return
     */
    public boolean delete(long groupId) {
        List<AdGroupUser> adGroupUser = qryByGroupId(groupId);
        if (CollectionUtils.isNotEmpty(adGroupUser)) {
            delete(adGroupUser);
        }
        return false;
    }

    /**
     * 新建项目与人员的关系
     *
     * @param groupId
     * @param userId
     * @param userName
     * @param type
     * @param opId
     * @return
     */
    public AdGroupUser create(long groupId, long userId, String userName, UserType type, long opId) {
        AdGroupUser groupUser = new AdGroupUser();
        groupUser.setGroupId(groupId);
        groupUser.setUserId(userId);
        groupUser.setUserName(userName);
        groupUser.setUserType(type.getDescription());
        groupUser.setOpId(opId);
        return adGroupUserDAO.save(groupUser);
    }


    /**
     * 添加或修改用户
     *
     * @param groupId
     * @param adGroupUsers
     * @return
     */
    public List<AdGroupUser> addOrUpdate(long groupId, List<AdGroupUser> adGroupUsers) {
        List<AdGroupUser> extAdGroupUser = qryByGroupId(groupId);
        if (CollectionUtils.isNotEmpty(adGroupUsers)) {
            for (AdGroupUser adGroupUser : adGroupUsers) {
                addOrUpdate(adGroupUser, extAdGroupUser);
            }
        }
        return adGroupUsers;
    }


    /**
     * 全量同步成员信息（添加修改删除）
     *
     * @param groupId
     * @param adGroupUsers
     * @return
     */
    public List<AdGroupUser> update(long groupId, List<AdGroupUser> adGroupUsers) {
        List<AdGroupUser> extAdGroupUser = qryByGroupId(groupId);
        List<AdGroupUser> delAdGroupUser = Lists.newArrayList();
        List<AdGroupUser> addAdGroupUser = Lists.newArrayList(adGroupUsers);
        if (CollectionUtils.isNotEmpty(extAdGroupUser)) {
            for (AdGroupUser adGroupUser : extAdGroupUser) {
                if (!isExistGroupUser(adGroupUser, addAdGroupUser)) {
                    delAdGroupUser.add(adGroupUser);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(addAdGroupUser)) {
            save(addAdGroupUser);
        }
        if (CollectionUtils.isNotEmpty(delAdGroupUser)) {
            delete(delAdGroupUser);
        }
        return adGroupUsers;
    }


    public List<QLAdGroupUser> qryLByUserName(String username) {
        return adGroupUserDAO.qryLByUserName(username);
    }

    /**
     * 判断用户是否拥有该角色
     *
     * @param userType
     * @param type
     * @return
     */
    public boolean isUserType(String userType, UserType type) {
        if (StringUtils.isEmpty(userType)) {
            return false;
        }
        if (userType.length() >= type.getCode()) {
            return "1".equals(userType.substring(type.getCode() - 1, type.getCode()));
        }
        return false;
    }

    /**
     * 判断该用户角色含有po/pm
     *
     * @param adGroupUser
     * @return
     */
    public boolean isGroupPm(AdGroupUser adGroupUser) {
        if (null == adGroupUser) {
            return false;
        }
        return isUserType(adGroupUser.getUserType(), UserType.PM);
    }

    public boolean isGroupTest(AdGroupUser adGroupUser) {
        if (null == adGroupUser) {
            return false;
        }
        return isUserType(adGroupUser.getUserType(), UserType.TEST);
    }

    public boolean isGroupDev(AdGroupUser adGroupUser) {
        if (null == adGroupUser) {
            return false;
        }
        return isUserType(adGroupUser.getUserType(), UserType.DEV);
    }


    public int isGroupDeploy(List<AdGroupUser> adGroupUserList) {
        int deployRole = 0;
        boolean hasTest = false;
        boolean hasDeploy = false;
        for (AdGroupUser groupUser : adGroupUserList) {
            if (null == groupUser) {
                continue;
            }
            if (!hasDeploy && isUserType(groupUser.getUserType(), UserType.DEPLOY)) {
                deployRole += 2;
                hasDeploy = true;
            }
            if (!hasTest && (isUserType(groupUser.getUserType(), UserType.TEST) || isUserType(groupUser.getUserType(), UserType.PM))) {
                deployRole += 1;
                hasTest = true;
            }
        }
        return deployRole;
    }

    private AdGroupUser save(AdGroupUser adGroupUser) {
        return adGroupUserDAO.save(adGroupUser);
    }

    private List<AdGroupUser> save(List<AdGroupUser> adGroupUsers) {
        return adGroupUserDAO.save(adGroupUsers);
    }

    private List<AdGroupUser> delete(List<AdGroupUser> adGroupUsers) {
        return adGroupUserDAO.delete(adGroupUsers);
    }

    private AdGroupUser update(AdGroupUser adGroupUser) {
        return adGroupUserDAO.update(adGroupUser);
    }

    private AdGroupUser addOrUpdate(AdGroupUser adGroupUser, List<AdGroupUser> extAdGroupUsers) {
        if (CollectionUtils.isNotEmpty(extAdGroupUsers)) {
            for (AdGroupUser extAdGroupUser : extAdGroupUsers) {
                if (isSameUser(adGroupUser, extAdGroupUser)) {
                    if (!isSameUserType(adGroupUser, extAdGroupUser)) {
                        extAdGroupUser.setUserType(adGroupUser.getUserType());
                        adGroupUser = update(extAdGroupUser);
                    }
                    return adGroupUser;
                }
            }
        }
        save(adGroupUser);
        return adGroupUser;
    }

    private boolean isExistGroupUser(AdGroupUser extAdGroupUser, List<AdGroupUser> adGroupUsers) {
        if (CollectionUtils.isNotEmpty(adGroupUsers)) {
            for (AdGroupUser adGroupUser : adGroupUsers) {
                if (isSameUser(adGroupUser, extAdGroupUser)) {
                    if (!isSameUserType(adGroupUser, extAdGroupUser)) {
                        //用户权限发生变更
                        extAdGroupUser.setUserType(adGroupUser.getUserType());
                        adGroupUserDAO.update(extAdGroupUser);
                    }
                    //已存在的用户从需要添加的列表中删除
                    removeExistUser(adGroupUsers, extAdGroupUser);
                    return true;
                }
            }
        }
        return false;
    }

    private void removeExistUser(List<AdGroupUser> adGroupUsers, AdGroupUser extAdGroupUser) {
        if (CollectionUtils.isNotEmpty(adGroupUsers)) {
            //后续操作用有删除adGroupUser动作，需新建一个用户数组
            for (AdGroupUser adGroupUser : Lists.newArrayList(adGroupUsers)) {
                if (isSameUser(adGroupUser, extAdGroupUser)) {
                    adGroupUsers.remove(adGroupUser);
                }
            }
        }
    }

    private boolean isSameUserType(AdGroupUser aAdGroupUser, AdGroupUser bAdGroupUser) {
        if (aAdGroupUser.getUserType().equals(bAdGroupUser.getUserType())) {
            return true;
        }
        return false;
    }

    private boolean isSameUser(AdGroupUser aAdGroupUser, AdGroupUser bAdGroupUser) {
        if (aAdGroupUser.getUserName().equals(bAdGroupUser.getUserName())) {
            return true;
        }
        return false;
    }

    //根据用户名模糊查找项目下的成员
    public UsersPojo qryByUserNameAndGroupId(String userName, long groupId) {
        List<SqlRow> userList = adGroupUserDAO.qryByUserNameAndGroupId(userName, groupId);
        UsersPojo usersPojo = new UsersPojo();
        usersPojo.setTotal(0);
        if (CollectionUtils.isNotEmpty(userList)) {
            List<GitUserPojoExt> users = new ArrayList<GitUserPojoExt>();
            GitUserPojoExt user = null;
            for (SqlRow sqlRow : userList) {
                user = new GitUserPojoExt();
                user.setUserId(sqlRow.getLong("user_id"));
                user.setUsername(sqlRow.getString("login_name"));
                user.setDisplayName(sqlRow.getString("display_name"));
                users.add(user);
            }
            usersPojo.setUsers(users);
            usersPojo.setTotal(userList.size());
        }
        return usersPojo;
    }
}
