package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.comm.common.pojo.pojoExt.AdBranchCheckPojoExt;
import com.asiainfo.util.DateConvertUtils;
import com.asiainfo.util.StringUtil;
import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdProject;
import com.asiainfo.comm.module.models.functionModels.AdBranchList;
import com.asiainfo.comm.module.models.functionModels.query.QAdBranchList;
import com.asiainfo.comm.module.models.query.QAdBranch;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.avaje.ebean.Ebean.createSqlQuery;

/**
 * Created by yangry on 2016/6/16 0016.
 */
@Component
public class AdBranchDAO {

    public AdBranch getEnvById(long envId) {
        return new QAdBranch().branchId.eq(envId).fetch("adJenkinsInfo").findUnique();
    }

    public List<AdBranch> getEnvsBySysId(long system_id) {
        return new QAdBranch().adProject.projectId.eq(system_id).state.eq(1).findList();
    }

    public List<AdBranch> getAllEnv() {
        List<AdBranch> adBranchList = new QAdBranch().state.eq(1).orderBy("BRANCH_ID").findList();
        return adBranchList;
    }

    public List<AdBranch> getAllBranchInfo(int pageNum, int pageSize, String[] projects) {
        QAdBranch qAdBranch = new QAdBranch().state.eq(1);
        for (String project : projects) {
            qAdBranch = qAdBranch.or().adProject.projectId.eq(Long.valueOf(project));
        }
        List<AdBranch> adBranchList = qAdBranch.orderBy("BRANCH_ID").findPagedList(pageNum, pageSize).getList();
        return adBranchList;
    }

    public long getAllBranchCount(String[] projects) {
        QAdBranch qAdBranch = new QAdBranch().state.eq(1);
        for (String project : projects) {
            qAdBranch = qAdBranch.or().adProject.projectId.eq(Long.valueOf(project));
        }
        return qAdBranch.findRowCount();
    }

    public long qryCountAllBranch() {
        return new QAdBranch().state.eq(1).findRowCount();
    }

    public List<AdBranch> qryBranchByProject(long projectId) {
        List<AdBranch> adBranchList = new QAdBranch().state.eq(1)
            .adProject.projectId.eq(projectId).orderBy("BRANCH_ID").findList();
        return adBranchList;
    }

    public List<AdBranch> qryBranchByProjects(String[] projectId) {
        QAdBranch qry = new QAdBranch().state.eq(1);
        for (String project : projectId) {
            qry = qry.or().adProject.projectId.eq(Long.parseLong(project));
        }
        return qry.orderBy("BRANCH_ID").findList();
    }

    public List<AdBranch> qryBranchByProject(long projectId, int status) {
        List<AdBranch> adBranchList = new QAdBranch().state.eq(status)
            .adProject.projectId.eq(projectId).findList();
        return adBranchList;
    }

    public AdBranch saveAdBranch(AdBranch adBranch) {
        if (adBranch != null) {
            Ebean.save(adBranch);
        }
        return adBranch;
    }

    public AdBranch qryAdBranchByname(String name, String branchDesc, long projectId) {
        QAdBranch qAdBranch = new QAdBranch();
        List<AdBranch> adBranchList = null;
        if (StringUtils.isNotEmpty(branchDesc)) {
            qAdBranch = qAdBranch.state.eq(1);
            qAdBranch = qAdBranch.and();
            qAdBranch = qAdBranch.branchDesc.eq(branchDesc).and().adProject.projectId.eq(projectId);
            adBranchList = qAdBranch.findList();
        }
        if (adBranchList != null && adBranchList.size() > 0) {
            return adBranchList.get(0);
        } else {
            qAdBranch = new QAdBranch();
            qAdBranch = qAdBranch.state.eq(1);
            qAdBranch = qAdBranch.and();
            qAdBranch = qAdBranch.branchName.eq(name);
            adBranchList = qAdBranch.findList();
            if (adBranchList != null && adBranchList.size() > 0) {
                return adBranchList.get(0);
            } else {
                return null;
            }
        }
    }

