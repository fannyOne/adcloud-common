package com.asiainfo.comm.module.build.dao.impl;

import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.models.query.*;
import com.asiainfo.comm.module.common.AdParaDetailDAO;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by weif on 2016/6/15.
 */
@Component
public class AdStageDAO {

    @Autowired
    AdParaDetailDAO paraDetailDao;
    @Autowired
    AdBranchDAO adBranchDAO;
    @Autowired
    AdBuildReturnValueDAO adBuildReturnValueDAO;
    @Autowired
    AdFastenSignDAO adFastenSignDAO;


    public List<AdStage> QryAdOperationByName(String jobname) {
        return new QAdStage().state.eq(1).jenkinsJobName.eq(jobname).fetch("adBranch").fetch("adPipeLineState").findList();
    }

    public AdStage QryAdOperationByNameUnique(String jobName) {
        return new QAdStage().state.eq(1).jenkinsJobName.eq(jobName).findUnique();
    }

    public List<AdStage> QryAdOperationByEnvId(String env_id) {
        List<AdStage> operationList = new QAdStage().adBranch.branchId.eq(Long.valueOf(env_id)).findList();
        return operationList;
    }

    public List<AdStage> QryValidAdOperationByEnvId(long branchId) {
        List<AdStage> operationList = new QAdStage().state.eq(1).adBranch.branchId.eq(branchId).orderBy(" STEP ASC ").findList();
        return operationList;
    }


    public int QryValidAdStageCount(long branchId) {
        return new QAdStage().state.eq(1).adBranch.branchId.eq(branchId).findRowCount();
    }

    public String UpdateAdOperationByEnvId(String env_id, String state) {      //1、成功 2、失败 3、强制中止
        String sql = "select AD_PROJECT$SEQ.nextval opseq from dual";
        String serialnumber = "";
        SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
        List<SqlRow> lists = sqlQuery.findList();
        for (SqlRow sqlRow : lists) {
            serialnumber = sqlRow.getString("opseq");
        }
        String s = "UPDATE AD_STAGE set deal_result = :state,build_seq=:serialnumber where STATE=1 AND BRANCH_ID = :env_id";
        SqlUpdate update = Ebean.createSqlUpdate(s);
        update.setParameter("env_id", env_id);
        update.setParameter("state", state);
        update.setParameter("serialnumber", serialnumber);
        Ebean.execute(update);
        return serialnumber;
    }

    public void UpdateAdOperationByAdstageId(long branch_id, long step, long serialnumber) {      //1、成功 2、失败 3、强制中止
        String s = "UPDATE AD_STAGE set build_seq=:serialnumber where state=1 and branch_id=:branch_id and step = :step";
        SqlUpdate update = Ebean.createSqlUpdate(s);
        update.setParameter("serialnumber", serialnumber);
        update.setParameter("branch_id", branch_id);
        update.setParameter("step", step);
        Ebean.execute(update);
    }

    public void UpdateAdOperationByEnvId(String env_id, Long serialnumber, long stage_id, String state) {      //1、成功 2、失败 3、强制中止
        Ebean.execute(() -> {
            String s = "UPDATE AD_STAGE set build_seq=:serialnumber where stage_id=:stage_id";
            SqlUpdate update = Ebean.createSqlUpdate(s);
            update.setParameter("stage_id", stage_id);
            update.setParameter("serialnumber", serialnumber);
            Ebean.execute(update);
            s = "UPDATE AD_STAGE set deal_result = :state where STATE=1 AND BRANCH_ID = :branchId";
            update = Ebean.createSqlUpdate(s);
            update.setParameter("branchId", env_id);
            update.setParameter("state", state);
            Ebean.execute(update);
        });
    }

    public Long getSeqId() {
        String sql = "select AD_PROJECT$SEQ.nextval opseq from dual";
        Long serialnumber = 0L;
        SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
        List<SqlRow> lists = sqlQuery.findList();
        for (SqlRow sqlRow : lists) {
            serialnumber = sqlRow.getLong("opseq");
        }
        return serialnumber;
    }

