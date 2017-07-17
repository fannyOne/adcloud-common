package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.dataModel.JobNotifyModel;
import com.asiainfo.comm.common.pojo.dataModel.JobNotifyStageModel;
import com.asiainfo.util.CommConstants;
import com.asiainfo.util.JsonUtil;
import com.asiainfo.util.MailUtil;
import com.asiainfo.comm.module.build.dao.impl.*;
import com.asiainfo.comm.module.external.service.impl.RmpSynImpl;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.schedule.helper.DealWebScan;
import com.asiainfo.schedule.helper.UploadArtifactoryRunnable;
import com.asiainfo.comm.module.common.AdParaDetailDAO;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by weif on 2016/6/15.
 * Jenkins回调相关
 */
@Component
public class JenkinsStatusImpl {

    private static final String STRING_OLD_NOTIFY = "/jobnotification'";
    private static final String STRING_NEW_NOTIFY = "/jobNotify'";
    protected static Logger logger = LoggerFactory.getLogger(JenkinsStatusImpl.class);
    @Autowired
    AdStageLogDtlDAO adStageLogDtlDAO;
    @Autowired
    AdBuildLogDAO buildInfoSinkDAO;
    @Autowired
    AdPipeLineStateDAO adPipeLineStateDAO;
    @Autowired
    AdOperationImpl operationImpl;
    @Autowired
    AdBranchRelatDAO adBranchRelatDAO;
    @Autowired
    AdUserDAO adUserDAO;
    @Autowired
    JenkinsImpl jenkinsImpl;
    @Autowired
    AdStaticDataImpl bsStaticImpl;
    @Autowired
    JenkinsImpl jenkinsService;
    @Autowired
    AdBranchDAO adBranchDAO;
    @Autowired
    RmpSynImpl rmpSynImpl;
    @Autowired
    private
    AdStageDAO adStageDAO;
    @Autowired
    private AdBuildReturnValueDAO adBuildReturnValueDAO;
    @Autowired
    private AdDockImagesDAO adDockImagesDAO;
    @Autowired
    private AdBranchDAO branchDAO;
    @Autowired
    private
    AdJenkinsInfoDAO jenkinsInfoDAO;
    @Autowired
    private
    AdProjectDeployPackageDAO packageDAO;
    @Autowired
    private
    AdBuildRmpLogDAO adBuildRmpLogDAO;
    @Autowired
    private
    AdRmpBranchRelateDAO adRmpBranchRelateDAO;
    @Autowired
    private
    AdBuildRmpLogDAO buildRmpLogDAO;
    @Autowired
    private
    AdParaDetailDAO bsParaDetailDAO;
    @Autowired
    private AdProjectDAO adProjectDAO;
    @Value("${artifactory.user.url}")
    private String artifactoryUrl;
    @Value("${artifactory.user.name}")
    private String artifactoryName;
    @Value("${artifactory.user.password}")
    private String artifactoryPassword;

    public static boolean notifySend(String notification, String type) {
        boolean send = false;
        if (!org.springframework.util.StringUtils.isEmpty(notification) && notification.length() > 2) {
            String sysNotify = notification.substring(0, 1);
            String emailNotify = notification.substring(1, 2);
            String smsNotify = notification.substring(2, 3);

            if (sysNotify != null && "1".equals(sysNotify) && "sys".equals(type)) {
                send = true;
            } else if (emailNotify != null && "1".equals(emailNotify) && "email".equals(type)) {
                send = true;
            } else if (smsNotify != null && "1".equals(smsNotify) && "sms".equals(type)) {
                send = true;
            }
        }
        return send;
    }