    public AdBranch qryAdBranchByname(long branchId, String branchDesc, long projectId) {
        QAdBranch qAdBranch = new QAdBranch();
        qAdBranch = qAdBranch.state.eq(1).branchDesc.eq(branchDesc).adProject.projectId.eq(projectId);
        List<AdBranch> adBranchList = qAdBranch.findList();
        if (adBranchList != null && adBranchList.size() > 0 && adBranchList.get(0).getBranchId() != branchId) {
            return adBranchList.get(0);
        } else {
            return null;
        }
    }

    public AdBranch qryBranchByid(long branchId) {
        return new QAdBranch().branchId.eq(branchId).fetch("adProject").findUnique();
    }

    public AdBranch qryById(long branchId) {
        List<AdBranch> adBranchList = new QAdBranch().branchId.eq(branchId).state.eq(1).findList();
        if (adBranchList != null && adBranchList.size() > 0) {
            return adBranchList.get(0);
        } else {
            return null;
        }
    }

    public String qryBuildFilePath(long branchId, String commitId) {
        String path = "";
        AdBranch adBranch = new QAdBranch().branchId.eq(branchId).fetch("adProject").findUnique();
        if (adBranch != null) {
            AdProject adProject = adBranch.getAdProject();
            HanyuPinyinOutputFormat defaultFormat =
                new HanyuPinyinOutputFormat();
            StringUtil util = new StringUtil();
            if (adProject != null) {
                path = util.ChineseToPinyin(adProject.getAdGroup().getGroupName(), defaultFormat) + "/" + util.ChineseToPinyin(adProject.getProjectName(), defaultFormat) + "/"
                    + util.ChineseToPinyin(adBranch.getBranchName(), defaultFormat) + "/" + DateConvertUtils.date2String(new Date(), "yyyyMMddHHmmss") + "/" + commitId + "/";
            }
        }
        return path;
    }

    /**
     * @param projectId  要查询的应用d
     * @param branchType 流水类型
     * @return 查询到的结果
     */
    public List<SqlRow> qryBranchByProjectAndBranchtype(long projectId, String branchType) {
        String sql = "SELECT t.branch_id, t.branch_desc\n" +
            "  FROM ad_branch t\n" +
            " where t.state = 1\n" +
            " and t.branch_type in(:branchType)\n" +
            " and t.project_id =:projectId \n";
        String[] branchTypes = StringUtils.split(branchType, ",");
        List<Integer> types = new ArrayList<>();
        for (String type : branchTypes) {
            types.add(Integer.parseInt(type));
        }
        SqlQuery sqlQuery = createSqlQuery(sql).setParameter("branchType", types).setParameter("projectId", projectId);
        return sqlQuery.findList();
    }

    public long countAll() {
        return new QAdBranch().state.eq(1).findRowCount();
    }

    public long countCreateDate(Date date) {
        return new QAdBranch().state.eq(1).doneDate.after(date).findRowCount();
    }

    public List<SqlRow> countAllEnv() {
        String sql = "   select count(*) num from ad_branch a where state=1 and \n" +
            "not exists (select * from ad_stage b where b.state=1 and b.branch_id = a.branch_id and b.stage_code=4)\n";
        SqlQuery sqlQuery = createSqlQuery(sql);
        return sqlQuery.findList();
    }

    public List<SqlRow> countEnvCreateDate(Date date) {
        String sql = "   select count(*) num from ad_branch a where state=1 and \n" +
            "  done_date > :createDate and " +
            "not exists (select * from ad_stage b where b.state=1 and b.branch_id = a.branch_id and b.stage_code=4)\n";
        SqlQuery sqlQuery = createSqlQuery(sql);
        sqlQuery.setParameter("createDate", date);
        return sqlQuery.findList();
    }