    public Long createSeqInfo(Long pipelineId) {
        String sql = "select AD_PROJECT$SEQ.nextval opseq from dual";
        Long serialnumber = 0L;
        SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
        List<SqlRow> lists = sqlQuery.findList();
        for (SqlRow sqlRow : lists) {
            serialnumber = sqlRow.getLong("opseq");
        }
        AdBuildReturnValue adBuildReturnValue = adBuildReturnValueDAO.qryBuildReturnValue(pipelineId, 1);
        if (adBuildReturnValue != null) {
            adBuildReturnValue.setBuildSeq(serialnumber);
            adBuildReturnValueDAO.updateBuildReturnSeq(adBuildReturnValue);
        } else {
            adBuildReturnValue = new AdBuildReturnValue();
            adBuildReturnValue.setPipelineId(pipelineId);
            adBuildReturnValue.setBuildSeq(serialnumber);
            adBuildReturnValue.setStep(1L);
            adBuildReturnValue.setNext_step(2L);
            adBuildReturnValueDAO.insertBuildReturnSeq(adBuildReturnValue);
        }
        return serialnumber;
    }

    public Long createSeqInfo(Long pipelineId, long step) {
        String sql = "select AD_PROJECT$SEQ.nextval opseq from dual";
        Long serialnumber = 0L;
        SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
        List<SqlRow> lists = sqlQuery.findList();
        for (SqlRow sqlRow : lists) {
            serialnumber = sqlRow.getLong("opseq");
        }
        AdBuildReturnValue adBuildReturnValue = adBuildReturnValueDAO.qryBuildReturnValue(pipelineId, step);
        if (adBuildReturnValue != null) {
            adBuildReturnValue.setBuildSeq(serialnumber);
            adBuildReturnValueDAO.updateBuildReturnSeq(adBuildReturnValue);
        } else {
            adBuildReturnValue = new AdBuildReturnValue();
            adBuildReturnValue.setPipelineId(pipelineId);
            adBuildReturnValue.setBuildSeq(serialnumber);
            adBuildReturnValue.setStep(step);
            adBuildReturnValue.setNext_step(step + 1);
            adBuildReturnValueDAO.insertBuildReturnSeq(adBuildReturnValue);
        }
        return serialnumber;
    }

    public void UpdateAdOperationByName(String env_id, String state, String jobname) {
        String s = "UPDATE AD_STAGE set deal_result = :state where  STATE=1 AND BRANCH_ID = :env_id and jenkins_job_name=:jobname";
        SqlUpdate update = Ebean.createSqlUpdate(s);
        update.setParameter("env_id", env_id);
        update.setParameter("jobname", jobname);
        update.setParameter("state", state);
        Ebean.execute(update);

    }

    public void UpdateAdOperationByName(long seqId, long env_id, long state, String jobname, String opId) {
        String s = "UPDATE AD_STAGE set deal_result = :state,build_seq =:seqId,pipeline_operator =:opId where  STATE=1 AND BRANCH_ID = :env_id and jenkins_job_name=:jobname";
        SqlUpdate update = Ebean.createSqlUpdate(s);
        update.setParameter("env_id", env_id);
        update.setParameter("jobname", jobname);
        update.setParameter("opId", opId);
        update.setParameter("state", state);
        update.setParameter("seqId", seqId);
        Ebean.execute(update);
    }

    public void UpdateStageOperatorById(long env_id, long state, String opId) {
        String s = "UPDATE AD_STAGE set pipeline_operator =:opId where  STATE=1 AND BRANCH_ID = :env_id";
        SqlUpdate update = Ebean.createSqlUpdate(s);
        update.setParameter("env_id", env_id);
        update.setParameter("opId", opId);
        update.setParameter("state", state);
        Ebean.execute(update);
    }

