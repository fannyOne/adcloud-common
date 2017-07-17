package com.asiainfo.comm.module.deploy.dao.impl;

import com.asiainfo.util.DateConvertUtils;
import com.asiainfo.comm.module.models.AdSystemDeployLog;
import com.asiainfo.comm.module.models.query.QAdSystemDeployLog;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.avaje.ebean.Ebean.createSqlQuery;

/**
 * Created by GUOJIAN on 7/26/16.
 */
@Component
public class SystemDeployLogDAO {
    /**
     * 根据system id查询当前系统的发布历史日志
     *
     * @param id
     * @return
     */
    public List<AdSystemDeployLog> getLogsBySystemId(Long id) {
        List<AdSystemDeployLog> logs = new QAdSystemDeployLog().projectId.eq(id).findList();
        return logs;
    }

    /**
     * 根据system id写入当前系统的发布历史日志
     *
     * @param log
     */
    public void addLogsBySystemId(AdSystemDeployLog log) {
        Ebean.save(log);
    }

    /**
     * 存储记录并返回ID
     */
    public long saveAndReturn(AdSystemDeployLog log) {
        String sql = "SELECT AD_SYSTEMDEPLOY_LOG_SEQ.NEXTVAL AS LOG_ID FROM DUAL";
        SqlRow sqlRow = Ebean.createSqlQuery(sql).findUnique();
        long logId = sqlRow.getLong("LOG_ID");
        log.setLogId(logId);
        Ebean.save(log);
        return logId;
    }

    public Map<String, Object> qrySystemDeployLogs(AdSystemDeployLog adSystemDeployLog, int pageNum, int pageSize, long op_id) {
        Map<String, Object> hmap = new HashMap<String, Object>();
        QAdSystemDeployLog qAdSystemDeployLog = new QAdSystemDeployLog();
        if (adSystemDeployLog.getDeployResult() != null && adSystemDeployLog.getDeployResult() != 0) {
            qAdSystemDeployLog = qAdSystemDeployLog.deployResult.eq(adSystemDeployLog.getDeployResult());
        }
        if (adSystemDeployLog.getStartTime() != null) {
            qAdSystemDeployLog = qAdSystemDeployLog.runTime.ge(adSystemDeployLog.getStartTime());
        }
        if (adSystemDeployLog.getEndTime() != null) {
            qAdSystemDeployLog = qAdSystemDeployLog.runTime.le(adSystemDeployLog.getEndTime());
        }
        if (op_id != 0) {
            qAdSystemDeployLog = qAdSystemDeployLog.adUser.userId.eq(op_id);
        }
        if (adSystemDeployLog.getProjectId() != 0) {
            qAdSystemDeployLog = qAdSystemDeployLog.projectId.eq(adSystemDeployLog.getProjectId());
        }
        if (!("0").equals(adSystemDeployLog.getDeployType())) {
            qAdSystemDeployLog = qAdSystemDeployLog.deployType.eq(adSystemDeployLog.getDeployType());
        }
        if (adSystemDeployLog.getEnvId() != null) {
            qAdSystemDeployLog = qAdSystemDeployLog.envId.eq(adSystemDeployLog.getEnvId());
        }
        if (adSystemDeployLog.getHostType() != null) {
            qAdSystemDeployLog = qAdSystemDeployLog.hostType.eq(adSystemDeployLog.getHostType());
        }
        if (adSystemDeployLog.getOperType() != null && adSystemDeployLog.getOperType() != 0) {
            qAdSystemDeployLog = qAdSystemDeployLog.operType.eq(adSystemDeployLog.getOperType());
        }
        qAdSystemDeployLog = qAdSystemDeployLog.orderBy("LOG_ID DESC");
        Map<String, String> logMap = null;
        List<Map<String, String>> logList = new ArrayList<Map<String, String>>();
        List<AdSystemDeployLog> adSystemDeployLogList = qAdSystemDeployLog.findPagedList(pageNum, pageSize).getList();
        StringBuffer xjsb = new StringBuffer();
        StringBuffer dcossb = new StringBuffer();
        if (adSystemDeployLogList != null) {
            for (AdSystemDeployLog adSystemDeployLog1 : adSystemDeployLogList) {
                logMap = new HashMap<String, String>();
                logMap.put("id", "" + adSystemDeployLog1.getLogId());
                logMap.put("startTime", "" + DateConvertUtils.date2String(adSystemDeployLog1.getRunTime(), "yyyy-MM-dd HH:mm:ss"));
                logMap.put("endTime", "" + DateConvertUtils.date2String(adSystemDeployLog1.getEndTime(), "yyyy-MM-dd HH:mm:ss"));
                logMap.put("ip", "" + adSystemDeployLog1.getIp());
                logMap.put("deployResult", "" + adSystemDeployLog1.getDeployResult());
                logMap.put("deployType", "" + adSystemDeployLog1.getDeployType());
                logMap.put("deployComment", "" + adSystemDeployLog1.getDeployComment());
                logMap.put("operatorName", adSystemDeployLog1.getAdUser().getDisplayName() + "");
                logMap.put("operType", "" + adSystemDeployLog1.getOperType());
                logMap.put("envId", "" + adSystemDeployLog1.getEnvId());
                logMap.put("hostType", "" + adSystemDeployLog1.getHostType());
                if (adSystemDeployLog1.getHostType() != null && adSystemDeployLog1.getHostType() == 2) {
                    dcossb.append("" + adSystemDeployLog1.getEnvId() + ",");
                } else {
                    xjsb.append("" + adSystemDeployLog1.getEnvId() + ",");
                }

                logList.add(logMap);
            }
            String xjendIds = null;
            String dcosendIds = null;
            xjendIds = xjsb.toString();
            dcosendIds = dcossb.toString();
            if (xjendIds.length() > 0) {
                xjendIds = xjendIds.substring(0, xjendIds.length() - 1);
                logList = qryEnvNameById(xjendIds, logList, "1");

            }
            if (dcosendIds.length() > 0) {
                dcosendIds = dcosendIds.substring(0, dcosendIds.length() - 1);
                logList = qryEnvNameById(dcosendIds, logList, "2");
            }
        }
        int size = qAdSystemDeployLog.findRowCount();
        hmap.put("logList", logList);
        hmap.put("total", "" + size);
        return hmap;
    }


