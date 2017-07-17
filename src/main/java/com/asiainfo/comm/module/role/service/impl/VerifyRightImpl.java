package com.asiainfo.comm.module.role.service.impl;

import com.asiainfo.comm.common.enums.Authorization;
import com.asiainfo.comm.common.enums.AuthorizationResource;
import com.asiainfo.comm.common.enums.AuthorizationType;
import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.module.common.AdParaDetailDAO;
import com.asiainfo.comm.module.build.service.impl.AdBranchImpl;
import com.asiainfo.comm.module.models.*;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zhenghp on 2016/8/30.
 */
@Component
public class VerifyRightImpl {
    @Autowired
    AdUserRoleRelImpl aduserRoleRelImpl;
    @Autowired
    AdProjectImpl projectImpl;
    @Autowired
    AdGroupUserImpl adGroupUserImpl;
    @Autowired
    RightManagerImpl rightManager;
    @Autowired
    AdBranchImpl adBranchImpl;
    @Autowired
    AdGroupAdminUserImpl adGroupAdminUser;
    @Autowired
    AdParaDetailDAO paraDetailDAO;


    /**
     * 判断用户是否对应用有权限
     *
     * @param projectId
     * @param userName
     * @return
     */
    public boolean verifyRight(long projectId, String userName) {
        AdProject adProject = projectImpl.qryProject(projectId);
        if (adProject != null && checkGroupRight(userName, adProject.getAdGroup().getGroupId())) {
            return true;
        }
        return isRelGroup(projectId, userName);
    }

    /**
     * 判断用户是否对应用有权限
     *
     * @param groupId
     * @param userName
     * @return
     */
    public boolean verifyRightGroup(long groupId, String userName) {
        if (checkGroupRight(userName, groupId)) {
            return true;
        }
        return isRelGroupForGroup(groupId, userName);
    }