    public void UpdateAdStageByBranchId(long branchId, int state, String opId) {
        String s = "UPDATE AD_STAGE set deal_result = :state,pipeline_operator=:opId where  STATE=1 AND BRANCH_ID = :env_id";
        SqlUpdate update = Ebean.createSqlUpdate(s);
        update.setParameter("env_id", branchId);
        update.setParameter("opId", opId);
        update.setParameter("state", state);
        Ebean.execute(update);
    }

    public List<AdStage> QryAdOperationByEnvIdType(Long envId) {
        List<AdStage> operationList = null;
        operationList = new QAdStage().adBranch.branchId.eq(envId).state.eq(1).orderBy(" STEP ASC ").findList();//.stageCode.eq(1)
        return operationList;
    }

    public AdStage QryAdOperationByEnvStep(Long envId, Integer buildType, int step) {
        AdStage adStage = null;
        AdPipeLineState adPipeLineState = new QAdPipeLineState().buildType.eq(buildType).adBranch.branchId.eq(envId).findUnique();
        if (adPipeLineState != null) {
            adStage = new QAdStage().adPipeLineState.pipelineId.eq(adPipeLineState.getPipelineId()).step.eq(step).findUnique();

        }
        return adStage;
    }


    public AdStage qryByBranchAndStageCode(long branchId, int stageCode) {
        List<AdStage> stages = new QAdStage().state.eq(1).adBranch.branchId.eq(branchId).stageCode.eq(stageCode).findList();
        if (stages != null && stages.size() > 0) {
            return stages.get(0);
        } else {
            return null;
        }
    }

    public boolean updateState(AdStage adStage, int state) throws SQLException {
        String sql = "UPDATE AD_STAGE SET DEAL_RESULT = :state WHERE STAGE_ID = :stage_id AND STATE != 0 AND DEAL_RESULT != :dealresult";
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        update.setParameter("state", state);
        update.setParameter("stage_id", adStage.getStageId());
        update.setParameter("dealresult", state);
        int returnNum = Ebean.execute(update);
        return returnNum > 0;
    }

    public Connection getConnection(String databaseName) throws SQLException {
        AdParaDetail paraDetail = paraDetailDao.qryByDetails("X", databaseName.toUpperCase() + "_DB_INFO", databaseName.toUpperCase() + "_DB_INFO").get(0);
        return DriverManager.getConnection(paraDetail.getPara1(), paraDetail.getPara2(), paraDetail.getPara3());
    }