    /**
     * Jenkins Job notification
     *
     * @param retValue 报文
     * @throws Exception 可能出现的异常
     */
    public void dealJobNotification(String retValue) throws Exception {
        Map<String, Object> hmap = JsonUtil.jsonToMap(retValue);
        if (hmap == null) {
            throw new Exception("报文解析异常");
        }
        String jkjobName;
        if (hmap.get("name") != null) {
            jkjobName = (String) hmap.get("name");
        } else {
            throw new Exception("获取job名称异常");
        }
        Integer buildNumber = 0;
        String buildStatus = "";
        String buildLog = "";
        String buildPhase = "";
        if (hmap.get("build") != null) {
            Map<String, Object> buildMap = (Map<String, Object>) hmap.get("build");
            if (buildMap != null) {
                buildNumber = (Integer) buildMap.get("number");
                buildStatus = (String) buildMap.get("status");
                buildPhase = (String) buildMap.get("phase");
                buildLog = (String) buildMap.get("log");
            }
        } else {
            throw new Exception("构建信息获取异常");
        }
        int is_spec = 0;//判断是是否是第一环节和最后一个环节   1表示第一环节 2表示最后一个环节
        long stage_id = 0;
        long step = 0;
        int deal_result;
        long branch_id = 0;
        int busi_type = 0;
        Long serial_number = 0L;
        Integer stage_code = 0;
        AdBranch adBranch = null;
        String preCommitId = "";
        long opId = 0;
        if ("SUCCESS".equals(buildStatus)) {
            deal_result = CommConstants.DEAL_RESULT.SUCCESS;
        } else {
            deal_result = CommConstants.DEAL_RESULT.FAIL;
        }
        AdStage adStage = null;
        AdPipeLineState pipeLineState = null;
        AdUser adUser = adUserDAO.getUserById(1);
        List<AdStage> adStageList = adStageDAO.QryAdOperationByName(jkjobName);
        if (adStageList != null && adStageList.size() > 0) {
            adStage = adStageList.get(0);
            if (null != adStage) {
                is_spec = adStage.getIsSpec() == null ? 0 : adStage.getIsSpec();
                stage_id = adStage.getStageId();
                step = adStage.getStep();
                branch_id = adStage.getAdBranch().getBranchId();
                adBranch = adStage.getAdBranch();
                busi_type = adStage.getStageCode();
                stage_code = adStage.getStageCode();
                serial_number = adStage.getBuildSeq();
                pipeLineState = adStage.getAdPipeLineState();
                preCommitId = adStage.getCommitId();
                opId = adStage.getPipelineOperator() == null ? 1 : adStage.getPipelineOperator();
            }
        }
        //先查找SeqId
        AdBuildReturnValue adBuildReturnValue = null;
        if (stage_id != 0) {
            adBuildReturnValue = adBuildReturnValueDAO.qryBuildReturnValue(pipeLineState.getPipelineId(), step);
        }
        //如果是第一步，并且seqId不存在或者seqId小于当前版本，则生成新的seqId
        if (is_spec == 1 && ("STARTED").equals(buildPhase)) {
            if ((adBuildReturnValue == null || serial_number == null || serial_number > adBuildReturnValue.getBuildSeq()) || opId <= 1) {
                serial_number = adStageDAO.getSeqId();
            } else {
                serial_number = adBuildReturnValue.getBuildSeq();
            }
        }
        //不是最后一步，更新下一步的seqId
        if (stage_id != 0 && is_spec != 2) {
            adBuildReturnValue = adBuildReturnValueDAO.qryBuildReturnValue(pipeLineState.getPipelineId(), step + 1);
            adStageDAO.UpdateAdOperationByAdstageId(branch_id, step + 1, serial_number);
            if (adBuildReturnValue != null) {
                adBuildReturnValue.setBuildSeq(serial_number);
                adBuildReturnValueDAO.updateBuildReturnSeq(adBuildReturnValue);
            } else {
                adBuildReturnValue = new AdBuildReturnValue();
                adBuildReturnValue.setPipelineId(pipeLineState.getPipelineId());
                adBuildReturnValue.setBuildSeq(serial_number);
                adBuildReturnValue.setStep(step + 1);
                adBuildReturnValue.setNext_step(step + 2);
                adBuildReturnValueDAO.insertBuildReturnSeq(adBuildReturnValue);
            }
        }
        if (("COMPLETED").equals(buildPhase)) {       //表示job执行后触发
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("is_spec", "" + is_spec);
            logMap.put("stage_id", "" + stage_id);
            logMap.put("step", "" + step);
            logMap.put("deal_result", "" + deal_result);
            logMap.put("branch_id", "" + branch_id);
            logMap.put("busi_type", "" + busi_type);
            logMap.put("serial_number", "" + serial_number);
            logMap.put("stage_code", "" + stage_code);
            logMap.put("jkjobName", "" + jkjobName);
            logMap.put("buildLog", "" + buildLog);
            logMap.put("buildNumber", "" + buildNumber);
            logMap.put("preCommitId", "" + preCommitId);
            endCallFun(logMap, pipeLineState, adStage);
        } else if (("STARTED").equals(buildPhase)) {
            if (is_spec == 1 && opId <= 1) {
                adStageDAO.UpdateAdOperationByEnvId("" + branch_id, serial_number, stage_id, "0");
                if (pipeLineState != null) {
                    pipeLineState.setAdBranch(adBranch);
                    pipeLineState.setLastBuildResult(0);
                    pipeLineState.setBranchState(2);
                    pipeLineState.setBuildType(busi_type);
                    adPipeLineStateDAO.updatePipeLineState(pipeLineState);
                }
                if (opId != 0) {
                    adUser = adUserDAO.getUserById(opId);
                }
                adStage.setPipelineOperator(0);
                adStageDAO.save(adStage);
                AdBuildLog adBuildLog = new AdBuildLog();
                adBuildLog.setBuildSeq(step);
                adBuildLog.setAdUser(adUser);
                adBuildLog.setBuildType(CommConstants.BuildConstants.BUILDlOG.BUILD_TYPE.BUILD);
                adBuildLog.setCreateDate(new java.util.Date());
                adBuildLog.setAdBranch(adBranch);
                adBuildLog.setBuildResult(0);
                adBuildLog.setState(1);
                adBuildLog.setLastStep((int) step);
                adBuildLog.setTotalStep((int) serial_number.longValue());
                buildInfoSinkDAO.insertBuildLog(adBuildLog);
            }
            if (stage_code == 6) {
                AdStage adStage1 = adStageDAO.qryStageByStep(branch_id, 1);
                if (adStage1 != null) {
                    Thread urunable = new Thread(new UploadArtifactoryRunnable(serial_number, branch_id, adStage1.getCommitId(),
                        branchDAO, jenkinsInfoDAO,
                        packageDAO, adStageDAO, artifactoryUrl, artifactoryName, artifactoryPassword, bsStaticImpl));
                    urunable.start();
                }
            }
            AdStageLogDtl buildLogDtl = new AdStageLogDtl();
            if (is_spec != 1 || opId <= 1) {
                buildLogDtl.setAdBranch(adBranch);
                buildLogDtl.setTotalStep(serial_number);
                buildLogDtl.setStep(step);
                buildLogDtl.setAdStage(adStage);
                buildLogDtl.setAdUser(adUser);
                buildLogDtl.setState(1L);
                buildLogDtl.setBuildSeqId(Long.valueOf(buildNumber));
                buildLogDtl.setBeginDate(new java.util.Date());
                adStageLogDtlDAO.insertStageLogDtl(buildLogDtl);
                adStageDAO.UpdateAdOperationByName("" + branch_id, "1", jkjobName);
            } else {
                buildLogDtl = adStageLogDtlDAO.qryStageLogByTotalstep(serial_number, branch_id, stage_id);
                buildLogDtl.setBuildSeqId(Long.valueOf(buildNumber));
                adStageLogDtlDAO.insertStageLogDtl(buildLogDtl);
            }
        }
    }

