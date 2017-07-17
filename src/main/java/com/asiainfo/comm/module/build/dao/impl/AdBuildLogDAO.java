package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.util.DateConvertUtils;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.models.query.QAdBuildLog;
import com.asiainfo.comm.module.models.query.QAdStageLogDtl;
import com.asiainfo.comm.module.role.dao.impl.AdUserRoleRelDAO;
import com.asiainfo.comm.module.common.AdStaticDataDAO;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.avaje.ebean.Ebean.createSqlQuery;

/**
 * Created by weif on 2016/6/15.
 */
@Component
public class AdBuildLogDAO {


    @Autowired
    AdStaticDataDAO bsStaticDataDAO;
    @Autowired
    AdUserRoleRelDAO adUserRoleRelDAO;
    @Autowired
    AdStageDAO adStageDAO;

    public void insertBuildLog(AdBuildLog infoSink) {
        if (infoSink != null) {
            Ebean.save(infoSink);
        }
    }

    public AdBuildLog qryBuildLog(int totalStep, long branchId) {
        List<AdBuildLog> adBuildLogList = new QAdBuildLog().totalStep.eq(totalStep).adBranch.branchId.eq(branchId).findList();
        if (adBuildLogList != null && adBuildLogList.size() > 0) {
            return adBuildLogList.get(0);
        } else {
            return null;
        }
    }

    public Map<String, List<Map>> qryBuildLog(List<AdPipeLineState> adSystemEnvList) {
        String sql = buildSqlToQueryLogOfAllBranches(adSystemEnvList);
        SqlQuery sqlQuery = createSqlQuery(sql);
        List<SqlRow> list = sqlQuery.findList();
        Map<String, List<Map>> retMap = new LinkedHashMap<>();
        for (SqlRow sqlRow : list) {
            String state = sqlRow.getInteger("build_result") == 2 ? "success" : "fail";
            List<Map> adBuildInfoSinkList;
            if (retMap.containsKey("" + sqlRow.getLong("branch_id"))) {
                adBuildInfoSinkList = retMap.get("" + sqlRow.getLong("branch_id"));
            } else {
                adBuildInfoSinkList = new ArrayList<>();
                retMap.put(sqlRow.getLong("branch_id") + "", adBuildInfoSinkList);
            }
            Map<String, String> hMap = new HashMap<>();
            hMap.put("state", state);
            hMap.put("date", "" + sqlRow.getString("build_date"));
            hMap.put("env_id", "" + sqlRow.getLong("branch_id"));
            hMap.put("id", "" + sqlRow.getInteger("total_step"));
            adBuildInfoSinkList.add(hMap);
        }
        return retMap;
    }

    @NotNull
    private String buildSqlToQueryLogOfAllBranches(List<AdPipeLineState> adSystemEnvList) {
        List<String> sqls = new ArrayList<>();
        for (AdPipeLineState adSystemEnv : adSystemEnvList) {
            sqls.add("select * from (SELECT build_result, to_char(build_date,'yyyy-mm-dd hh24:mi:ss') build_date,branch_id,total_step FROM ad_build_log t WHERE  t.build_date is not null and t.branch_id=" + adSystemEnv.getAdBranch().getBranchId() + " order by t.build_date desc) where rownum<6");
        }

        String sql = String.join(" union ", sqls);
        if (StringUtils.isNotEmpty(sql)) {
            sql = "select * from (" + sql + ") order by branch_id,build_date";
        }
        return sql;
    }

    public List<AdBuildLog> qryFailedBuilds() {
        return new QAdBuildLog().state.eq(1).buildResult.eq(3).orderBy(" BUILD_DATE DESC ").findList();
    }

