package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdProject;
import com.asiainfo.comm.module.models.query.QAdProject;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangry on 2016/6/16 0016.
 */
@Component
public class AdProjectDAO {

    public AdProject getSystemById(long systemId) {
        return new QAdProject().projectId.eq(systemId).findUnique();
    }

    public AdProject getValidSystemById(long systemId) {
        return new QAdProject().state.eq(1).projectId.eq(systemId).findUnique();
    }

    public List<AdProject> getAllSystem() {
        return new QAdProject().state.eq(1).findList();
    }

    public Map<String, String> getSystems() {
        List<AdProject> adProjectList = new QAdProject().state.eq(1).findList();
        Map<String, String> retMap = new HashMap<String, String>();
        if (adProjectList != null) {
            for (AdProject adProject : adProjectList) {
                retMap.put(adProject.getProjectId() + "", adProject.getProjectName());
            }
        }
        return retMap;
    }

    public AdProject saveAdProject(AdProject adProject) {
        if (adProject != null) {
            Ebean.save(adProject);
        }
        return adProject;
    }

    public List<AdProject> qryProjectByGroupId(int groupId) {
        return new QAdProject().state.eq(1).fetch("adGroup").adGroup.groupId.eq(groupId).projectId.asc().findList();
    }

    public AdProject qryById(long value) {
        List<AdProject> projectList = new QAdProject().state.eq(1).projectId.eq(value).findList();
        if (projectList != null && projectList.size() > 0) {
            return projectList.get(0);
        } else {
            return null;
        }
    }


    public int qryRowById(long value) {
        return new QAdProject().state.eq(1).projectId.eq(value).findRowCount();
    }

    public int qryRowByIds(List<String> projectIds) {
        return Ebean.find(AdProject.class).where()
            .eq("STATE", "1")
            .in("PROJECT_ID", projectIds).findRowCount();
    }

    public List<AdProject> qryByIds(List<String> projectIds) {
        return Ebean.find(AdProject.class).where()
            .eq("STATE", "1")
            .in("PROJECT_ID", projectIds).findList();
    }

    /**
     * 根据应用名模糊查询
     *
     * @param projectName 查询的应用名
     * @return 返回查到的应用名
     */
    public List<AdProject> qryProjectLikeName(String projectName) {
        List<AdProject> adProjects = new QAdProject().state.eq(1).projectName.like("%" + projectName + "%").findList();
        if (CollectionUtils.isNotEmpty(adProjects)) {
            return adProjects;
        }
        return null;    //查询不到返回null
    }

    /**
     * @param projectName 要查询的系统名
     * @return 查询的详细信息
     */
    public AdProject qryProjectByName(String projectName) {
        AdProject adProject = new QAdProject().projectName.eq(projectName).findUnique();
        if (adProject != null) {
            return adProject;
        }
        return null;
    }

}