    private void endCallFun(Map<String, Object> hmap, AdPipeLineState pipeLineState, AdStage adStage) {
        Integer is_spec;//判断是是否是第一环节和最后一个环节   1表示第一环节 2表示最后一个环节
        long stage_id;
        int deal_result;
        long branch_id;
        Long serial_number;
        Integer stage_code;
        String jkjobName;
        String buildLog;
        String preCommitId = "";
        String commitId = "";
        String commitOperator;
        long step;
        is_spec = Integer.valueOf((String) hmap.get("is_spec"));
        stage_id = Long.valueOf((String) hmap.get("stage_id"));
        deal_result = Integer.valueOf((String) hmap.get("deal_result"));
        branch_id = Long.valueOf((String) hmap.get("branch_id"));
        serial_number = Long.valueOf((String) hmap.get("serial_number"));
        stage_code = Integer.valueOf((String) hmap.get("stage_code"));
        jkjobName = (String) hmap.get("jkjobName");
        buildLog = (String) hmap.get("buildLog");
        step = Long.valueOf((String) hmap.get("step"));
        if (hmap.get("preCommitId") != null && !"null".equals(hmap.get("preCommitId"))) {
            preCommitId = (String) hmap.get("preCommitId");
        }
        if ((is_spec != null && is_spec == 2 && stage_code != 16) || deal_result == 3) {//最后环节和处理失败时记录日志汇总,16对应webScan代码扫描
            if (pipeLineState != null) {
                pipeLineState.setLastBuildResult(deal_result);
                pipeLineState.setBuildSeqId(serial_number);
                pipeLineState.setBranchState(1);
                pipeLineState.setLastBuildDate(new java.util.Date());
                adPipeLineStateDAO.updatePipeLineState(pipeLineState);
            }

            AdBuildLog adBuildLog = buildInfoSinkDAO.qryBuildLog((int) serial_number.longValue(), branch_id);
            if (adBuildLog != null) {
                adBuildLog.setBuildDate(new Date());
                adBuildLog.setBuildResult(deal_result);
                adBuildLog.setLastStep((int) step);
                adBuildLog.setLastStageId(stage_id);
                buildInfoSinkDAO.insertBuildLog(adBuildLog);
            }
            if (stage_code == 102 || stage_code == 103) {//镜像job进行特殊处理
                if (StringUtils.isNotEmpty(buildLog) && buildLog.indexOf("dockerbuildtag:") > 0) {
                    String tag = buildLog.substring(buildLog.indexOf("dockerbuildtag:") + ("dockerbuildtag:").length());
                    tag = tag.substring(0, tag.indexOf("dockerbuildtag:"));
                    List<AdDockImages> adDockImagesList = adDockImagesDAO.getDockImagsByTag(tag);
                    if (adDockImagesList != null && adDockImagesList.size() > 0) {
                        AdDockImages adDockImages = adDockImagesList.get(0);
                        adDockImages.setImageStatus(deal_result);
                        if (deal_result == 2)
                            adDockImages.setHasImage(1);
                        else
                            adDockImages.setHasImage(0);
                        adDockImages.setCreateDate(new Date());
                        adDockImages.save();
                    }
                }
            }
        }
        if (stage_code == 1) {      //下载环节记录一下git的commitid
            if (buildLog.lastIndexOf("commit ") > 0) {
                commitId = buildLog.substring(buildLog.indexOf("commit ") + ("commit ").length());
                if (StringUtils.isNotEmpty(commitId)) {
                    commitOperator = commitId.substring(commitId.indexOf("Author: ") + ("Author: ").length());
                    commitOperator = commitOperator.substring(0, commitOperator.indexOf("\n"));
                    commitId = commitId.substring(0, 40);
                    List<AdProjectDeployPackage> adProjectDeployPackage = packageDAO.qryByCommitAndBranch(commitId, branch_id);
                    if (adProjectDeployPackage != null && adProjectDeployPackage.size() > 0) {
                        adStageDAO.updateStageIsupdate(stage_id, CommConstants.BuildConstants.STAGE.IS_UPDATE.UPDATED);
                    } else {
                        adStageDAO.updateStageIsupdate(stage_id, CommConstants.BuildConstants.STAGE.IS_UPDATE.NOT_UPDATE);
                    }
                    if (!preCommitId.equals(commitId)) {
                        adStageDAO.updateStageCommitId(preCommitId, commitId, commitOperator, stage_id);
                    }
                }
            }
        }
        //处理失败，则进行错误高亮显示
        buildLog = buildLog.substring(buildLog.indexOf(STRING_OLD_NOTIFY) + STRING_OLD_NOTIFY.length()).replaceFirst("(Notifying.*jobnotification')", "").replaceFirst("Aborted by.*\n", "");
        buildLog = buildLog.replaceAll("sshpass\\s[^\\s]*\\s", "sshpass ****** ").replaceAll("sh[\\s&^\n]*((ADCloud_vm_public_deploy)|(ADCloud_dcoss_public_deploy))\\.sh[^\\n]*\n", "部署工程包\n").replaceAll("sh[\\s&^\n]*((ADCloud_vm_public_restart)|(ADCloud_dcoss_public_restart))\\.sh[^\\n]*\n", "重启应用\n");//隐藏密码信息
        if (deal_result == 3) {
            List<AdParaDetail> detail = bsParaDetailDAO.qryByDetails("X", "ERROR_REGEX", "JENKINS_ERROR");
            String regex = "(\\[ERROR\\])|(TypeError)|(Error:)|(npm\\sERR!)|(\\s+at\\s+)|(cannot\\s+access)|(line\\s+\\d{0,9}.+\\.sh)|(错误:)|(Killed\\s+by)|(Exception)|(>\\s+.*FAILED)|(Caused by:)|(Build was aborted)";
            if (detail != null && detail.size() > 0) {
                regex = detail.get(0).getPara1();
            }
            StringBuilder logDom = new StringBuilder("");
            String[] strList = buildLog.split("\n");
            Pattern pattern = Pattern.compile(regex);
            replaceError(strList, logDom, pattern);
            buildLog = logDom.toString();
        }
        if (stage_code != 16) {
            adStageDAO.UpdateAdOperationByName("" + branch_id, "" + deal_result, jkjobName);
        }
        AdStageLogDtl adStageLogDtl = adStageLogDtlDAO.qryStageLogByTotalstep(serial_number, branch_id, stage_id);
        if (adStageLogDtl != null) {
            if (stage_code != 16) {
                adStageLogDtl.setFailLog(buildLog);
                adStageLogDtl.setFinishDate(new java.util.Date());
                adStageLogDtl.setStageResult("" + deal_result);
                adStageLogDtl.setCommitId(commitId);
                adStageLogDtlDAO.updateStageLogDtl(adStageLogDtl);
            } else {
                adStageLogDtl.setFailLog(buildLog);
                adStageLogDtl.setCommitId(commitId);
                adStageLogDtlDAO.updateStageLogDtl(adStageLogDtl);
                Thread runThread = new Thread(new DealWebScan(adStage, adStageLogDtl, operationImpl));
                runThread.start();
            }
        }
    }