    public List<SqlRow> qryPersonalFailedBuildsSqlRow(Long userId) {
        String sql = "SELECT DISTINCT A.STAGE_CODE,\n" +
            "       T.*,\n" +
            "       BRANCH.BRANCH_DESC,\n" +
            "       PRO.PROJECT_ID,\n" +
            "       PRO.PROJECT_NAME\n" +
            "  FROM AD_PROJECT PRO, AD_BUILD_LOG T, AD_STAGE A,AD_BRANCH BRANCH,AD_USER U,AD_USER_ROLE_REL REL,AD_AUTHOR AUTHOR\n" +
            " WHERE PRO.PROJECT_ID(+) = BRANCH.PROJECT_ID\n" +
            "   AND T.BRANCH_ID = BRANCH.BRANCH_ID(+)\n" +
            "   AND T.BRANCH_ID = A.BRANCH_ID(+)\n" +
            "   AND T.LAST_STEP = A.STEP\n" +
            "   AND U.LOGIN_NAME=REL.USER_NAME\n" +
            "   AND REL.ROLE_ID=AUTHOR.ROLE_ID\n" +
            "   AND PRO.PROJECT_ID=AUTHOR.PROJECT_ID\n" +
            "   AND T.BUILD_RESULT = 3\n" +
            "   AND T.STATE = 1\n" +
            "   AND U.USER_ID = " + userId + "\n" +
            " ORDER BY T.BUILD_DATE DESC";
        SqlQuery sqlQuery = createSqlQuery(sql);
        List<SqlRow> list = sqlQuery.findList();
        return list;
    }

    public List<SqlRow> qryLastFailedBuildsSqlRow(String loginName) {
        List<AdUserRoleRel> userRoleRelList = adUserRoleRelDAO.qryByUser(loginName);
        boolean isAdmin = false;
        for (AdUserRoleRel rel : userRoleRelList) {
            if (rel.getAdRole().getRoleLevel() == 0) {
                isAdmin = true;
                break;
            }
        }
        String sql = "SELECT distinct J.GROUP_ID,J.GROUP_NAME,D.BRANCH_ID,D.BRANCH_DESC,A.PROJECT_ID,A.PROJECT_NAME,B.LAST_STEP,C.STAGE_CODE,B.BUILD_DATE,B.BUILD_RESULT " +
            "FROM AD_PROJECT A, AD_BUILD_LOG B, AD_STAGE C,AD_BRANCH D,(SELECT max(t.build_id) build_id,t.branch_id branch_id FROM ad_build_log t group by t.branch_id) E,AD_GROUP_USER G,AD_GROUP J\n" +
            "WHERE  C.STATE=1 AND C.STAGE_ID=B.LAST_STAGE_ID(+) " +
            " AND D.PROJECT_ID=A.PROJECT_ID" +
            " AND A.GROUP_ID=G.GROUP_ID" +
            " AND A.GROUP_ID=J.GROUP_ID" +
            " AND B.BUILD_ID = E.BUILD_ID" +
            " AND B.BRANCH_ID=D.BRANCH_ID";
        if (!isAdmin) {
            sql = sql + " AND G.USER_NAME='" + loginName + "'";
        }
        sql = sql + " ORDER BY B.BUILD_DATE DESC";
        SqlQuery sqlQuery = createSqlQuery(sql);
        List<SqlRow> list = sqlQuery.findList();
        return list;
    }