    /**
     * 修改sql
     *
     * @param projectId 要查询的应用d
     * @param buildType 流水类型
     * @return 查询到的结果
     */
    public List<SqlRow> qryBranchByProecIdBranchType(long projectId, long buildType) {
        String sql = "SELECT t.branch_id, t.branch_desc\n" +
            "  FROM ad_branch t\n" +
            " where t.state = 1\n" +
            "   and t.project_id =:projectId \n" +
            "   and t.branch_type >=:buildType\n" +
            "   and t.branch_type <= 5";
        return Ebean.createSqlQuery(sql).setParameter("projectId", projectId).setParameter("buildType", buildType).findList();
    }

    public List<SqlRow> qryBranchByBranchType(long projectId, String branchType) {
        StringBuffer sql = new StringBuffer("");
        sql.append("SELECT branch_id, branch_type, branch_desc")
            .append("  FROM ad_branch")
            .append(" WHERE state = 1")
            .append("   AND project_id = :projectId")
            .append("   AND branch_type in (").append(branchType).append(")");
        return Ebean.createSqlQuery(sql.toString()).setParameter("projectId", projectId).findList();
    }


    public AdBranchList qryAllTriggerBranch(Long triggerBranch) {
        return new QAdBranchList().branchId.eq(triggerBranch).state.eq(1).findUnique();
    }

    public boolean qryBranchByEnvId(Long envId, String envType) {
        boolean flag = false;
        List<AdBranch> adBranchs = new QAdBranch().envType.eq(envType).envId.eq(envId).findList();
        if (CollectionUtils.isNotEmpty(adBranchs)) {
            flag = true;
        }
        return flag;
    }

    public List<AdBranchCheckPojoExt> qryEnvBranchByRegion(Long projectId, int region) {
        String branchType = "";
        List<AdBranchCheckPojoExt> adBranchCheckPojoExtList = new ArrayList<AdBranchCheckPojoExt>();
        if (region == 1) {
            branchType = "1,2,3,4,5";
        } else if (region == 2) {
            branchType = "1,2,3,4";
        } else {
            return adBranchCheckPojoExtList;
        }
        List<SqlRow> branchList = qryBranchByProjectAndBranchtype(projectId, branchType);
        if (CollectionUtils.isNotEmpty(branchList)) {
            AdBranchCheckPojoExt adBranchCheckPojoExt = null;
            for (SqlRow sqlRow : branchList) {
                adBranchCheckPojoExt = new AdBranchCheckPojoExt();
                adBranchCheckPojoExt.setBranchId(sqlRow.getString("branch_id"));
                adBranchCheckPojoExt.setBranchName(sqlRow.getString("branch_desc"));
                adBranchCheckPojoExtList.add(adBranchCheckPojoExt);
            }
        }
        return adBranchCheckPojoExtList;
    }

    /**
     * 根据projectId查询生产流水
     *
     * @param projectId 要查询的projectID
     * @return 返回查询到的branch名称和id
     */
    public List<SqlRow> qryBranchByProjectId(final long projectId) {
        String sql = "select branch_id as branchId,branch_name as branchName from ad_branch t where t.project_id=:projectId and branch_type=5";
        List<SqlRow> branchIds = Ebean.createSqlQuery(sql).setParameter("projectId", projectId).findList();
        if (CollectionUtils.isNotEmpty(branchIds)) {
            return branchIds;
        }
        return null;
    }

    public List<AdBranch> qryBranchRelateByEnvId(long envId) {
        List<AdBranch> adBranchs = new QAdBranch().envType.eq("vm").envId.eq(envId).state.eq(1).findList();
        return adBranchs;
    }

    public List<AdBranch> qryBranchRelateByEnvIddcos(long envId) {
        List<AdBranch> adBranchs = new QAdBranch().envType.eq("dcos").envId.eq(envId).state.eq(1).findList();
        return adBranchs;
    }

    /**
     * 获取有CRON任务的流水
     */
    public List<AdBranch> qryBranchCronExsit() {
        List<AdBranch> adBranches = new QAdBranch().state.eq(1).buildCron.isNotNull().findList();
        return adBranches;
    }
}
