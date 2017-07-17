package com.asiainfo.comm.module.role.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.GroupPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.ProjectExtPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.ProjectOfUserPojo;
import com.asiainfo.comm.module.build.dao.impl.AdGroupDAO;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDAO;
import com.asiainfo.comm.module.build.dao.impl.AdUserDAO;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.role.dao.impl.AdAuthorDAO;
import com.asiainfo.comm.module.role.dao.impl.AdGroupUserDAO;
import com.asiainfo.comm.module.role.dao.impl.AdUserRoleRelDAO;
import com.avaje.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YangRY
 * 2016/7/12 0012.
 */
@Component
public class AdProjectImpl {
    @Autowired
    AdProjectDAO projectDAO;

    @Autowired
    AdUserRoleRelDAO userRoleRelDAO;

    @Autowired
    AdAuthorDAO adAuthorDAO;

    @Autowired
    AdGroupUserDAO adGroupUserDAO;

    @Autowired
    AdUserDAO adUserDAO;

    @Autowired
    AdGroupDAO adGroupDAO;

    public AdProject qryProject(long projectId) {
        return projectDAO.qryById(projectId);
    }

    public List<AdProject> qryProjects(List<String> projectIds) {
        return projectDAO.qryByIds(projectIds);
    }

    public ProjectOfUserPojo qryProjectOfUser(String userName) {
        ProjectOfUserPojo poj = new ProjectOfUserPojo();
        List<AdUserRoleRel> roleRels = userRoleRelDAO.qryByUser(userName);
        List<GroupPojoExt> groups = new ArrayList<>();
        Map<Long, GroupPojoExt> groupMaps = new HashMap<>();
        for (AdUserRoleRel roleRel : roleRels) {
            List<AdAuthor> authors = adAuthorDAO.qryAuthorByRole(roleRel.getAdRole().getRoleId());
            for (AdAuthor author : authors) {
                if (author.getAdProject() == null || author.getAdProject().getAdGroup() == null) {
                    continue;
                }
                if (groupMaps.containsKey(author.getAdProject().getAdGroup().getGroupId())) {
                    ProjectExtPojo project = new ProjectExtPojo();
                    project.setProjectId(author.getAdProject().getProjectId());
                    project.setProjectName(author.getAdProject().getProjectName());
                    groupMaps.get(author.getAdProject().getAdGroup().getGroupId()).getProjects().add(project);
                } else {
                    GroupPojoExt groupExt = new GroupPojoExt();
                    groupExt.setGroupId(author.getAdProject().getAdGroup().getGroupId());
                    groupExt.setGroupName(author.getAdProject().getAdGroup().getGroupName());
                    List<ProjectExtPojo> projectExtList = new ArrayList<>();
                    ProjectExtPojo projectExt = new ProjectExtPojo();
                    projectExt.setProjectId(author.getAdProject().getProjectId());
                    projectExt.setProjectName(author.getAdProject().getProjectName());
                    projectExtList.add(projectExt);
                    groupExt.setProjects(projectExtList);
                    groupMaps.put(groupExt.getGroupId(), groupExt);
                }
            }
        }
        //增加自己新建的GROUP
        AdUser adUser = adUserDAO.getUserByLoginName(userName);
        if (null != adUser) {
            List<AdGroupUser> adGroupUsers = adGroupUserDAO.qryByUserName(userName);
            if (null != adGroupUsers && adGroupUsers.size() > 0) {
                for (AdGroupUser adGroupUser : adGroupUsers) {
                    if (groupMaps.containsKey(adGroupUser.getGroupId())) {
                        continue;
                    }
                    AdGroup group = adGroupDAO.qryAdGroupById(adGroupUser.getGroupId().intValue());
                    if (null != group) {
                        GroupPojoExt groupExt = new GroupPojoExt();
                        groupExt.setGroupId(group.getGroupId());
                        groupExt.setGroupName(group.getGroupName());
                        groupMaps.put(groupExt.getGroupId(), groupExt);
                    }
                }
            }
        }
        for (Map.Entry<Long, GroupPojoExt> entry : groupMaps.entrySet()) {
            groups.add(entry.getValue());
        }
        poj.setGroup(groups);
        return poj;
    }


    public List<AdProject> getAllSystem() {

        return projectDAO.getAllSystem();
    }

    public List<AdProject> qryProjectLikeName(String name) {
        return projectDAO.qryProjectLikeName(name);
    }

    public AdProject qryProjectByName(String name) {
        return projectDAO.qryProjectByName(name);
    }


}
