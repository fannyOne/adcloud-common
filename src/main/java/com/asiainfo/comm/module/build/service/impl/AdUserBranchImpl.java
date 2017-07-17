package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.WorkingSpacePojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.WorkingSpacePojo;
import com.asiainfo.comm.module.build.dao.impl.AdUserBranchDAO;
import com.asiainfo.comm.module.models.AdUserBranch;
import com.asiainfo.comm.module.models.query.QAdUserBranch;
import com.avaje.ebean.SqlRow;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YangRY on 2016/8/30.
 */
@Component
public class AdUserBranchImpl {
    @Autowired
    AdUserBranchDAO userBranchDAO;

    public WorkingSpacePojo qryWorkingSpaceByUser(Long userId) {
        WorkingSpacePojo workingSpacePojo = new WorkingSpacePojo();
        List<WorkingSpacePojoExt> extList = new ArrayList<>();
        List<SqlRow> userBranchList = userBranchDAO.qryWorkingSpaceByUser(userId);
        if (userBranchList != null) {
            for (SqlRow sqlRow : userBranchList) {
                WorkingSpacePojoExt ext = new WorkingSpacePojoExt();
                ext.setBranchId(sqlRow.getLong("BRANCH_ID"));
                String branchDesc = sqlRow.getString("BRANCH_DESC");
                ext.setName(StringUtils.isNotEmpty(branchDesc) ? (sqlRow.getString("GROUP_NAME") + "-" + sqlRow.getString("PROJECT_NAME") + branchDesc) :
                    (sqlRow.getString("GROUP_NAME") + "-" + sqlRow.getString("PROJECT_NAME") + sqlRow.getString("BRANCH_NAME")));
                extList.add(ext);
            }
        }
        workingSpacePojo.setMyBranch(extList);
        return workingSpacePojo;
    }

    public WorkingSpacePojo qryWorkingSpaceByUser(String userName) {
        WorkingSpacePojo workingSpacePojo = new WorkingSpacePojo();
        List<WorkingSpacePojoExt> extList = new ArrayList<>();
        List<SqlRow> userBranchList = userBranchDAO.qryWorkingSpaceByUser(userName);
        if (userBranchList != null) {
            for (SqlRow sqlRow : userBranchList) {
                WorkingSpacePojoExt ext = new WorkingSpacePojoExt();
                ext.setBranchId(sqlRow.getLong("BRANCH_ID"));
                String branchDesc = sqlRow.getString("BRANCH_DESC");
                ext.setName(StringUtils.isNotEmpty(branchDesc) ? (sqlRow.getString("GROUP_NAME") + "-" + sqlRow.getString("PROJECT_NAME") + branchDesc) :
                    (sqlRow.getString("GROUP_NAME") + "-" + sqlRow.getString("PROJECT_NAME") + sqlRow.getString("BRANCH_NAME")));
                extList.add(ext);
            }
        }
        workingSpacePojo.setMyBranch(extList);
        return workingSpacePojo;
    }

    public WorkingSpacePojo qryWorkingSpaceByAdminUser(Long userId) {
        WorkingSpacePojo workingSpacePojo = new WorkingSpacePojo();
        List<WorkingSpacePojoExt> extList = new ArrayList<>();
        List<AdUserBranch> userBranchList = userBranchDAO.qryWorkingSpaceByAdminUser(userId);
        if (userBranchList != null) {
            for (AdUserBranch userBranch : userBranchList) {
                WorkingSpacePojoExt ext = new WorkingSpacePojoExt();
                ext.setBranchId(userBranch.getAdBranch().getBranchId());
                String branchDesc = userBranch.getAdBranch().getBranchDesc();
                ext.setName(StringUtils.isNotEmpty(branchDesc) ? (userBranch.getAdBranch().getAdProject().getAdGroup().getGroupName() + "-" + userBranch.getAdBranch().getAdProject().getProjectName() + branchDesc) :
                    (userBranch.getAdBranch().getAdProject().getAdGroup().getGroupName() + "-" + userBranch.getAdBranch().getAdProject().getProjectName() + userBranch.getAdBranch().getBranchName()));
                extList.add(ext);
            }
        }
        workingSpacePojo.setMyBranch(extList);
        return workingSpacePojo;
    }

    public WorkingSpacePojo qryWorkingSpaceByAdminUser(String userName) {
        WorkingSpacePojo workingSpacePojo = new WorkingSpacePojo();
        List<WorkingSpacePojoExt> extList = new ArrayList<>();
        List<AdUserBranch> userBranchList = new QAdUserBranch().adBranch.state.eq(1).state.eq(1).userName.eq(userName).findList();
        if (userBranchList != null) {
            for (AdUserBranch userBranch : userBranchList) {
                WorkingSpacePojoExt ext = new WorkingSpacePojoExt();
                ext.setBranchId(userBranch.getAdBranch().getBranchId());
                String branchDesc = userBranch.getAdBranch().getBranchDesc();
                ext.setName(StringUtils.isNotEmpty(branchDesc) ? (userBranch.getAdBranch().getAdProject().getAdGroup().getGroupName() + "-" + userBranch.getAdBranch().getAdProject().getProjectName() + branchDesc) :
                    (userBranch.getAdBranch().getAdProject().getAdGroup().getGroupName() + "-" + userBranch.getAdBranch().getAdProject().getProjectName() + userBranch.getAdBranch().getBranchName()));
                extList.add(ext);
            }
        }
        workingSpacePojo.setMyBranch(extList);
        return workingSpacePojo;
    }

    public List<AdUserBranch> qryByUserAndBranch(Long branchId, Long userId) {
        return new QAdUserBranch().state.eq(1).adBranch.branchId.eq(branchId).adUser.userId.eq(userId).findList();
    }

    public List<AdUserBranch> qryByUserAndBranch(Long branchId, String username) {
        return new QAdUserBranch().state.eq(1).adBranch.branchId.eq(branchId).userName.eq(username).findList();
    }

    public void del(List<AdUserBranch> userBranchList) {
        if (userBranchList != null && userBranchList.size() > 0) {
            for (int i = 0; i < userBranchList.size(); i++) {
                userBranchList.get(i).setState(0);
            }
            userBranchDAO.update(userBranchList);
        }
    }
}
