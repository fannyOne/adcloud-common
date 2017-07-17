package com.asiainfo.comm.module.role.dao.impl;

import com.asiainfo.comm.common.pojo.pojoMaster.CommonPojo;
import com.asiainfo.comm.module.models.AdAuthor;
import com.asiainfo.comm.module.models.AdProject;
import com.asiainfo.comm.module.models.AdRole;
import com.asiainfo.comm.module.models.query.QAdAuthor;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by YangRY
 * 2016/7/11 0011.
 */
@Component
public class AdAuthorDAO {
    @Autowired
    AdRoleDAO roleDAO;

    public List<AdAuthor> qryAuthorByRole(long roleId) {
        return new QAdAuthor().adRole.roleId.eq(roleId).adProject.state.eq(1).state.eq(1).findList();
    }

    public List<SqlRow> qryAuthorAndSignCheck(long roleId) {
        String sql = "SELECT T.*,NVL(S.STATE,0) AS IS_CHECKED FROM AD_PROJECT T,AD_AUTHOR S WHERE S.PROJECT_ID(+) = T.PROJECT_ID AND S.ROLE_ID(+) =:roleId AND T.STATE = 1" +
            " ORDER BY PROJECT_NAME ASC";
        List<SqlRow> authorRows = Ebean.createSqlQuery(sql).setParameter("roleId", roleId).findList();
        return authorRows;
    }

    public CommonPojo changeRoleAuthor(long roleId, String projectIds) {
        String[] projectIdStr = projectIds.trim().split(",");
        List<AdAuthor> authors = qryAuthorByRole(roleId);
        AdRole role = roleDAO.qryById(roleId);
        HashSet<Long> projectHashes = new HashSet<>();
        for (String projectId : projectIdStr) {
            Long projectIdLong = Long.parseLong(projectId);
            if (!projectHashes.contains(projectIdLong)) {
                projectHashes.add(projectIdLong);
            }
        }
        for (AdAuthor author : authors) {
            if (!projectHashes.contains(author.getAdProject().getProjectId())) {
                author.delete();
            } else {
                projectHashes.remove(author.getAdProject().getProjectId());
            }
        }
        for (Long projectHash : projectHashes) {
            AdAuthor author = new AdAuthor();
            AdProject project = new AdProject();
            project.setProjectId(projectHash);
            author.setState(1);
            author.setAdRole(role);
            author.setAdProject(project);
            author.setCreateDate(new Date());
            author.save();
        }
        return new CommonPojo();
    }

    public List<AdAuthor> qryroleAndSignCheck(long roleId, long projectId) {
        QAdAuthor qAdAuthor = new QAdAuthor().state.eq(1).adRole.roleId.eq(roleId);
        if (projectId != 0) {
            qAdAuthor = qAdAuthor.adProject.projectId.eq(projectId);
        }
        List<AdAuthor> adAuthorList = qAdAuthor.findList();
        return adAuthorList;
    }

}