    /**
     * 判断用户是否与应用有关系
     *
     * @param projectId
     * @param userName
     * @return
     */
    public boolean isRelGroup(long projectId, String userName) {
        AdProject adproject = projectImpl.qryProject(projectId);
        if (null != adproject) {
            List<AdGroupUser> groupUser = adGroupUserImpl.qryByGroupIdAndUserName(adproject.getAdGroup().getGroupId(), userName);
            if (CollectionUtils.isNotEmpty(groupUser)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户是否与应用有关系
     *
     * @param groupId
     * @param userName
     * @return
     */
    public boolean isRelGroupForGroup(long groupId, String userName) {
        List<AdGroupUser> groupUser = adGroupUserImpl.qryByGroupIdAndUserName(groupId, userName);
        if (CollectionUtils.isNotEmpty(groupUser)) {
            return true;
        }
        return false;
    }


    /**
     * 验证用户是否是Admin
     *
     * @param userName
     * @return
     */
    public boolean isAdmin(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return false;
        }
        List<AdUserRoleRel> adUserRoleRels = aduserRoleRelImpl.qryByUser(userName);
        if (CollectionUtils.isNotEmpty(adUserRoleRels)) {
            if (null != adUserRoleRels.get(0).getAdRole() && adUserRoleRels.get(0).getAdRole().getRoleLevel() ==
                CommConstants.USER_LEVEL.USER_LEVEL_ADMIN) {
                return true;
            }
        }
        return false;
    }

    public int isAdminOrProjectAdmin(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return -1;
        }
        List<AdUserRoleRel> adUserRoleRels = aduserRoleRelImpl.qryByUser(userName);
        if (CollectionUtils.isNotEmpty(adUserRoleRels)) {
            if (null != adUserRoleRels.get(0).getAdRole() && adUserRoleRels.get(0).getAdRole().getRoleLevel() ==
                CommConstants.USER_LEVEL.USER_LEVEL_ADMIN) {
                return CommConstants.USER_LEVEL.USER_LEVEL_ADMIN;
            } else if (null != adUserRoleRels.get(0).getAdRole() && adUserRoleRels.get(0).getAdRole().getRoleLevel() ==
                CommConstants.USER_LEVEL.USER_PROJECT_ADMIN) {
                return CommConstants.USER_LEVEL.USER_PROJECT_ADMIN;
            }
        }
        return -1;
    }

    public boolean checkGroupRight(String userName, long groupId) {
        if (!isAdmin(userName)) {
            List<AdUserRoleRel> adUserRoleRels = aduserRoleRelImpl.qryByUser(userName);
            if (CollectionUtils.isNotEmpty(adUserRoleRels)) {
                if (null != adUserRoleRels.get(0).getAdRole() && adUserRoleRels.get(0).getAdRole().getRoleLevel() ==
                    CommConstants.USER_LEVEL.USER_PROJECT_ADMIN) {//项目管理员
                    List<AdGroupAdminUser> adGroupAdminUsers = adGroupAdminUser.qryByGroupIdAndUsername(groupId, userName);
                    if (!adGroupAdminUsers.isEmpty()) {
                        return true;
                    }
                }
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * 批量查询判断用户是否对应用有权限
     *
     * @param projectIds
     * @param userName
     * @return
     */
    public boolean verifyRight(String[] projectIds, String userName) {
        int adminLevel = isAdminOrProjectAdmin(userName);
        if (adminLevel == CommConstants.USER_LEVEL.USER_LEVEL_ADMIN) {
            return true;
        }
        if (projectIds == null || CollectionUtils.isEmpty(Lists.newArrayList(projectIds))) {
            return false;
        }
        for (String projectId : projectIds) {
            if (!isRelGroup(Long.parseLong(projectId), userName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断用户是否拥有该项目所有权限
     *
     * @param groupId
     * @param userName
     * @return
     */
    public boolean verifyGroupMaster(long groupId, String userName) {
        //      系统管理员                            PO/PM
        return checkGroupRight(userName, groupId) || isRelGroupMaster(groupId, userName);

    }

    /**
     * 判断用户是否拥有该项目的PO/PM权限
     *
     * @param groupId
     * @param userName
     * @return
     */
    public boolean isRelGroupMaster(long groupId, String userName) {
        List<AdGroupUser> groupUser = adGroupUserImpl.qryByGroupIdAndUserName(groupId, userName);
        if (CollectionUtils.isNotEmpty(groupUser)) {
            if (adGroupUserImpl.isGroupPm(groupUser.get(0))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断用户是否拥有生产发布权限（发布人员是2，测试人员是1）
     *
     * @param groupId
     * @param userName
     * @return
     */
    public int isRelGroupDeploy(long groupId, String userName) {
        List<AdGroupUser> groupUser = adGroupUserImpl.qryByGroupIdAndUserName(groupId, userName);
        return adGroupUserImpl.isGroupDeploy(groupUser);
    }
//    /**
//     * 判断用户是否拥有该应用的PO/PM权限
//     *
//     * @param projectId
//     * @param userName
//     * @return
//     */
//    public boolean isRelProjectMaster(long projectId, String userName) {
//        if (isAdmin(userName)) {
//            return true;
//        }
//        AdProject adproject = projectImpl.qryProject(projectId);
//        if (null != adproject) {
//            return isRelGroupMaster(adproject.getAdGroup().getGroupId(), userName);
//        }
//        return false;
//    }

    /**
     * 判断用户是否采用该权限实体
     *
     * @param authorization
     * @param authorizations
     * @return
     */
    public boolean isRight(Authorization authorization, List<Authorization> authorizations) {
        if (null == authorization) {
            return true;
        }
        if (authorizations.contains(Authorization.ALL)) {
            return true;
        }
        return authorizations.contains(authorization);
    }

    public boolean verifyProjectRight(Authorization authorization, long projectId, String userName) {
        AdProject adproject = projectImpl.qryProject(projectId);
        if (null == adproject || null == adproject.getAdGroup()) {
            return false;
        }
        return verifyRight(authorization, adproject.getAdGroup().getGroupId(), userName);
    }

    public boolean verifyRight(Authorization authorization, long groupId, String userName) {
        return isRight(authorization, rightManager.qryRight(userName, groupId));
    }

    public boolean verifyBranchRight(AdBranch env, List<Authorization> rights) {
        if (1 < env.getBranchType() && env.getBranchType() < 5) {
            return isRight(Authorization.getAuthorization(
                AuthorizationResource.PIPE_TEST, AuthorizationType.OPER), rights);
        }
        if (env.getBranchType() == 5) {
            return isRight(Authorization.getAuthorization(
                AuthorizationResource.PIPE_PROD, AuthorizationType.OPER), rights);
        }
        return true;
    }

    public boolean verifyBranchRight(Long branchId, String userName) {
        AdBranch branch = adBranchImpl.qryById(branchId);
        if (null == branch) return false;
        if (null == branch.getAdProject()) return false;
        if (null == branch.getAdProject().getAdGroup()) return false;
        return verifyBranchRight(branch, rightManager.qryRight(userName, branch.getAdProject().getAdGroup().getGroupId()));
    }
}
