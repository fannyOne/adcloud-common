package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.GitUserPojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.UsersPojo;
import com.asiainfo.comm.module.build.dao.impl.AdBranchDAO;
import com.asiainfo.comm.module.build.dao.impl.AdStageDAO;
import com.asiainfo.comm.module.build.dao.impl.AdStageLogDtlDAO;
import com.asiainfo.comm.module.common.AdStaticDataDAO;
import com.asiainfo.comm.module.models.AdBranch;
import com.asiainfo.comm.module.models.AdStage;
import com.asiainfo.comm.module.models.AdStageLogDtl;
import com.asiainfo.comm.module.models.AdStaticData;
import com.asiainfo.comm.module.models.query.QAdStageLogDtl;
import com.avaje.ebean.SqlRow;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by weif on 2016/6/21.
 */
@Component
public class AdStageLogDtlImpl {
    @Autowired
    AdStageLogDtlDAO adStageLogDtlDAO;
    @Autowired
    AdStageDAO adStageDAO;
    @Autowired
    AdStaticDataDAO bsStaticDataDAO;
    @Autowired
    AdBranchDAO adBranchDAO;

    public Map<String, Object> qryStageLogDtl(long env_id, long id) {
        List<Map> retEnvBuidLogList = new ArrayList<Map>();
        Map envBuildLogMap = null;
        Map<String, String> operationMap = new HashMap<String, String>();
        Map<String, String> jobnameMap = new HashMap<String, String>();
        Map<String, String> staticdataMap = new HashMap<String, String>();
        Map<String, Object> retMap = new HashMap<String, Object>();
        if (env_id != 0) {
            List<AdStage> adStageList = adStageDAO.QryAdOperationByEnvId("" + env_id);
            if (adStageList != null) {
                for (AdStage adStage : adStageList) {
                    operationMap.put(adStage.getStageId() + "", adStage.getStageCode() + "");
                    jobnameMap.put(adStage.getStageId() + "", adStage.getJenkinsJobName());
                }
            }
            List<AdStaticData> adStaticDatas = bsStaticDataDAO.qryByCodeType("BUILDER_TYPE");
            if (adStaticDatas != null && adStageList != null) {
                for (AdStaticData adStaticData : adStaticDatas) {
                    staticdataMap.put(adStaticData.getCodeValue(), adStaticData.getCodeName());
                }
            }
        }
        AdBranch adBranch = adBranchDAO.getEnvById(env_id);
        List<AdStageLogDtl> adStageLogDtlList = adStageLogDtlDAO.qryStageLogBySeqId(id);
        String buildType;
        String codeName = "";
        String state;
        if (adStageLogDtlList != null) {
            for (AdStageLogDtl adStageLogDtl : adStageLogDtlList) {
                envBuildLogMap = new HashMap<>();
                if (operationMap.size() > 0 && staticdataMap.size() > 0) {
                    buildType = operationMap.get("" + adStageLogDtl.getAdStage().getStageId());
                    codeName = staticdataMap.get(buildType);
                }
                if (("2").equals(adStageLogDtl.getStageResult())) {
                    state = "success";
                } else {
                    state = "fail";
                }
                envBuildLogMap.put("name", "" + codeName);
                envBuildLogMap.put("state", "" + state);
                envBuildLogMap.put("log", "" + adStageLogDtl.getFailLog().replaceFirst("Started", "<p>Started").replace("\n", "</p><p>").replaceAll("<p>\\+", "<p class=\"command-color\">").replaceAll("<p>errorJenkins", "<p class=\"error-color\">") + "</p>");
                retEnvBuidLogList.add(envBuildLogMap);
            }
        }
        if (adBranch != null) {
            retMap.put("envname", adBranch.getBranchName());
        }
        retMap.put("job", retEnvBuidLogList);
        return retMap;
    }


    public AdStageLogDtl qryBySeqStage(long seq, long stageId) {
        List<AdStageLogDtl> logs = new QAdStageLogDtl().state.eq(1).adStage.stageId
            .eq(stageId).totalStep.eq(seq).orderBy(" FINISH_DATE DESC").findList();
        if (logs != null && logs.size() > 0) {
            return logs.get(0);
        } else {
            return null;
        }
    }

    public void update(AdStageLogDtl log) {
        adStageLogDtlDAO.update(log);
    }

    public UsersPojo qryOpUserInLogDtl(AdBranch adBranch, UsersPojo poj) {
        List<SqlRow> sqlRows = adStageLogDtlDAO
            .qryOpUserInLogDtl(adBranch.getBranchId());
        List<GitUserPojoExt> users = new ArrayList<>();
        for (SqlRow sqlRow : sqlRows) {
            GitUserPojoExt user = new GitUserPojoExt();
            user.setUserId(sqlRow.getLong("OP_ID"));
            user.setDisplayName(sqlRow.getString("DISPLAY_NAME"));
            users.add(user);
        }
        poj.setUsers(users);
        return poj;
    }

    public long qryAvgTime(long branchId, long stage) {
        List<SqlRow> sqlrows = adStageLogDtlDAO.qryAvgTime(branchId, stage);
        if (CollectionUtils.isEmpty(sqlrows)) {
            return 0;
        }
        return sqlrows.get(0).getLong("avgtime");
    }

    public AdStageLogDtl qryLastAdstage(Long total_step, long stage_id) {
        if (null == total_step) {
            total_step = 0l;
        }
        List<AdStageLogDtl> adStageLogDtls = adStageLogDtlDAO.qryStageLogByStageId(total_step, stage_id);
        if (CollectionUtils.isNotEmpty(adStageLogDtls)) {
            return adStageLogDtls.get(0);
        }
        return null;
    }
}