    public Map<String, Object> qryBuildLogList(long op_id, int dealresult, java.sql.Date begin_date, java.sql.Date end_date, int pagenum, int pageSize, long branchId, int buildType) {
        QAdBuildLog qadBuildLog;
        long total = 0;
        if (op_id != 0) {
            qadBuildLog = new QAdBuildLog().adUser.userId.eq(op_id);
        } else {
            qadBuildLog = new QAdBuildLog();
        }
        if (dealresult != 0 && dealresult == 100) {//取所有成功的
            qadBuildLog = qadBuildLog.buildResult.eq(2);
        }
        if (dealresult != 0 && dealresult == 101) {//取所有失败的
            qadBuildLog = qadBuildLog.buildResult.eq(3);
        }
        if (begin_date != null) {
            qadBuildLog = qadBuildLog.buildDate.ge(begin_date);
        }
        if (end_date != null) {
            qadBuildLog = qadBuildLog.buildDate.le(end_date);
        }
        if (branchId != 0) {
            qadBuildLog = qadBuildLog.adBranch.branchId.eq(branchId);
        }
        if (buildType != 0) {
            qadBuildLog = qadBuildLog.buildType.eq(buildType);
        }
        List<AdStageLogDtl> adstagelogList = null;
        if (dealresult != 0 && dealresult != 100 && dealresult != 101) {
            qadBuildLog = qadBuildLog.buildResult.eq(3);
            adstagelogList = new QAdStageLogDtl().adStage.stageId.eq(dealresult).stageResult.eq("3").adBranch.branchId.eq(branchId).findList();
            if (adstagelogList != null && adstagelogList.size() > 0) {
                qadBuildLog = qadBuildLog.and();
                for (AdStageLogDtl adStageLogDtl : adstagelogList) {
                    qadBuildLog = qadBuildLog.or().totalStep.eq(adStageLogDtl.getTotalStep().intValue());
                }
                qadBuildLog = qadBuildLog.endAnd();
            } else {
                qadBuildLog = qadBuildLog.totalStep.eq(1000);
            }
        }
        Map<String, Object> retMap = new HashMap<String, Object>();
        Map<String, String> logMap = null;
        String BranchName = "";
        Map<String, String> bsStaticData = bsStaticDataDAO.qryStaticDatas("DEALRESULT");
        Map<String, String> BUILD_TYPE = bsStaticDataDAO.qryStaticDatas("BUILD_TYPE");
        List<Map<String, String>> adBuildLogs = new ArrayList<Map<String, String>>();
        List<AdBuildLog> adBuildLogList = qadBuildLog.fetch("adUser").fetch("adBranch").orderBy().createDate.desc().findPagedList(pagenum, pageSize).getList();
        total = qadBuildLog.fetch("adUser").fetch("adBranch").findPagedList(pagenum, pageSize).getTotalRowCount();
        if (adBuildLogList != null) {
            for (AdBuildLog adBuildLog : adBuildLogList) {
                logMap = new HashMap<String, String>();
                logMap.put("user", adBuildLog.getAdUser().getDisplayName());
                logMap.put("date", "" + DateConvertUtils.date2String(adBuildLog.getBuildDate(), "yyyy-MM-dd HH:mm:ss"));
                logMap.put("env_id", "" + adBuildLog.getAdBranch().getBranchId());
                if (bsStaticData.containsKey("" + adBuildLog.getBuildResult()))
                    logMap.put("state", "" + bsStaticData.get("" + adBuildLog.getBuildResult()));
                else
                    logMap.put("state", "" + adBuildLog.getBuildResult());
                logMap.put("id", "" + adBuildLog.getTotalStep());
                if (BUILD_TYPE.containsKey("" + adBuildLog.getBuildType())) {
                    logMap.put("buildType", BUILD_TYPE.get("" + adBuildLog.getBuildType()));
                } else {
                    logMap.put("buildType", "" + adBuildLog.getBuildType());
                }
                BranchName = adBuildLog.getAdBranch().getBranchName();
                adBuildLogs.add(logMap);
            }
        }
        retMap.put("buildHis", adBuildLogs);
        retMap.put("branchid", branchId);
        retMap.put("name", BranchName);
        retMap.put("total", total);
        return retMap;
    }

