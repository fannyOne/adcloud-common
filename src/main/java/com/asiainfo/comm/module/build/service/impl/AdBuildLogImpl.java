package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.AdBuildCurrentPojoExt;
import com.asiainfo.util.DateConvertUtils;
import com.asiainfo.comm.module.build.dao.impl.AdBuildLogDAO;
import com.asiainfo.comm.module.common.AdStaticDataDAO;
import com.asiainfo.comm.module.models.AdBuildLog;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import com.avaje.ebean.SqlRow;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by YangRY
 * 2016/7/6 0006.
 */
@Component
public class AdBuildLogImpl {
    @Autowired
    AdBuildLogDAO buildLogDAO;
    @Autowired
    AdStaticDataDAO bsStaticDataDAO;
    @Autowired
    private AdUserRoleRelImpl userRoleRelImpl;

    public List<AdBuildLog> qryFailedBuilds() {
        return buildLogDAO.qryFailedBuilds();
    }

    public List<SqlRow> qryPersonalFailedBuildsSqlRow(Long userId) {
        return buildLogDAO.qryPersonalFailedBuildsSqlRow(userId);
    }

    public List<SqlRow> qryLastFailedBuildsSqlRow(String loginName) {
        return buildLogDAO.qryLastFailedBuildsSqlRow(loginName);
    }

    public Map<String, Object> qryStageLogList(Map inputMap) throws IOException {
        QryBuildModelInputMap qryBuildModelInputMap = new QryBuildModelInputMap(inputMap).invoke();
        long ll_BranchId = qryBuildModelInputMap.getLl_branchId();
        long ll_Opid = qryBuildModelInputMap.getLl_opid();
        int li_DealResult = qryBuildModelInputMap.getLi_dealResult();
        Date ld_beginDate = qryBuildModelInputMap.getLd_beginDate();
        Date ld_endDate = qryBuildModelInputMap.getLd_endDate();
        int li_PageNum = qryBuildModelInputMap.getLi_pageNum();
        int li_pageSize = qryBuildModelInputMap.getLi_pageSize();
        int li_buildType = qryBuildModelInputMap.getLi_buildType();
        /* 资源隔离，权限验证 */
        if (!userRoleRelImpl.verifyPurview("branchId", ll_BranchId)) {
            return null;
        }
        /* 资源隔离，权限验证 Over */

        Map<String, Object> buildLogMap = buildLogDAO.qryBuildLogList(ll_Opid, li_DealResult, ld_beginDate, ld_endDate, li_PageNum, li_pageSize, ll_BranchId, li_buildType);
        return buildLogMap;

    }

    public Map<String, Object> qryStageReport(Map inputMap) throws IOException {
        QryBuildModelInputMap qryBuildModelInputMap = new QryBuildModelInputMap(inputMap).invoke2();
        long ll_BranchId = qryBuildModelInputMap.getLl_branchId();
        long ll_Opid = qryBuildModelInputMap.getLl_opid();
        int li_DealResult = qryBuildModelInputMap.getLi_dealResult();
        Date ld_beginDate = qryBuildModelInputMap.getLd_beginDate();
        Date ld_endDate = qryBuildModelInputMap.getLd_endDate();
        /* 资源隔离，权限验证 */
        if (!userRoleRelImpl.verifyPurview("branchId", ll_BranchId)) {
            return null;
        }
        /* 资源隔离，权限验证 Over */

        Map<String, Object> buildLogMap = buildLogDAO.qryBulidReport(ll_Opid, li_DealResult, ld_beginDate, ld_endDate, ll_BranchId);
        return buildLogMap;
    }

    public Map<String, Object> qryStageReportdtl(Map inputMap) throws IOException {
        String build_id = (String) inputMap.get("buildId");
        int li_build_id = Integer.valueOf(build_id);
        String branch_id = (String) inputMap.get("branchId");
        int branchId = Integer.valueOf(branch_id);
        Map<String, Object> buildLogMap = buildLogDAO.qryBulidReportdtl(li_build_id, branchId);
        return buildLogMap;

    }

    public void insertBuildLog(AdBuildLog adBuildLog) {
        buildLogDAO.insertBuildLog(adBuildLog);
    }

    public AdBuildLog qryLast(Long branchId) {
        List<AdBuildLog> adBuildLogs = buildLogDAO.qryBuildLog(branchId);
        if (CollectionUtils.isEmpty(adBuildLogs)) {
            return null;
        }
        return adBuildLogs.get(0);
    }

