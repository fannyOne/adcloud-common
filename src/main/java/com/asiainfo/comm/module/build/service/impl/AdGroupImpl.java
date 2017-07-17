package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.*;
import com.asiainfo.comm.common.pojo.pojoMaster.FlagPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.GroupRequireInfoPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.IndexGroupPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.ProjectOfUserPojo;
import com.asiainfo.comm.module.build.dao.impl.AdGroupDAO;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDAO;
import com.asiainfo.comm.module.build.dao.impl.AdRmpBranchRelateDAO;
import com.asiainfo.comm.module.models.AdGroup;
import com.asiainfo.comm.module.models.AdGroupAdminUser;
import com.asiainfo.comm.module.models.AdGroupUser;
import com.asiainfo.comm.module.models.functionModels.AdGroupAndProject;
import com.asiainfo.comm.module.models.functionModels.AdProject;
import com.asiainfo.comm.module.models.functionModels.QLAdGroup;
import com.asiainfo.comm.module.models.functionModels.query.QQLAdGroup;
import com.asiainfo.comm.module.role.controller.GroupUserConverter;
import com.asiainfo.comm.module.role.dao.impl.AdGroupUserDAO;
import com.asiainfo.comm.module.role.service.impl.AdGroupAdminUserImpl;
import com.asiainfo.comm.module.role.service.impl.AdGroupUserImpl;
import com.asiainfo.comm.module.role.service.impl.VerifyRightImpl;
import com.asiainfo.util.CommConstants;
import com.asiainfo.util.DateConvertUtils;
import com.avaje.ebean.SqlRow;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/27.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class AdGroupImpl {
    @Autowired
    AdGroupDAO groupDAO;
    @Autowired
    AdRmpBranchRelateDAO rmpBranchRelateDAO;
    @Autowired
    AdGroupUserDAO adGroupUserDAO;
    @Autowired
    VerifyRightImpl verifyRightImpl;
    @Autowired
    AdGroupUserImpl impl;
    @Autowired
    GroupUserConverter converter;
    @Autowired
    AdGroupAdminUserImpl adGroupAdminUser;
    @Autowired
    AdProjectDAO adProjectDAO;


    public AdGroup qryById(Long groupId) {
        return groupDAO.qryAdGroupById(groupId.intValue());
    }


    public List<AdGroup> qryAllGroup() {
        return groupDAO.qryAllGroup();
    }

    public IndexGroupPojo qryIndexGroup(IndexGroupPojo pojMaster) {
        List<IndexGroupInfoPojoExt> system = new ArrayList<>();
        //前台系统
        IndexGroupInfoPojoExt fontSystem = new IndexGroupInfoPojoExt();
        fontSystem.setSysType(1);
        fontSystem.setSysName("前台系统");
        List<IndexGroupPojoExt> fontGroupList = new ArrayList<>();
        //中台系统
        IndexGroupInfoPojoExt midSystem = new IndexGroupInfoPojoExt();
        midSystem.setSysType(2);
        midSystem.setSysName("中台系统");
        List<IndexGroupPojoExt> midGroupList = new ArrayList<>();
        //地市系统
        IndexGroupInfoPojoExt areaSystem = new IndexGroupInfoPojoExt();
        areaSystem.setSysType(3);
        areaSystem.setSysName("地市系统");
        List<IndexGroupPojoExt> areaGroupList = new ArrayList<>();
        //其他系统
        IndexGroupInfoPojoExt otherSystem = new IndexGroupInfoPojoExt();
        otherSystem.setSysType(4);
        otherSystem.setSysName("其他系统");
        List<IndexGroupPojoExt> otherGroupList = new ArrayList<>();
        List<SqlRow> groups = groupDAO.qryIndexGroup();
        if (groups != null) {
            for (SqlRow sqlRow : groups) {
                IndexGroupPojoExt ext = new IndexGroupPojoExt();
                ext.setDesc(sqlRow.getString("GROUP_DESC"));
                ext.setGroupId(sqlRow.getLong("GROUP_ID"));
                ext.setGroupName(sqlRow.getString("GROUP_NAME"));
                ext.setImgIcon(sqlRow.getString("IMAGE_ICON"));
                if (sqlRow.getInteger("PRO_NUM") <= 0) {
                    ext.setStatus(0);
                } else if (sqlRow.getInteger("RUN_NUM") > 0) {
                    ext.setStatus(1);
                } else if (sqlRow.getInteger("FAIL_NUM") > 0) {
                    ext.setStatus(3);
                } else {
                    ext.setStatus(sqlRow.getInteger("SUC_NUM") > 0 ? 2 : 0);
                }
                String style = sqlRow.getString("GROUP_STYLE");
                if (style != null) {
                    if (sqlRow.getInteger("GROUP_STYLE") == 1) {
                        fontGroupList.add(ext);
                    } else if (sqlRow.getInteger("GROUP_STYLE") == 2) {
                        midGroupList.add(ext);
                    } else if (sqlRow.getInteger("GROUP_STYLE") == 3) {
                        areaGroupList.add(ext);
                    } else {
                        otherGroupList.add(ext);
                    }
                }
            }
            if (fontGroupList != null && fontGroupList.size() > 0) {
                fontSystem.setGroups(fontGroupList);
                system.add(fontSystem);
            }
            if (midGroupList != null && midGroupList.size() > 0) {
                midSystem.setGroups(midGroupList);
                system.add(midSystem);
            }
            if (areaGroupList != null && areaGroupList.size() > 0) {
                areaSystem.setGroups(areaGroupList);
                system.add(areaSystem);
            }
            if (otherGroupList != null && otherGroupList.size() > 0) {
                otherSystem.setGroups(otherGroupList);
                system.add(otherSystem);
            }
        }
        pojMaster.setSystem(system);
        return pojMaster;
    }

    ProjectOfUserPojo qryGroupOfAdminUser(List<AdGroupUser> adGroupUserList) {
        ProjectOfUserPojo poj = new ProjectOfUserPojo();
        List<GroupPojoExt> groups = new ArrayList<>();
        List<AdGroupAndProject> adQGroupUsers = groupDAO.qryAdGroupAndProject();
        if (CollectionUtils.isNotEmpty(adQGroupUsers)) {
            AdGroupUser adGroupUser2 = null;
            for (AdGroupAndProject adGroupAndProject : adQGroupUsers) {
                for (AdGroupUser adGroupUser : adGroupUserList) {
                    adGroupUser2 = null;
                    if (adGroupAndProject.getGroupId().equals(adGroupUser.getGroupId())) {
                        adGroupUser2 = adGroupUser;
                        break;
                    }
                }
                GroupPojoExt groupExt = new GroupPojoExt();
                setGroup(groupExt, adGroupAndProject, adGroupUser2);
                groups.add(groupExt);
            }
        }
        poj.setGroup(groups);
        return poj;
    }

    ProjectOfUserPojo qryGroupOfProjectAdminUser(String userName) {
        ProjectOfUserPojo poj = new ProjectOfUserPojo();
        List<GroupPojoExt> groups = new ArrayList<>();
        List<AdGroupAdminUser> adGroupAdminUsers = adGroupAdminUser.qryByUsername(userName);
        List<com.asiainfo.comm.module.models.AdProject> adProjectList;
        if (CollectionUtils.isNotEmpty(adGroupAdminUsers)) {
            for (AdGroupAdminUser adGroupAdminUser : adGroupAdminUsers) {
                GroupPojoExt groupExt = new GroupPojoExt();
                groupExt.setGroupId(adGroupAdminUser.getAdGroup().getGroupId());
                adProjectList = adProjectDAO.qryProjectByGroupId(adGroupAdminUser.getAdGroup().getGroupId().intValue());
                List<ProjectExtPojo> projectExtList = new ArrayList<>();
                ProjectExtPojo projectExt;
                if (CollectionUtils.isNotEmpty(adProjectList)) {
                    for (com.asiainfo.comm.module.models.AdProject adProject : adProjectList) {
                        projectExt = new ProjectExtPojo();
                        projectExt.setProjectId(adProject.getProjectId());
                        projectExt.setProjectName(adProject.getProjectName());
                        groupExt.setGroupName(adProject.getAdGroup().getGroupName());
                        projectExtList.add(projectExt);
                    }
                } else {
                    groupExt.setGroupName(groupDAO.qryAdGroupById(adGroupAdminUser.getAdGroup().getGroupId().intValue()).getGroupName());
                }
                groupExt.setProjects(projectExtList);
                groups.add(groupExt);
            }
        }
        poj.setGroup(groups);
        return poj;
    }


    private void setGroup(GroupPojoExt groupExt, AdGroupAndProject adGroupAndProject, AdGroupUser adGroupUser) {
        groupExt.setGroupId(adGroupAndProject.getGroupId());
        groupExt.setGroupName(adGroupAndProject.getGroupName());
        if (null != adGroupUser) {
            groupExt.setUserMember(converter.converter(Lists.newArrayList(adGroupUser)));
        }
        if (CollectionUtils.isNotEmpty(adGroupAndProject.getAdProjects())) {
            setGroupProject(groupExt, adGroupAndProject.getAdProjects());
        }
    }

    private void setGroupProject(GroupPojoExt groupExt, List<AdProject> projects) {
        List<ProjectExtPojo> projectExtList = new ArrayList<>();
        for (AdProject adProject : projects) {
            ProjectExtPojo projectExt = new ProjectExtPojo();
            projectExt.setProjectId(adProject.getProjectId());
            projectExt.setProjectName(adProject.getProjectName());
            projectExtList.add(projectExt);
        }
        groupExt.setProjects(projectExtList);
    }

    public ProjectOfUserPojo qryGroupOfUser(String userName) {
        //超级用户返回全部
        ProjectOfUserPojo poj = new ProjectOfUserPojo();
        int adminLevel = verifyRightImpl.isAdminOrProjectAdmin(userName);
        List<AdGroupUser> adGroupUsers = adGroupUserDAO.qryByUserName(userName);
        if (adminLevel == CommConstants.USER_LEVEL.USER_LEVEL_ADMIN) {
            return qryGroupOfAdminUser(adGroupUsers);
        } else if (adminLevel == CommConstants.USER_LEVEL.USER_PROJECT_ADMIN) {
            poj = qryGroupOfProjectAdminUser(userName);
        }
        List<GroupPojoExt> groups = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(poj.getGroup())) {
            groups = poj.getGroup();
        }
        if (CollectionUtils.isNotEmpty(adGroupUsers)) {
            for (AdGroupUser adGroupUser : adGroupUsers) {
                AdGroupAndProject group = groupDAO.qryAdGroupAndProjectId(adGroupUser.getGroupId());
                if (null == group) {
                    continue;
                }
                if (CollectionUtils.isNotEmpty(poj.getGroup())) {
                    for (GroupPojoExt groupPojoExt : poj.getGroup()) {
                        if (groupPojoExt.getGroupId() == group.getGroupId()) {
                            poj.getGroup().remove(groupPojoExt);
                            break;
                        }
                    }
                }
                GroupPojoExt groupExt = new GroupPojoExt();
                setGroup(groupExt, group, adGroupUser);
                groups.add(groupExt);
            }
        }
        poj.setGroup(groups);
        return poj;
    }

    public List<QLAdGroup> qryL() {
        return new QQLAdGroup().state.eq(1).orderBy("GROUP_NAME ASC").findList();
    }

    public long countAllGroup() {
        return groupDAO.qryCountAllGroup();
    }

    public long countLastMonthGroup() {
        return groupDAO.countGroupCreateDate(DateConvertUtils.getStartTimeInMonth());
    }

    public List<AdGroup> qryGroupByIds(String groupIds) {
        List<AdGroup> groups = Lists.newArrayList();
        if (StringUtils.isNotEmpty(groupIds)) {
            List<SqlRow> sqlrows = groupDAO.qryGroupByIds(groupIds);
            if (CollectionUtils.isNotEmpty(sqlrows)) {
                for (SqlRow row : sqlrows) {
                    AdGroup group = new AdGroup();
                    group.setGroupId(row.getLong("group_id"));
                    group.setGroupName(row.getString("group_name"));
                    groups.add(group);
                }
            }
        }
        return groups;
    }


    /**
     * 查询所有的项目
     *
     * @return 返回结果
     */
    public List<AdAllGroupPojoExt> qryAllGroupName() {
        return groupDAO.qryAllGroupName();
    }

    // 根据projectID 查询 group
    public AdGroup qryGroupByProjectId(Long projectId) {
        com.asiainfo.comm.module.models.AdProject adProject = adProjectDAO.qryById(projectId);
        if (adProject != null) {
            AdGroup adGroup = qryById(adProject.getAdGroup().getGroupId());
            return adGroup;
        }
        return null;
    }

    // 查询是否是CMP 云管理平台的 项目
    public Boolean qryIsCmpGroup(Long projectId) {
        AdGroup adGroup = qryGroupByProjectId(projectId);
        if (adGroup != null) {
            if (adGroup.getGroupIdExt() != null && StringUtils.isNotEmpty(adGroup.getGroupIdExt())) {   // 若 group 表中GroupIdExt 不为空，则是云管入口进来的项目
                return true;
            } else {
                return false;
            }
        }
        return null;
    }
}