    public void jobNotify(String message) throws Exception {
        JobNotifyModel jobNotifyModel = new JobNotifyModel();
        try {
            initJobNotifyModel(jobNotifyModel, message);
            initJobNotifyStageModel(jobNotifyModel);
            dealReturnValue(jobNotifyModel);
            dealJobNotify(jobNotifyModel);
        } catch (Exception e) {
            jobNotifyModel.getStage().setDealResult(3);
            jobNotifyModel.setBuildLog(jobNotifyModel.getBuildLog() + "\nERROR FROM ADCloud : " + e.getMessage());
            dealCompleteNotify(jobNotifyModel);
            throw e;
        }
    }

    private void dealJobNotify(JobNotifyModel jobNotifyModel) {
        if (("STARTED").equals(jobNotifyModel.getBuildPhase())) {
            dealStartNotify(jobNotifyModel);
        } else if (("COMPLETED").equals(jobNotifyModel.getBuildPhase())) {       //表示job执行后触发
            dealCompleteNotify(jobNotifyModel);
        }
    }

    // 从报文中解析数据
    void initJobNotifyModel(JobNotifyModel jobNotifyModel, String message) throws Exception {
        Map<String, Object> messageMap = JsonUtil.jsonToMap(message);
        if (messageMap == null) {
            throw new Exception("报文解析异常");
        }
        if (messageMap.get("name") != null) {
            jobNotifyModel.setJobName((String) messageMap.get("name"));
        } else {
            throw new Exception("获取job名称异常");
        }
        if (messageMap.get("build") != null) {
            Map<String, Object> buildMap = (Map<String, Object>) messageMap.get("build");
            if (buildMap != null) {
                jobNotifyModel.setBuildNumber((Integer) buildMap.get("number"));
                jobNotifyModel.setBuildStatus((String) buildMap.get("status"));
                jobNotifyModel.setBuildPhase((String) buildMap.get("phase"));
                jobNotifyModel.setBuildLog((String) buildMap.get("log"));
            }
        } else {
            throw new Exception("构建信息获取异常");
        }
    }