    public List<Map<String, String>> qryEnvNameById(String envIds, List<Map<String, String>> logList, String type) {
        Map<String, String> retMap = new HashMap<String, String>();
        Map<String, String> branchTypeMap = new HashMap<String, String>();
        String sql = "";
        String[] splitIds = StringUtils.split(envIds, ",");
        List<Integer> envId = new ArrayList<>();
        for (String id : splitIds) {
            envId.add(Integer.parseInt(id));
        }
        if ("2".equals(type)) {
            sql = "select c.branch_id,c.branch_desc,c.branch_type,d.deploy_info_id env_id from ad_branch c,ad_dcos_deploy_info d where c.branch_id=d.branch_id and d.deploy_info_id in(:envIds)";
        } else {
            sql = "select c.branch_id,c.branch_desc,c.branch_type,d.virtual_id env_id from ad_branch c,ad_virtual_environment d where c.branch_id=d.branch_id and d.virtual_id in(:envIds)";
        }
        SqlQuery sqlQuery = createSqlQuery(sql).setParameter("envIds", envId);
        List<SqlRow> list = sqlQuery.findList();
        for (SqlRow sqlRow : list) {
            retMap.put("" + sqlRow.get("env_id"), "" + sqlRow.get("branch_desc"));
            branchTypeMap.put("" + sqlRow.get("env_id"), "" + sqlRow.get("branch_type"));
        }
        if (logList != null) {
            for (Map<String, String> hmap : logList) {
                if (retMap.get(hmap.get("envId")) != null) {
                    if (type.equals("" + hmap.get("hostType"))) {
                        hmap.put("branchName", retMap.get(hmap.get("envId")));
                        hmap.put("branchType", branchTypeMap.get(hmap.get("envId")));
                    }
                }
            }
        }
        return logList;
    }


    public AdSystemDeployLog qryById(Long logId) {
        return new QAdSystemDeployLog().logId.eq(logId).findUnique();
    }

    public List<AdSystemDeployLog> qryInvalidPlan() {
        Date date = new Date(new Date().getTime() - 60000 * 5);
        return new QAdSystemDeployLog().runTime.before(date).deployResult.eq(3).findList();
    }

    public List<AdSystemDeployLog> noDealPlan() {
        return new QAdSystemDeployLog().deployResult.eq(3).or().planState.eq(1).planState.eq(3).endOr().findList();
    }

    public boolean changeState(AdSystemDeployLog log, int state, String jobToken) {
        String sql = "UPDATE AD_SYSTEMDEPLOY_LOG SET PLAN_STATE =:state,JOB_TOKEN =:jobToken WHERE LOG_ID =:logId AND PLAN_STATE =:planState";
        return Ebean.createSqlUpdate(sql).setParameter("state", state).setParameter("jobToken", jobToken)
            .setParameter("logId", log.getLogId())
            .setParameter("planState", log.getPlanState()).execute() > 0;
    }
}