    public AdStage qryById(long stagId) {
        List<AdStage> list = new QAdStage().stageId.eq(stagId).state.eq(1).findList();
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public void updateAdStage(long pipelineId, List<Map<String, String>> adStages, Long triggerBranchId, AdBranch inputAdBranch, String envId) {//long branchId, String branchDesc,String originPath, String buildFileType,
        Ebean.execute(() -> {
            String s = "UPDATE AD_STAGE set STATE=0 where  STATE=1 AND BRANCH_ID = :branchId and PIPELINE_ID=:pipelineId";
            SqlUpdate update = Ebean.createSqlUpdate(s);
            update.setParameter("branchId", inputAdBranch.getBranchId());
            update.setParameter("pipelineId", pipelineId);
            Ebean.execute(update);
            if (StringUtils.isNotEmpty(envId) && envId.indexOf("_") > 0) {
                String[] arrayenvId = envId.split("_");
                String branchDescSQL = "UPDATE AD_BRANCH SET BRANCH_DESC = :branchDesc,ORIGIN_PATH=:originPath,BUILD_FILE_TYPE=:buildFileType , TRIGGER_BRANCH=:triggerBranch,ENV_ID=:envid,ENV_TYPE=:envtype,BUILD_CRON=:buildCron   where BRANCH_ID = :branchId";
                SqlUpdate branchupdate = Ebean.createSqlUpdate(branchDescSQL);
                branchupdate.setParameter("branchDesc", inputAdBranch.getBranchDesc());
                branchupdate.setParameter("branchId", inputAdBranch.getBranchId());
                branchupdate.setParameter("triggerBranch", triggerBranchId);
                branchupdate.setParameter("originPath", inputAdBranch.getOriginPath());
                branchupdate.setParameter("buildFileType", inputAdBranch.getBuildFileType());
                branchupdate.setParameter("envid", arrayenvId[0]);
                branchupdate.setParameter("envtype", arrayenvId[1]);
                branchupdate.setParameter("buildCron", inputAdBranch.getBuildCron());
                Ebean.execute(branchupdate);
            } else {
                String branchDescSQL = "UPDATE AD_BRANCH SET BRANCH_DESC = :branchDesc,ORIGIN_PATH=:originPath,BUILD_FILE_TYPE=:buildFileType , TRIGGER_BRANCH=:triggerBranch,ENV_ID=:envid,ENV_TYPE=:envtype,BUILD_CRON=:buildCron   where BRANCH_ID = :branchId";
                SqlUpdate branchupdate = Ebean.createSqlUpdate(branchDescSQL);
                branchupdate.setParameter("branchDesc", inputAdBranch.getBranchDesc());
                branchupdate.setParameter("branchId", inputAdBranch.getBranchId());
                branchupdate.setParameter("triggerBranch", triggerBranchId);
                branchupdate.setParameter("originPath", inputAdBranch.getOriginPath());
                branchupdate.setParameter("buildFileType", inputAdBranch.getBuildFileType());
                branchupdate.setParameter("envid", 0);
                branchupdate.setParameter("envtype", "none");
                branchupdate.setParameter("buildCron", inputAdBranch.getBuildCron());
                Ebean.execute(branchupdate);
            }
            List<Map<String, String>> adstagelist = new ArrayList<Map<String, String>>();
            for (Map<String, String> stagemap : adStages) {
                if (("false").equals(stagemap.get("ischoosestage"))) {
                } else {
                    adstagelist.add(stagemap);
                }
            }
            int i = 0;
            if (adStages != null && pipelineId != 0 && inputAdBranch.getBranchId() != 0) {
                AdPipeLineState adPipeLineState = new QAdPipeLineState().pipelineId.eq(pipelineId).findUnique();
                AdBranch adBranch = new QAdBranch().branchId.eq(inputAdBranch.getBranchId()).findUnique();

                AdStage adStage;
                for (Map<String, String> stagemap : adstagelist) {
                    adStage = new AdStage();
                    i = i + 1;
                    adStage.setJenkinsJobName(stagemap.get("name"));
                    adStage.setStageCode(Integer.valueOf((String) stagemap.get("stagecode")));
                    adStage.setState(1l);
                    adStage.setStep(i);
                    adStage.setDealResult(0);
                    adStage.setStageConfig(stagemap.get("stageconfig"));
                    if (i == 1) {
                        adStage.setIsSpec(1);
                    } else if (i == adstagelist.size()) {
                        adStage.setIsSpec(2);
                    }
                    if (("6").equals(adStage.getStageCode())) {
                        adStage.setIsSpec(3);
                    }
                    if (stagemap.get("spec") != null) {
                        adStage.setJobSchedule(stagemap.get("spec"));
                    }
                    adStage.setAdPipeLineState(adPipeLineState);
                    adStage.setAdBranch(adBranch);
                    adStage.setCreateDate(new java.util.Date());
                    adStage.save();

                    if (("1").equals(stagemap.get("stagecode"))) {//表示第一个环节
                        if (adBranch != null) {
                            adBranch.setBranchPath(stagemap.get("branch"));
                            adBranch.save();
                        }
                    }
                }
            }
        });
    }


    public void saveAdStages(List<AdStage> adStages) {
        if (adStages != null && adStages.size() > 0)
            Ebean.saveAll(adStages);

    }

    public Map<String, List<AdStage>> QryAdOperationByProjectId(long projectId) {
        List<AdStage> operationList = new QAdStage().adBranch.adProject.projectId.eq(projectId).state.eq(1).findList();
        Map<String, List<AdStage>> hmap = new HashMap<String, List<AdStage>>();
        List<AdStage> stageList;
        if (operationList != null) {
            for (AdStage adStage : operationList) {
                if (hmap.containsKey("" + adStage.getAdBranch().getBranchId())) {
                    stageList = hmap.get("" + adStage.getAdBranch().getBranchId());
                } else {
                    stageList = new ArrayList<>();
                    hmap.put("" + adStage.getAdBranch().getBranchId(), stageList);
                }
                stageList.add(adStage);
            }
        }
        return hmap;
    }

    public void deleteStageByProjectId(long projectId) throws Exception {
        Ebean.execute(() -> {
            String s = "UPDATE AD_STAGE set STATE=0 WHERE  BRANCH_ID IN (SELECT  BRANCH_ID FROM AD_BRANCH WHERE PROJECT_ID=:project_id)";
            SqlUpdate update = Ebean.createSqlUpdate(s);
            update.setParameter("project_id", projectId);
            Ebean.execute(update);

            s = "UPDATE ad_pipeline_state set STATE=0 WHERE  PROJECT_ID=:project_id";
            update = Ebean.createSqlUpdate(s);
            update.setParameter("project_id", projectId);
            Ebean.execute(update);

            s = "UPDATE AD_BRANCH set STATE=0 WHERE  PROJECT_ID=:project_id";
            update = Ebean.createSqlUpdate(s);
            update.setParameter("project_id", projectId);
            Ebean.execute(update);

            List<AdProject> adProjectList = new QAdProject().projectId.eq(projectId).findList();
            if (adProjectList != null) {
                for (AdProject adProject : adProjectList) {
                    adProject.setState(0l);
                    // 删除相应 应用的订阅信息
                    adFastenSignDAO.deleteByProjectId(adProject.getProjectId());
                }
                Ebean.updateAll(adProjectList);
            }
            List<AdAuthor> adAuthorList = new QAdAuthor().adProject.projectId.eq(projectId).findList();
            if (adAuthorList != null) {
                Ebean.deleteAll(adAuthorList);
            }
        });
    }

    public void deleteStageByBranchId(long branchId) throws Exception {
        Ebean.execute(() -> {
            String s = "UPDATE AD_STAGE set STATE=0 WHERE  BRANCH_ID =:branchId";
            SqlUpdate update = Ebean.createSqlUpdate(s);
            update.setParameter("branchId", branchId);
            Ebean.execute(update);

            s = "UPDATE ad_pipeline_state set STATE=0 WHERE  BRANCH_ID=:branchId";
            update = Ebean.createSqlUpdate(s);
            update.setParameter("branchId", branchId);
            Ebean.execute(update);

            s = "UPDATE AD_BRANCH set STATE=0 WHERE  BRANCH_ID=:branchId";
            update = Ebean.createSqlUpdate(s);
            update.setParameter("branchId", branchId);
            Ebean.execute(update);

        });
    }


    public void updateStageConfig(int stageId, String stageConfig) throws Exception {
        AdStage adStage = new QAdStage().stageId.eq(stageId).findUnique();
        if (adStage != null) {
            adStage.setStageConfig(stageConfig);
            adStage.update();
        }
    }

    public void updateStageCommitId(String preCommitId, String commitId, String commitOperator, long stageId) {
        AdStage adStage = new QAdStage().stageId.eq(stageId).findUnique();
        if (adStage != null) {
            if (StringUtils.isNotEmpty(preCommitId))
                adStage.setPreCommitId(preCommitId);
            if (StringUtils.isNotEmpty(commitId))
                adStage.setCommitId(commitId);
            if (StringUtils.isNotEmpty(commitOperator))
                adStage.setCommitOperator(commitOperator);
            adStage.save();
        }
    }

    public void updateStageIsupdate(long stageId, int is_update) {
        AdStage adStage = new QAdStage().stageId.eq(stageId).findUnique();
        if (adStage != null) {
            adStage.setIsUpdate(is_update);
            adStage.save();
        }
    }

    public AdStage qryStageByStep(long branchId, int stagecode) {
        List<AdStage> adStageList = new QAdStage().adBranch.branchId.eq(branchId).state.eq(1).stageCode.eq(stagecode).findList();
        if (adStageList != null && adStageList.size() > 0) {
            return adStageList.get(0);
        }
        return null;
    }

    public void save(AdStage downloadStage) {
        downloadStage.save();
    }

    public List<AdStage> QryBuildingStage(long branchId, int[] dealResult) {
        QAdStage qAdStage = new QAdStage().state.eq(1).adBranch.branchId.eq(branchId);
        if (dealResult != null && dealResult.length > 0) {
            qAdStage = qAdStage.and();
            for (int i = 0; i < dealResult.length; i++) {
                qAdStage = qAdStage.or().dealResult.eq(dealResult[i]);
            }
            qAdStage = qAdStage.endAnd();
        }
        List<AdStage> operationList = qAdStage.orderBy(" STEP ASC ").findList();
        return operationList;
    }

    public AdStage qryRunStage(Long branchId) {
        List<AdStage> stageList = new QAdStage().state.eq(1).adBranch.branchId.eq(branchId).dealResult.eq(1).findList();
        if (stageList != null && stageList.size() > 0) {
            return stageList.get(0);
        } else {
            return null;
        }
    }

    public List<AdStage> qryAfterStep(Long branchId, int step) {
        List<AdStage> stageList = new QAdStage().state.eq(1).adBranch.branchId.eq(branchId).step.greaterOrEqualTo(step).findList();
        return stageList;
    }

    public AdStage qryByStep(Long branchId, int step) {
        AdStage stage = new QAdStage().state.eq(1).adBranch.branchId.eq(branchId).step.eq(step).findUnique();
        return stage;
    }

    public void updateDealResult(long branchId) {
        String sql = "update ad_stage t set t.deal_result=3 where t.branch_id= :branchId and t.state=1 and t.deal_result=1";
        SqlUpdate update = Ebean.createSqlUpdate(sql);
        update.setParameter("branchId", branchId);
        Ebean.execute(update);
    }

    public List<AdStage> qryBranchIdByCode(List<Long> branchIds) {
        QAdStage qAdStage = new QAdStage().state.eq(1).stageCode.eq(6);
        if (branchIds != null && branchIds.size() > 0) {
            for (Long id : branchIds) {
                qAdStage = qAdStage.or().adBranch.branchId.eq(id);
            }
            List<AdStage> adStageList = qAdStage.findList();
            return adStageList;
        } else {
            return null;
        }
    }

    public List<AdStage> qryAdStage(Long branchId) {
        return new QAdStage().adBranch.branchId.eq(branchId).state.eq(1).orderBy(" STEP ASC ").findList();

    }

    public List<AdStage> qryAutoTestStages(int stageCode) {
        List<AdStage> adStageList = new QAdStage().state.eq(1).jobSchedule.isNotNull().stageCode.eq(stageCode).findList();
        return adStageList;
    }

    public List<AdStage> qryDownAdStage(Long projectId) {
        List<AdStage> adStageList = new QAdStage().state.eq(1).stageCode.eq(CommConstants.STAGE_CODE.downLoad).adBranch.adProject.projectId.eq(projectId).findList();
        return adStageList;
    }


    public List<AdStage> qryStageByEnvIdType(long envId, String envType) {
        List<AdStage> adStageList1 = new QAdStage().adBranch.envId.eq(envId).adBranch.envType.eq(envType).stageCode.eq(CommConstants.STAGE_CODE.deploy).state.eq(1).findList();
        List<AdStage> adStageList2 = new QAdStage().adBranch.envId.eq(envId).adBranch.envType.eq(envType).stageCode.eq(CommConstants.STAGE_CODE.restart).state.eq(1).findList();
        adStageList1.addAll(adStageList2);
        return adStageList1;
    }

    public AdProject qryAdProjectByStageId(Long StageId) {
        AdStage adStage = qryById(StageId);
        if (adStage != null) {
            return adStage.getAdBranch().getAdProject();
        }
        return null;
    }
}