    // 组装节点相关数据
    void initJobNotifyStageModel(JobNotifyModel jobNotifyModel) {
        JobNotifyStageModel stageModel = new JobNotifyStageModel();
        if ("SUCCESS".equals(jobNotifyModel.getBuildStatus())) {
            stageModel.setDealResult(CommConstants.DEAL_RESULT.SUCCESS);
        } else {
            stageModel.setDealResult(CommConstants.DEAL_RESULT.FAIL);
        }
        AdStage adStage = adStageDAO.QryAdOperationByNameUnique(jobNotifyModel.getJobName());
        initStageModel(stageModel, adStage);
        jobNotifyModel.setStage(stageModel);
        jobNotifyModel.setAdStage(adStage);
    }

    private void initStageModel(JobNotifyStageModel stageModel, AdStage adStage) {
        if (adStage != null) {
            stageModel.setIsSpec(adStage.getIsSpec() == null ? 0 : adStage.getIsSpec());
            stageModel.setStageId(adStage.getStageId());
            stageModel.setStep(adStage.getStep());
            stageModel.setAdBranch(adStage.getAdBranch());
            stageModel.setStageCode(adStage.getStageCode());
            stageModel.setSeqId(adStage.getBuildSeq());
            stageModel.setPipeLineState(adStage.getAdPipeLineState());
            stageModel.setPreCommitId(adStage.getCommitId());
            stageModel.setFirstpreCommitId(adStage.getPreCommitId());
            stageModel.setOpId(adStage.getPipelineOperator() == null ? 1 : adStage.getPipelineOperator());// 流水线创建人
        }
    }

    private void dealReturnValue(JobNotifyModel jobNotifyModel) {
        //先查找SeqId
        AdBuildReturnValue adBuildReturnValue = null;
        if (jobNotifyModel.getStage().getStageId() != 0) {
            adBuildReturnValue = adBuildReturnValueDAO.qryBuildReturnValue
                (jobNotifyModel.getStage().getPipeLineState().getPipelineId(), jobNotifyModel.getStage().getStep());
        }
        //如果是第一步，并且seqId不存在或者seqId小于当前版本，则生成新的seqId
        if (jobNotifyModel.getStage().getIsSpec() == 1 && ("STARTED").equals(jobNotifyModel.getBuildPhase())) {
            if ((adBuildReturnValue == null || jobNotifyModel.getStage().getSeqId() == null
                || jobNotifyModel.getStage().getSeqId() > adBuildReturnValue.getBuildSeq()) || jobNotifyModel.getStage().getOpId() <= 1) {
                jobNotifyModel.getStage().setSeqId(adStageDAO.getSeqId());
            } else {
                jobNotifyModel.getStage().setSeqId(adBuildReturnValue.getBuildSeq());
            }
        }
        //不是最后一步，更新下一步的seqId
        if (jobNotifyModel.getStage().getIsSpec() != 2) {
            adBuildReturnValue = adBuildReturnValueDAO.qryBuildReturnValue(jobNotifyModel.getStage().getPipeLineState().getPipelineId(),
                jobNotifyModel.getStage().getStep() + 1);
            adStageDAO.UpdateAdOperationByAdstageId(jobNotifyModel.getStage().getAdBranch().getBranchId(), jobNotifyModel.getStage().getStep() + 1, jobNotifyModel.getStage().getSeqId());
            if (adBuildReturnValue != null) {
                adBuildReturnValue.setBuildSeq(jobNotifyModel.getStage().getSeqId());
                adBuildReturnValueDAO.updateBuildReturnSeq(adBuildReturnValue);
            } else {
                adBuildReturnValue = new AdBuildReturnValue();
                adBuildReturnValue.setPipelineId(jobNotifyModel.getStage().getPipeLineState().getPipelineId());
                adBuildReturnValue.setBuildSeq(jobNotifyModel.getStage().getSeqId());
                adBuildReturnValue.setStep(jobNotifyModel.getStage().getStep() + 1L);
                adBuildReturnValue.setNext_step(jobNotifyModel.getStage().getStep() + 2L);
                adBuildReturnValueDAO.insertBuildReturnSeq(adBuildReturnValue);
            }
        }
    }