    public long qryAvgTimeByBranchId(Long branchId) {
        List<SqlRow> sqlrows = buildLogDAO.qryAvgTimeByBranchId(branchId);
        if (CollectionUtils.isEmpty(sqlrows)) {
            return 0;
        }
        return sqlrows.get(0).getLong("avgtime");
    }

    public static class QryBuildModelInputMap {
        private Map inputMap;
        private Date ld_beginDate;
        private Date ld_endDate;
        private long ll_opid;
        private int li_dealResult;
        private int li_pageNum;
        private long ll_branchId;
        private int li_pageSize;
        private int li_buildType;

        QryBuildModelInputMap(Map inputMap) {
            this.inputMap = inputMap;
        }

        Date getLd_beginDate() {
            return ld_beginDate;
        }

        Date getLd_endDate() {
            return ld_endDate;
        }

        long getLl_opid() {
            return ll_opid;
        }

        int getLi_dealResult() {
            return li_dealResult;
        }

        int getLi_pageNum() {
            return li_pageNum;
        }

        long getLl_branchId() {
            return ll_branchId;
        }

        int getLi_pageSize() {
            return li_pageSize;
        }

        int getLi_buildType() {
            return li_buildType;
        }

        public QryBuildModelInputMap invoke() {
            String op_id = null;
            String dealResult = null;
            String pageNum = null;
            String branchId = null;
            String beginDate = null;
            String endDate = null;
            String buildType = null;
            if (inputMap != null) {
                op_id = (String) inputMap.get("user");
                dealResult = (String) inputMap.get("state");
                beginDate = (String) inputMap.get("startDate");
                endDate = (String) inputMap.get("endDate");
                branchId = (String) inputMap.get("pipelineId");
                pageNum = (String) inputMap.get("page");
                buildType = (String) inputMap.get("buildType");
            }
            ld_beginDate = null;
            ld_endDate = null;
            ll_opid = 0;
            li_dealResult = 0;
            li_pageNum = 0;
            ll_branchId = 0;
            li_pageSize = 10;
            li_buildType = 0;
            if (beginDate != null && beginDate.indexOf("-") > 0) {
                ld_beginDate = DateConvertUtils.StringToDate(beginDate.trim() + " 00:00:00");
            }
            if (endDate != null && endDate.indexOf("-") > 0) {
                ld_endDate = DateConvertUtils.StringToDate(endDate.trim() + " 24:00:00");
            }
            if (StringUtils.isNotEmpty(op_id)) {
                ll_opid = Long.valueOf(op_id);
            }
            if (StringUtils.isNotEmpty(dealResult)) {
                li_dealResult = Integer.valueOf(dealResult);
            }
            if (StringUtils.isNotEmpty(pageNum)) {
                li_pageNum = Integer.valueOf(pageNum);
                if (li_pageNum > 0) {
                    li_pageNum = li_pageNum - 1;
                }
            }
            if (StringUtils.isNotEmpty(branchId)) {
                ll_branchId = Long.valueOf(branchId);
            }
            if (StringUtils.isNotEmpty(buildType)) {
                li_buildType = Integer.valueOf(buildType);
            }
            return this;
        }

        public QryBuildModelInputMap invoke2() {
            String operateId = null;
            String branchId = null;
            String resultType = null;
            String startDate = null;
            String endDate = null;
            if (inputMap != null) {
                operateId = (String) inputMap.get("operateId");
                branchId = (String) inputMap.get("branchId");
                resultType = (String) inputMap.get("resultType");
                startDate = (String) inputMap.get("startDate");
                endDate = (String) inputMap.get("endDate");
            }
            ld_beginDate = null;
            ld_endDate = null;
            ll_opid = 0;
            li_dealResult = 0;
            li_pageNum = 0;
            ll_branchId = 0;
            li_pageSize = 10;
            li_buildType = 0;
            if (startDate != null && startDate.indexOf("-") > 0) {
                ld_beginDate = DateConvertUtils.StringToDate(startDate.trim() + " 00:00:00");
            }
            if (endDate != null && endDate.indexOf("-") > 0) {
                ld_endDate = DateConvertUtils.StringToDate(endDate.trim() + " 24:00:00");
            }
            if (StringUtils.isNotEmpty(operateId)) {
                ll_opid = Long.valueOf(operateId);
            }
            if (StringUtils.isNotEmpty(resultType)) {
                li_dealResult = Integer.valueOf(resultType);
            }
            if (StringUtils.isNotEmpty(branchId)) {
                ll_branchId = Long.valueOf(branchId);
            }
            return this;
        }

    }
}