    public Map<String, Object> qryBulidReport(long op_id, int dealresult, java.sql.Date begin_date, java.sql.Date end_date, long branchId) {
        QAdBuildLog qadBuildLog;
        if (op_id != 0) {
            qadBuildLog = new QAdBuildLog().adUser.userId.eq(op_id);
        } else {
            qadBuildLog = new QAdBuildLog();
        }
        if (dealresult != 0 && dealresult == 100) {//取所有成功的
            qadBuildLog = qadBuildLog.buildResult.eq(2);
        }
        if (dealresult != 0 && dealresult == 101) {//取所有失败的
            qadBuildLog = qadBuildLog.buildResult.eq(3);
        }
        if (begin_date != null) {
            qadBuildLog = qadBuildLog.buildDate.ge(begin_date);
        }
        if (end_date != null) {
            qadBuildLog = qadBuildLog.buildDate.le(end_date);
        }
        if (branchId != 0) {
            qadBuildLog = qadBuildLog.adBranch.branchId.eq(branchId);
        }
        Map<String, Object> retMap = new HashMap<String, Object>();
        Map<String, String> logMap = null;
        List<Map<String, String>> adBuildLogs = new ArrayList<Map<String, String>>();
        List<AdBuildLog> adBuildLogList = qadBuildLog.fetch("adUser").fetch("adBranch").orderBy().createDate.desc().adBranch.branchId.eq(branchId).findList();
        if (adBuildLogList != null) {
            for (AdBuildLog adBuildLog : adBuildLogList) {
                logMap = new HashMap<String, String>();
                logMap.put("buildId", String.valueOf(adBuildLog.getBuildId()));
                logMap.put("totalStep", "" + adBuildLog.getTotalStep());
                logMap.put("optName", adBuildLog.getAdUser().getDisplayName());
                logMap.put("branchId", "" + adBuildLog.getAdBranch().getBranchId());
                logMap.put("dealtime", String.valueOf((adBuildLog.getBuildDate().getTime() - adBuildLog.getCreateDate().getTime()) / 1000));
                if (adBuildLog.getBuildResult() == 2) {
                    logMap.put("dealResult", "成功");
                } else if (adBuildLog.getBuildResult() == 3) {
                    logMap.put("dealResult", "失败");
                }
                logMap.put("startDate", DateConvertUtils.date2String(adBuildLog.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
                logMap.put("endDate", DateConvertUtils.date2String(adBuildLog.getBuildDate(), "yyyy-MM-dd HH:mm:ss"));
                adBuildLogs.add(logMap);
            }
        }
        retMap.put("data", adBuildLogs);
        return retMap;
    }


    public Map<String, Object> qryBulidReportdtl(int li_build_id, int branchId) {
        QAdBuildLog qadBuildLog;
        if (li_build_id != 0) {
            qadBuildLog = new QAdBuildLog().buildId.eq(li_build_id);
        } else {
            qadBuildLog = new QAdBuildLog();
        }
        Map<String, Object> retMap = new HashMap<String, Object>();
        Map<String, Object> logMap = new HashMap<String, Object>();
        List<Map<String, Object>> adStageLogs = new ArrayList<Map<String, Object>>();
        List<AdBuildLog> adBuildLogList = qadBuildLog.findList();
        int totalStep = 0;
        if (adBuildLogList != null) {
            for (AdBuildLog adBuildLog : adBuildLogList) {
                totalStep = adBuildLog.getTotalStep();
            }
        }
        QAdStageLogDtl qadStageLogDtl;
        if (totalStep != 0) {
            qadStageLogDtl = new QAdStageLogDtl().totalStep.eq(totalStep);
        } else {
            qadStageLogDtl = new QAdStageLogDtl();
        }
        List<AdStageLogDtl> adStageLogDtlList = qadStageLogDtl.orderBy().beginDate.desc().findList();
        Map<String, String> staticdataMap = new HashMap<String, String>();
        Map<String, String> operationMap = new HashMap<String, String>();
        List<AdStaticData> adStaticDatas = bsStaticDataDAO.qryByCodeType("BUILDER_TYPE");
        if (adStaticDatas != null) {
            for (AdStaticData adStaticData : adStaticDatas) {
                staticdataMap.put(adStaticData.getCodeValue(), adStaticData.getCodeName());
            }
        }
        String codeName = null;
        String buildType = null;
        for (AdStageLogDtl adStageLogDtl : adStageLogDtlList) {
            if (branchId != 0) {
                List<AdStage> adStageList = adStageDAO.QryAdOperationByEnvId("" + branchId);
                if (adStageList != null) {
                    for (AdStage adStage : adStageList) {
                        logMap = new HashMap<String, Object>();
                        operationMap.put(adStage.getStageId() + "", adStage.getStageCode() + "");
                        codeName = "";
                        if (operationMap.size() > 0 && staticdataMap.size() > 0) {
                            buildType = operationMap.get("" + adStageLogDtl.getAdStage().getStageId());
                            codeName = staticdataMap.get(buildType);
                        }
                        logMap.put("name", "" + codeName);
                        if (adStageLogDtl.getFinishDate() != null && adStageLogDtl.getBeginDate() != null) {
                            Long a = adStageLogDtl.getFinishDate().getTime();
                            Long b = adStageLogDtl.getBeginDate().getTime();
                            logMap.put("value", (int) ((a - b) / 1000));
                        } else {
                            logMap.put("value", 0);
                        }
                    }
                }
                adStageLogs.add(logMap);
            }
        }
        retMap.put("stepTimePercent", adStageLogs);
        return retMap;
    }


    public List<AdBuildLog> qryBuildLog(long branchId) {
        return new QAdBuildLog().adBranch.branchId.eq(branchId).orderBy(" BUILD_ID DESC").findList();
    }


    public List<SqlRow> qryAvgTimeByBranchId(long branchId) {
        String sql = "select NVL(avg(build_date-create_date)*24*3600,100) avgtime from (select build_date,create_date from ad_build_log where branch_id=:branchId and state=1 and build_result=2 and create_date is not null and build_date is not null order by build_id desc) where rownum<=10";
        SqlQuery sqlQuery = createSqlQuery(sql);
        sqlQuery.setParameter("branchId", branchId);
        return sqlQuery.findList();
    }


    public long qryAvgBuildTime() {
        String sql = " select round(avg(build_date - create_date) * 24 * 3600)   avgtime\n" +
            "   from ad_build_log t  where t.create_date is not null and t.build_date is not null and t.build_result=2";
        SqlRow avgBuildTime = Ebean.createSqlQuery(sql).findUnique();
        return avgBuildTime.getLong("avgtime");
    }

    public long qryCountAllBuildNum() {
        return new QAdBuildLog().or().buildResult.eq(2).or().buildResult.eq(3).findRowCount();
    }

    public List<SqlRow> qryCurrentBuildLog() {
        List<SqlRow> adBuildLogList;
        String sql = "select f.build_result ,\n" +
            "       f.build_type  ,\n" +
            "       f.create_date  ,\n" +
            "       f.branch_desc  ,\n" +
            "       f.group_name  ,\n" +
            "       f.display_name  \n" +
            "  from (SELECT a.build_result,\n" +
            "               a.build_type,\n" +
            "               a.create_date,\n" +
            "               b.branch_desc,\n" +
            "               d.group_name,\n" +
            "               e.display_name\n" +
            "          FROM ad_build_log a,\n" +
            "               ad_branch    b,\n" +
            "               ad_project  c,\n" +
            "               ad_group     d,\n" +
            "               ad_user      e\n" +
            "         where a.branch_id = b.branch_id\n" +
            "           and a.op_id = e.user_id\n" +
            "           and b.project_id = c.project_id\n" +
            "           and c.group_id = d.group_id\n" +
            "           and a.build_result in (2, 3)\n" +
            "         order by a.create_date desc) f\n" +
            " where rownum<9";
        adBuildLogList = Ebean.createSqlQuery(sql).findList();
        return adBuildLogList;
    }

}