    private void dealStartNotify(JobNotifyModel jobNotifyModel) {
        AdUser adUser = adUserDAO.getUserById(1);
        if (jobNotifyModel.getStage().getIsSpec() == 1 && jobNotifyModel.getStage().getOpId() <= 1) {
            adStageDAO.UpdateAdOperationByEnvId("" + jobNotifyModel.getStage().getAdBranch().getBranchId(), jobNotifyModel.getStage().getSeqId(), jobNotifyModel.getStage().getStageId(), "0");
            if (jobNotifyModel.getStage().getPipeLineState() != null) {
                jobNotifyModel.getStage().getPipeLineState().setAdBranch(jobNotifyModel.getStage().getAdBranch());
                jobNotifyModel.getStage().getPipeLineState().setLastBuildResult(0);
                jobNotifyModel.getStage().getPipeLineState().setBranchState(2);
                jobNotifyModel.getStage().getPipeLineState().setBuildType(jobNotifyModel.getStage().getStageCode());
                adPipeLineStateDAO.updatePipeLineState(jobNotifyModel.getStage().getPipeLineState());
            }
            if (jobNotifyModel.getStage().getOpId() != 0) {
                adUser = adUserDAO.getUserById(jobNotifyModel.getStage().getOpId());
            }
            jobNotifyModel.getAdStage().setPipelineOperator(0);
            adStageDAO.save(jobNotifyModel.getAdStage());
            AdBuildLog adBuildLog = new AdBuildLog();
            adBuildLog.setBuildSeq((long) jobNotifyModel.getStage().getStep());
            adBuildLog.setAdUser(adUser);
            adBuildLog.setBuildType(CommConstants.BuildConstants.BUILDlOG.BUILD_TYPE.BUILD);
            adBuildLog.setCreateDate(new java.util.Date());
            adBuildLog.setAdBranch(jobNotifyModel.getStage().getAdBranch());
            adBuildLog.setBuildResult(0);
            adBuildLog.setState(1);
            adBuildLog.setLastStep(jobNotifyModel.getStage().getStep());
            adBuildLog.setTotalStep((int) jobNotifyModel.getStage().getSeqId().longValue());
            buildInfoSinkDAO.insertBuildLog(adBuildLog);
        }
        if (jobNotifyModel.getStage().getStageCode() == 6) {
            AdStage adStage1 = adStageDAO.qryStageByStep(jobNotifyModel.getStage().getAdBranch().getBranchId(), 1);
            if (adStage1 != null) {
                Thread urunable = new Thread(new UploadArtifactoryRunnable(jobNotifyModel.getStage().getSeqId(), jobNotifyModel.getStage().getAdBranch().getBranchId(), adStage1.getCommitId(),
                    branchDAO, jenkinsInfoDAO,
                    packageDAO, adStageDAO, artifactoryUrl, artifactoryName, artifactoryPassword, bsStaticImpl));
                urunable.start();
            }
        }
        AdStageLogDtl buildLogDtl;
        if (jobNotifyModel.getStage().getIsSpec() == 1 && jobNotifyModel.getStage().getOpId() <= 1) {
            insertLogDtl(jobNotifyModel);
        } else {
            buildLogDtl = adStageLogDtlDAO.qryStageLogByTotalstep(jobNotifyModel.getStage().getSeqId(), jobNotifyModel.getStage().getAdBranch().getBranchId(), jobNotifyModel.getStage().getStageId());
            //如果是通过部署环节上传部署包的，会查不到记录，做兼容
            if (buildLogDtl == null) {
                insertLogDtl(jobNotifyModel);
                buildLogDtl = adStageLogDtlDAO.qryStageLogByTotalstep(jobNotifyModel.getStage().getSeqId(), jobNotifyModel.getStage().getAdBranch().getBranchId(), jobNotifyModel.getStage().getStageId());
            }
            buildLogDtl.setBuildSeqId(Long.valueOf(jobNotifyModel.getBuildNumber()));
            adStageLogDtlDAO.insertStageLogDtl(buildLogDtl);
        }
    }

