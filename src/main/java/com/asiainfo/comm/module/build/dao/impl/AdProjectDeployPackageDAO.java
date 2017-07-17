package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.module.models.AdProjectDeployPackage;
import com.asiainfo.comm.module.models.query.QAdProjectDeployPackage;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YangRY on 2016/8/4.
 */
@Component
public class AdProjectDeployPackageDAO {
    public void save(List<AdProjectDeployPackage> projectDeployPackage) {
        for (AdProjectDeployPackage deployPackage : projectDeployPackage) {
            deployPackage.save();
        }
    }

    //根据commitId获取包
    public List<AdProjectDeployPackage> qryByCommitId(String commitId) {
        return new QAdProjectDeployPackage().commitId.eq(commitId).ext1.eq("success").findList();
    }

    //根据commitId和branchId获取包
    public List<AdProjectDeployPackage> qryByCommitAndBranch(String commitId, Long branchId) {
        return new QAdProjectDeployPackage().commitId.eq(commitId).adBranch.branchId
            .eq(branchId).ext1.eq("success").findList();
    }

    public void save(AdProjectDeployPackage projectDeployPackage) {
        projectDeployPackage.save();
    }

    public List<AdProjectDeployPackage> qryByBranchId(long branchId) {
        return new QAdProjectDeployPackage().adBranch.branchId.eq(branchId).ext1.eq("success").orderBy().createDate.desc().findList();
    }

    public AdProjectDeployPackage qryByPackageId(long packageId) {
        List<AdProjectDeployPackage> adProjectDeployPackageList = new QAdProjectDeployPackage().packageId.eq(packageId).findList();
        if (adProjectDeployPackageList != null && adProjectDeployPackageList.size() > 0) {
            return adProjectDeployPackageList.get(0);
        }
        return null;
    }

    public AdProjectDeployPackage qryById(Long packageId) {
        return new QAdProjectDeployPackage().packageId.eq(packageId).findUnique();
    }

    public void delete(Long packageId) {
        AdProjectDeployPackage adProjectDeployPackage = new QAdProjectDeployPackage().packageId.eq(packageId).findUnique();
        adProjectDeployPackage.delete();
    }

    public void deleteBycommitAndbranchId(Long branchId, String commitId) {
        String sql = "delete devopsdb.ad_project_deploy_package t where  t.commit_id= :commitId and t.branch_id= :branchId";
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        update.setParameter("commitId", commitId);
        update.setParameter("branchId", branchId);
        Ebean.execute(update);
    }

    public List<SqlRow> qryByBranchs(String branchId) {
        String[] branchIds = StringUtils.split(branchId, ",");
        List<Integer> ids = new ArrayList<>();
        for (String type : branchIds) {
            ids.add(Integer.parseInt(type));
        }
        String sql = "SELECT * from ad_project_deploy_package a where a.branch_id in(:branchId) and a.ext1='success' order by branch_id,create_date desc";
        List<SqlRow> sqlRow = Ebean.createSqlQuery(sql).setParameter("branchId", ids).findList();
        return sqlRow;
    }
    public List<SqlRow> qryByBranchsDesc(String branchId) {
        String[] branchIds = StringUtils.split(branchId, ",");
        List<Integer> ids = new ArrayList<>();
        for (String type : branchIds) {
            ids.add(Integer.parseInt(type));
        }
        String sql = "SELECT * from ad_project_deploy_package a where a.branch_id in(:branchId) and a.ext1='success' order by create_date desc";
        List<SqlRow> sqlRow = Ebean.createSqlQuery(sql).setParameter("branchId", ids).findList();
        return sqlRow;
    }
}