    private void dealCompleteNotify(JobNotifyModel jobNotifyModel) {
        AdStage nextStage = adStageDAO.qryByStep(jobNotifyModel.getStage().getAdBranch().getBranchId(), jobNotifyModel.getStage().getStep() + 1);
        int operId = 0;
        try {
            operId = jobNotifyModel.getAdStage().getPipelineOperator();
        } catch (Exception e) {
        }
        //最后环节和处理失败时记录日志汇总,16对应webScan代码扫描
        if ((jobNotifyModel.getStage().getIsSpec() == 2 || nextStage == null) && jobNotifyModel.getStage().getDealResult() == 2) {
            long triggerBranch = jobNotifyModel.getStage().getAdBranch().getTriggerBranch();
            AdBranch adBranch;
            int branchType;
            if (triggerBranch != -1) {
                adBranch = adBranchDAO.qryById(triggerBranch);
                if (adBranch != null && adBranch.getState() == 1) {
                    branchType = adBranch.getBranchType();
                    jenkinsService.triggerJenkins(triggerBranch, branchType, jobNotifyModel.getStage().getOpId() + "", false, "", false);
                }
            }
        }
        if ((jobNotifyModel.getStage().getIsSpec() == 2 && jobNotifyModel.getStage().getStageCode() != 16) || jobNotifyModel.getStage().getDealResult() == 3 || (nextStage == null)) {
            if (jobNotifyModel.getStage().getPipeLineState() != null) {
                jobNotifyModel.getStage().getPipeLineState().setLastBuildResult(jobNotifyModel.getStage().getDealResult());
                jobNotifyModel.getStage().getPipeLineState().setBuildSeqId(jobNotifyModel.getStage().getSeqId());
                jobNotifyModel.getStage().getPipeLineState().setBranchState(1);
                jobNotifyModel.getStage().getPipeLineState().setLastBuildDate(new java.util.Date());
                adPipeLineStateDAO.updatePipeLineState(jobNotifyModel.getStage().getPipeLineState());
            }
            AdBuildLog adBuildLog = buildInfoSinkDAO.qryBuildLog((int) jobNotifyModel.getStage().getSeqId().longValue(), jobNotifyModel.getStage().getAdBranch().getBranchId());
            if (adBuildLog != null) {
                adBuildLog.setBuildDate(new Date());
                adBuildLog.setBuildResult(jobNotifyModel.getStage().getDealResult());
                adBuildLog.setLastStep(jobNotifyModel.getStage().getStep());
                adBuildLog.setLastStageId(jobNotifyModel.getStage().getStageId());
                buildInfoSinkDAO.insertBuildLog(adBuildLog);
            }
            adStageDAO.UpdateStageOperatorById(jobNotifyModel.getStage().getAdBranch().getBranchId(), 1L, "1");
            if (jobNotifyModel.getStage().getStageCode() == 102 || jobNotifyModel.getStage().getStageCode() == 103) {//镜像job进行特殊处理
                if (StringUtils.isNotEmpty(jobNotifyModel.getBuildLog()) && jobNotifyModel.getBuildLog().indexOf("dockerbuildtag:") > 0) {
                    String tag = jobNotifyModel.getBuildLog().substring(jobNotifyModel.getBuildLog().indexOf("dockerbuildtag:") + ("dockerbuildtag:").length());
                    tag = tag.substring(0, tag.indexOf("dockerbuildtag:"));
                    List<AdDockImages> adDockImagesList = adDockImagesDAO.getDockImagsByTag(tag);
                    if (adDockImagesList != null && adDockImagesList.size() > 0) {
                        AdDockImages adDockImages = adDockImagesList.get(0);
                        adDockImages.setImageStatus(jobNotifyModel.getStage().getDealResult());
                        if (jobNotifyModel.getStage().getDealResult() == 2)
                            adDockImages.setHasImage(1);
                        else
                            adDockImages.setHasImage(0);
                        adDockImages.setCreateDate(new Date());
                        adDockImages.save();
                    }
                }
            }
        }
        String commitId = "";
        String commitOperator;
        if (jobNotifyModel.getStage().getStageCode() == 1) {      //下载环节记录一下git的commitId
            if (jobNotifyModel.getBuildLog().lastIndexOf("commit ") > 0) {
                commitId = jobNotifyModel.getBuildLog().substring(jobNotifyModel.getBuildLog().indexOf("commit ") + ("commit ").length());
                if (StringUtils.isNotEmpty(commitId)) {
                    commitOperator = commitId.substring(commitId.indexOf("Author: ") + ("Author: ").length());
                    commitOperator = commitOperator.substring(0, commitOperator.indexOf("\n"));
                    commitId = commitId.substring(0, 40);
                    List<AdProjectDeployPackage> adProjectDeployPackage = packageDAO.qryByCommitAndBranch(commitId, jobNotifyModel.getStage().getAdBranch().getBranchId());
                    if (adProjectDeployPackage != null && adProjectDeployPackage.size() > 0) {
                        adStageDAO.updateStageIsupdate(jobNotifyModel.getStage().getStageId(), CommConstants.BuildConstants.STAGE.IS_UPDATE.UPDATED);
                    } else {
                        adStageDAO.updateStageIsupdate(jobNotifyModel.getStage().getStageId(), CommConstants.BuildConstants.STAGE.IS_UPDATE.NOT_UPDATE);
                    }
                    if (!commitId.equals(jobNotifyModel.getStage().getPreCommitId()) || StringUtils.isEmpty(jobNotifyModel.getStage().getPreCommitId())) {
                        adStageDAO.updateStageCommitId(jobNotifyModel.getStage().getPreCommitId(), commitId, commitOperator, jobNotifyModel.getStage().getStageId());
                    }
                    if(StringUtils.isEmpty(jobNotifyModel.getStage().getFirstpreCommitId())){
                        adStageDAO.updateStageCommitId(jobNotifyModel.getStage().getPreCommitId(), "","", jobNotifyModel.getStage().getStageId());
                    }
                }
            }
        }
        // 替换日志内容，处理日志信息
        highLightLog(jobNotifyModel);
        if (jobNotifyModel.getStage().getStageCode() != 16) {
            adStageDAO.UpdateAdOperationByName("" + jobNotifyModel.getStage().getAdBranch().getBranchId(), "" + jobNotifyModel.getStage().getDealResult(), jobNotifyModel.getJobName());
        }
        AdStageLogDtl adStageLogDtl = adStageLogDtlDAO.qryStageLogByTotalstep(jobNotifyModel.getStage().getSeqId(), jobNotifyModel.getStage().getAdBranch().getBranchId(), jobNotifyModel.getStage().getStageId());
        if (adStageLogDtl != null) {
            if (jobNotifyModel.getStage().getStageCode() != 16) {
                adStageLogDtl.setFailLog(jobNotifyModel.getBuildLog());
                adStageLogDtl.setFinishDate(new java.util.Date());
                adStageLogDtl.setStageResult("" + jobNotifyModel.getStage().getDealResult());
                adStageLogDtl.setCommitId(commitId);
                adStageLogDtlDAO.updateStageLogDtl(adStageLogDtl);
            } else {
                adStageLogDtl.setFailLog(jobNotifyModel.getBuildLog());
                adStageLogDtl.setCommitId(commitId);
                adStageLogDtlDAO.updateStageLogDtl(adStageLogDtl);
                Thread runThread = new Thread(new DealWebScan(jobNotifyModel.getAdStage(), adStageLogDtl, operationImpl));
                runThread.start();
            }
        }
        if (jobNotifyModel.getStage().getDealResult() == 2) {
            if (nextStage != null) {
                jobNotifyModel.setAdStage(nextStage);
                jobNotifyModel.setJobName(nextStage.getJenkinsJobName());
                initStageModel(jobNotifyModel.getStage(), nextStage);
                insertLogDtl(jobNotifyModel);
                jenkinsImpl.triggerJenkins(nextStage);
            }
        }
        //编译、部署环节出错，邮件通知 (5 == jobNotifyModel.getStage().getStageCode() || 6 == jobNotifyModel.getStage().getStageCode())
        //所有环节失败都通知
        if (3 == jobNotifyModel.getStage().getDealResult()) {
            AdUser oper = adUserDAO.getUserById(operId);
            if (oper != null && oper.getEmail() != null && notifySend(oper.getNotification(), "email")) {
                String email = oper.getEmail();
                AdBranch adbranch = jobNotifyModel.getAdStage().getAdBranch();
                AdProject adproject = adbranch.getAdProject();
                AdGroup adGroup = adproject.getAdGroup();

                SimpleDateFormat formatter;
                formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                StringBuffer title = new StringBuffer();
                title.append("[系统]").append(adGroup.getGroupDesc());
                title.append("[应用]").append(adproject.getProjectName());
                title.append("[流水]").append(adbranch.getBranchDesc());
                title.append("[结束时间]").append(formatter.format(new java.util.Date()));
                title.append("构建失败！");

                MailUtil.simpleMailSend(email, null, "构建日志：\n" + jobNotifyModel.getBuildLog(), title.toString());
            }
        }
    }

    private void insertLogDtl(JobNotifyModel jobNotifyModel) {
        AdUser adUser = new AdUser();
        adUser.setUserId(jobNotifyModel.getStage().getOpId());
        AdStageLogDtl buildLogDtl;
        buildLogDtl = new AdStageLogDtl();
        buildLogDtl.setAdBranch(jobNotifyModel.getStage().getAdBranch());
        buildLogDtl.setTotalStep(jobNotifyModel.getStage().getSeqId());
        buildLogDtl.setStep((long) jobNotifyModel.getStage().getStep());
        buildLogDtl.setAdStage(jobNotifyModel.getAdStage());
        buildLogDtl.setAdUser(adUser);
        buildLogDtl.setState(1L);
        buildLogDtl.setBuildSeqId(Long.valueOf(jobNotifyModel.getBuildNumber()));
        buildLogDtl.setBeginDate(new Date());
        adStageLogDtlDAO.insertStageLogDtl(buildLogDtl);
        adStageDAO.UpdateAdOperationByName("" + jobNotifyModel.getStage().getAdBranch().getBranchId(), "1", jobNotifyModel.getJobName());
    }

    void highLightLog(JobNotifyModel jobNotifyModel) {
        //处理失败，则进行错误高亮显示
        jobNotifyModel.setBuildLog(jobNotifyModel.getBuildLog().substring
            (jobNotifyModel.getBuildLog().indexOf(STRING_NEW_NOTIFY) + STRING_NEW_NOTIFY.length())
            .replaceFirst("(Notifying.*" + STRING_NEW_NOTIFY + ")", "").replaceFirst("Aborted by.*\n", ""));
        jobNotifyModel.setBuildLog(jobNotifyModel.getBuildLog().replaceAll("sshpass\\s[^\\s]*\\s", "sshpass ****** ").replaceAll("sh[\\s&^\n]*((ADCloud_vm_public_deploy)|(ADCloud_dcoss_public_deploy))\\.sh[^\\n]*\n", "部署工程包\n").replaceAll("sh[\\s&^\n]*((ADCloud_vm_public_restart)|(ADCloud_dcoss_public_restart))\\.sh[^\\n]*\n", "重启应用\n"));//隐藏密码信息
        if (jobNotifyModel.getStage().getDealResult() == 3) {
            List<AdParaDetail> detail = bsParaDetailDAO.qryByDetails("X", "ERROR_REGEX", "JENKINS_ERROR");
            String regex = "(\\[ERROR])|(TypeError)|(Error:)|(error)|(npm\\sERR!)|(\\s+at\\s+)|(cannot\\s+access)|(cannot\\s+find)|(line\\s+\\d{0,9}.+\\.sh)|(\u9519\u8bef:)|(Killed\\s+by)|(Exception)|(>\\s+.*FAILED)|(Caused by:)|(Build was aborted)";
            if (detail != null && detail.size() > 0) {
                regex = detail.get(0).getPara1();
            }
            StringBuilder logDom = new StringBuilder("");
            String[] strList = jobNotifyModel.getBuildLog().split("\n");
            Pattern pattern = Pattern.compile(regex);
            replaceError(strList, logDom, pattern);
            jobNotifyModel.setBuildLog(logDom.toString());
        }
    }

    public void replaceError(String[] strList, StringBuilder logDom, Pattern pattern) {
        String regex = "(WARN)|(warn)";
        for (String s : strList) {
            if (Pattern.compile(regex).matcher(s).find() == false) {
                if (pattern.matcher(s).find()) {
                    s = "errorJenkins" + s;
                }
                logDom.append(s).append("\n");
            }
        }
    }
}

