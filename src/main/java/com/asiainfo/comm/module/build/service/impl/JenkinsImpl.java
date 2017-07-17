package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.SysEnvConfigExt;
import com.asiainfo.comm.common.pojo.pojoExt.SysReformBranchPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.SysReformProjectPojoExt;
import com.asiainfo.comm.common.pojo.pojoExt.SysStagePojoExt;
import com.asiainfo.comm.common.pojo.pojoMaster.CommonPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.ManualHandPojo;
import com.asiainfo.comm.externalservice.jenkins.client.JenkinsClient;
import com.asiainfo.comm.externalservice.jenkins.client.JenkinsPoster;
import com.asiainfo.comm.externalservice.jenkins.client.JobData;
import com.asiainfo.comm.externalservice.jenkins.guice.JenkinsWsClientGuiceModule;
import com.asiainfo.comm.externalservice.jenkins.jobconfig.JobConfigUtil;
import com.asiainfo.comm.externalservice.jenkins.jobconfig.entity.*;
import com.asiainfo.comm.externalservice.jenkins.jobs.Job;
import com.asiainfo.comm.externalservice.jenkins.jobs.JobImpl;
import com.asiainfo.comm.module.build.dao.impl.*;
import com.asiainfo.comm.module.common.AdParaDetailDAO;
import com.asiainfo.comm.module.common.AdStaticDataDAO;
import com.asiainfo.comm.module.deploy.dao.impl.AdDcosDeployInfoDAO;
import com.asiainfo.comm.module.deploy.dao.impl.SystemDeployDAO;
import com.asiainfo.comm.module.deploy.service.impl.SystemDeployImpl;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.models.functionModels.AdBranchList;
import com.asiainfo.util.CommConstants;
import com.asiainfo.util.XmlUtils;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;

/**
 * Created by YangRY
 * on 2016/6/20 0020.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class JenkinsImpl {
    protected static Logger logger = LoggerFactory.getLogger(JenkinsImpl.class);
    @Autowired
    AdJenkinsInfoDAO jkDAO;
    @Autowired
    AdParaDetailDAO paraDAO;
    @Autowired
    AdBranchDAO branchDAO;
    @Autowired
    AdPipeLineStateDAO stateDAO;
    @Autowired
    AdStageDAO adStageDAO;
    @Autowired
    AdBranchImpl branchImpl;
    @Autowired
    AdRmpBranchRelateDAO branchRelateDAO;
    @Autowired
    AdBuildTaskRelateImpl buildTaskRelateImpl;
    @Autowired
    AdStaticDataDAO bsStaticDataDAO;
    @Autowired
    AdBranchRelatDAO adBranchRelatDAO;
    @Autowired
    AdDockImagesDAO adDockImagesDAO;
    @Autowired
    AdProjectDAO adProjectDAO;
    @Autowired
    AdStageLogDtlDAO adStageLogDtlDAO;
    @Autowired
    AdPipeLineStateDAO adPipeLineStateDAO;
    @Autowired
    AdBuildLogImpl adBuildLogImpl;
    @Autowired
    AdBranchDAO adBranchDAO;
    @Autowired
    AdParaDetailDAO bsParaDetailDAO;
    @Autowired
    AdVirtualEnvironmentDAO virtualEnvironmentDAO;
    @Autowired
    SystemDeployDAO systemDeployDAO;
    @Autowired
    SystemDeployImpl systemDeployImpl;
    @Autowired
    AdDcosDeployInfoDAO dcosDeployInfoDAO;
    @Autowired
    AdDcosDeployDtlDAO adDcosDeployDtlDAO;
    @Autowired
    AdGroupImpl adGroupImpl;


    public ManualHandPojo triggerJenkins(long envId, int buildType, String opId, boolean isProd, String tag) {
        return triggerJenkins(envId, buildType, opId, isProd, tag, true);
    }

    public ManualHandPojo triggerJenkins(long envId, int buildType, String opId, boolean isProd, String tag, boolean flag) {
        ManualHandPojo poj = new ManualHandPojo();
        if (envId != 0) {
            AdStage adStage;
            AdBranch branch = branchDAO.getEnvById(envId);
            AdPipeLineState envState = stateDAO.qryByEnvIdBuildType(envId, buildType);
            long seqId = adStageDAO.createSeqInfo(envState.getPipelineId());
            poj.setSeqId(seqId);
            try {
                if (branch != null) {
                    log.error("******************************环境编号***********************" + branch.getBranchId());
                    AdJenkinsInfo jkInfo = jkDAO.qryByJkId(branch.getAdJenkinsInfo().getJenkinsId());
                    if (jkInfo == null) {
                        throw new Exception("获取jenkins登陆信息失败");
                    }
                    List<AdStage> opts = adStageDAO.QryAdOperationByEnvIdType(envId);
                    if (opts != null && opts.size() > 0 && envState.getBranchState() != 2) {
                        adStage = opts.get(0);
                    } else {
                        throw new Exception("环境正在构建");
                    }
                    if (flag) {
                        if (adStage != null) {
                            adStageDAO.UpdateAdStageByBranchId(envId, 0, opId);
                            adStageDAO.UpdateAdOperationByName(seqId, envId, 1L, adStage.getJenkinsJobName(), opId);
                            AdStageLogDtl buildLogDtl = new AdStageLogDtl();
                            //TODO 待优化
                            buildLogDtl.setBeginDate(new Date());
                            AdUser user = new AdUser();
                            user.setUserId(Long.parseLong(opId));
                            buildLogDtl.setAdUser(user);
                            buildLogDtl.setAdStage(adStage);
                            buildLogDtl.setAdBranch(branch);
                            buildLogDtl.setState(1L);
                            buildLogDtl.setStageResult("1");
                            buildLogDtl.setStep(1L);
                            buildLogDtl.setTotalStep(seqId);
                            adStageLogDtlDAO.insertStageLogDtl(buildLogDtl);
                            AdBuildLog adbuildlog = new AdBuildLog();
                            adbuildlog.setAdBranch(branch);
                            adbuildlog.setAdUser(user);
                            adbuildlog.setBuildType(CommConstants.BuildConstants.BUILDlOG.BUILD_TYPE.BUILD);
                            adbuildlog.setState(1);
                            adbuildlog.setBuildDate(new Date());
                            adbuildlog.setBuildResult(1);
                            adbuildlog.setBuildSeq(1L);
                            adbuildlog.setCreateDate(new Date());
                            adbuildlog.setTotalStep(Long.valueOf(seqId).intValue());
                            adbuildlog.setLastStep(adStage.getStep());
                            adbuildlog.setLastStageId(adStage.getStageId());
                            adBuildLogImpl.insertBuildLog(adbuildlog);
                            envState.setBranchState(2);
                            stateDAO.savePipeLineState(envState);
                        } else {
                            throw new Exception("获取adstage信息失败");
                        }
                    }
                    String jkIp = jkInfo.getJenkinsUrl();
                    String jkUserName = jkInfo.getJenkinsUsername();
                    String jkPassword = jkInfo.getJenkinsPassword();
                    int serverPort = jkInfo.getServerPort();
                    if (isProd) {
                        JobData jd = new JobData();
                        jd.setServer("http://" + jkIp);
                        jd.setPort("" + serverPort);
                        jd.setUsername(jkUserName);
                        jd.setPassword(jkPassword);
                        List<NameValuePair> parameters = jd.getParameters();
                        parameters.add(new BasicNameValuePair("tag", tag));
                        jd.setJob(adStage.getJenkinsJobName());
                        JenkinsPoster jp = new JenkinsPoster(jd);
                        boolean seccessFlag = jp.postJenkinsJob();
                        if (!seccessFlag) {
                            throw new Exception("构建失败");
                        }
                    } else {
                        String jkUrl = "http://" + jkIp + ":" + serverPort/*serverPort*/;
                        Injector injector;
                        injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                            jkUrl), jkUserName, jkPassword));
                        JenkinsClient client = injector.getInstance(JenkinsClient.class);
                        Job job = new JobImpl(adStage.getJenkinsJobName());
                        client.buildJob(job);
                        client.close();
                    }
                }
            } catch (Exception e) {
                log.error("", e);
                e.printStackTrace();
                poj.setRetCode("500");
                poj.setRetMessage(e.getMessage().trim());
            }
        }
        return poj;
    }


    public JenkinsClient getJenkinsClient(AdJenkinsInfo jkInfo) throws Exception {
        if (jkInfo == null) {
            throw new Exception("获取jenkins登陆信息失败");
        }
        String jkIp = jkInfo.getJenkinsUrl();
        String jkUserName = jkInfo.getJenkinsUsername();
        String jkPassword = jkInfo.getJenkinsPassword();
        int serverPort = jkInfo.getServerPort();
        String jkUrl = "http://" + jkIp + ":" + serverPort/*serverPort*/;
        Injector injector = null;
        injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
            jkUrl), jkUserName, jkPassword));
        return injector.getInstance(JenkinsClient.class);
    }

    //从某个节点开始构建（目前用于回滚）
    public ManualHandPojo triggerJenkins(AdBranch adBranch, AdStage curAdStage) {
        ManualHandPojo commonPojo = new ManualHandPojo();
        try {
            if (adBranch != null && curAdStage != null) {
                AdPipeLineState envState = stateDAO.qryByEnvIdBuildType(adBranch.getBranchId(), adBranch.getBranchType());
                long seqId = adStageDAO.createSeqInfo(envState.getPipelineId(), curAdStage.getStep());
                commonPojo.setSeqId(seqId);
                log.error("******************************环境编号***********************" + adBranch.getBranchId());
                AdJenkinsInfo jkInfo = jkDAO.qryByJkId(adBranch.getAdJenkinsInfo().getJenkinsId());
                JenkinsClient client = getJenkinsClient(jkInfo);
                Job job = new JobImpl(curAdStage.getJenkinsJobName());
                client.buildJob(job);
                client.close();
            }
        } catch (Exception e) {
            log.error("", e);
            e.printStackTrace();
            commonPojo.setRetCode("500");
            commonPojo.setRetMessage(e.getMessage().trim());
        } finally {
            return commonPojo;
        }
    }

    // 仅调用Jenkins执行，不做其他操作
    public void triggerJenkins(AdStage stage) {
        try {
            AdJenkinsInfo jkInfo = jkDAO.qryByJkId(stage.getAdBranch().getAdJenkinsInfo().getJenkinsId());
            if (jkInfo == null) {
                throw new Exception("获取jenkins登陆信息失败");
            }
            String jkIp = jkInfo.getJenkinsUrl();
            String jkUserName = jkInfo.getJenkinsUsername();
            String jkPassword = jkInfo.getJenkinsPassword();
            int serverPort = jkInfo.getServerPort();
            String jkUrl = "http://" + jkIp + ":" + serverPort;
            Injector injector;
            injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                jkUrl), jkUserName, jkPassword));
            JenkinsClient client = injector.getInstance(JenkinsClient.class);
            Job job = new JobImpl(stage.getJenkinsJobName());
            client.buildJob(job);
            client.close();
        } catch (Exception e) {
            log.error("", e);
        }
    }




    public Map qryJenkinsConfig(long branchId) throws Exception {

        Injector injector;
        Map<Object, Object> retMap = new HashMap<>();
        long ll_pipelinedId = 0;
        AdBranch adBranch = branchDAO.getEnvById(branchId);
        if (adBranch != null) {
            AdJenkinsInfo jenkinsInfo = qryJenkinsInfo(adBranch);
            if (jenkinsInfo != null) {
                List<AdStage> stageList = adStageDAO.QryValidAdOperationByEnvId(branchId);
                Map<String, String> stageMap;
                Map<String, String> type_names = bsStaticDataDAO.qryStaticDatas("BUILDER_TYPE");
                List<Map<String, String>> stagedetail = new ArrayList<Map<String, String>>();
                String url = "", branchname;
                String shell = "";
                String para_type = "";
                String jenkinJob = "";
                String downStageName = "";
                String spec = "";
                try {
                    for (AdStage adStage : stageList) {
                        shell = "";
                        branchname = "";
                        url = "";
                        spec = "";
                        JobConfig jobinfo;
                        jenkinJob = adStage.getJenkinsJobName();
                        if (StringUtils.isEmpty(adStage.getStageConfig())) {
                            injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                                jenkinsInfo.getJenkinsUrl()), jenkinsInfo.getJenkinsUsername(), jenkinsInfo.getJenkinsPassword()));
                            JenkinsClient client = injector.getInstance(JenkinsClient.class);
                            Job job = client.retrieveJob(jenkinJob);
                            jobinfo = job.getJobinfo();
                            client.close();
                            adStageDAO.updateStageConfig(adStage.getStageId().intValue(), job.asXml());
                        } else {
                            Job job = JobImpl.fromXml(jenkinJob, adStage.getStageConfig());
                            jobinfo = job.getJobinfo();
                        }
                        stageMap = new HashMap<String, String>();
                        stageMap.put("jobname", adStage.getJenkinsJobName());
                        stageMap.put("stagename", type_names.get(adStage.getStageCode() + ""));
                        stageMap.put("stagecode", adStage.getStageCode() + "");
                        if (("1").equals(adStage.getStageCode() + "")) {
                            downStageName = adStage.getJenkinsJobName();
                        }
                        if (jobinfo.getBuilders() != null && jobinfo.getBuilders().size() > 0) {
                            shell = jobinfo.getBuilders().get(0).getCommand();
                            if (shell != null) {
                                shell = shell.replaceAll(CommConstants.getDownJobName(jenkinsInfo.getPathShell(), downStageName), "");
                            }
                        }
                        stageMap.put("shell", shell);
                        ll_pipelinedId = adStage.getAdPipeLineState().getPipelineId();
                        if (adStage.getStageCode() == 1) {
                            if (jobinfo.getScm() != null && jobinfo.getScm() instanceof GitSCM) {
                                if (((GitSCM) jobinfo.getScm()).getUserRemoteConfigs().size() > 0) {
                                    url = ((GitSCM) jobinfo.getScm()).getUserRemoteConfigs().get(0).getUrl();
                                    branchname = ((GitSCM) jobinfo.getScm()).getBranches().get(0).getName();
                                }
                            }
                            stageMap.put("dowpath", url);
                            stageMap.put("branch", branchname);
                        }
                        spec = adStage.getJobSchedule();
                        stageMap.put("spec", "" + spec);
                        stagedetail.add(stageMap);
                    }

                    //添加环境信息
                    if (adBranch.getEnvType() != null && !adBranch.getEnvType().equals("")) {
                        SysEnvConfigExt envConfigExt = systemDeployImpl.getSysEnvConfigExt(adBranch);
                        retMap.put("envConfig", envConfigExt);
                    } else {
                        retMap.put("envConfig", null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (logger.isErrorEnabled()) {
                        logger.error("jenkins error" + e);
                    }
                }
                para_type = CommConstants.qryBuildType();

                List<Map<String, String>> defaultStages = new ArrayList<Map<String, String>>();
                Map<String, String> defaultstage;
                List<AdStaticData> adStaticDatas = bsStaticDataDAO.qryByCodeType(para_type);
                for (AdStaticData adStaticData : adStaticDatas) {
                    defaultstage = new HashMap<>();
                    defaultstage.put("stagecode", adStaticData.getCodeValue());
                    defaultstage.put("stagename", adStaticData.getCodeName());
                    defaultstage.put("notdel", adStaticData.getCodeDesc() == null ? "" : adStaticData.getCodeDesc());
                    defaultstage.put("suffixName", adStaticData.getCodeTypeAlias());
                    defaultStages.add(defaultstage);
                }
                retMap.put("detail", stagedetail);
                retMap.put("branchid", "" + branchId);
                retMap.put("pipelineid", "" + ll_pipelinedId);
                retMap.put("type", adBranch.getBranchType());//1开发 、2测试  3、准发布
                retMap.put("defaultstage", defaultStages);
                retMap.put("originPath", adBranch.getOriginPath());
                if (StringUtils.isNotEmpty(adBranch.getBuildFileType())) {
                    String[] buildFileTypes = adBranch.getBuildFileType().split(",");
                    Map<String, Boolean> typeMap = new HashMap<String, Boolean>();
                    for (String buildFile : buildFileTypes) {
                        typeMap.put(buildFile, true);
                    }
                    retMap.put("buildFileType", typeMap);
                }

            } else {
                throw new Exception("获取jenkins登陆信息失败");
            }
        }

        return retMap;
    }

    private AdJenkinsInfo qryJkInfoByBranch(long branchId) throws Exception {
        AdBranch branch = branchDAO.getEnvById(branchId);
        return qryJenkinsInfo(branch);
    }


    private AdJenkinsInfo qryJenkinsInfo(AdBranch branch) throws Exception {
        AdJenkinsInfo jkInfo = jkDAO.qryByJkId(branch.getAdJenkinsInfo().getJenkinsId());
        if (jkInfo != null) {
            int serverPort = jkInfo.getServerPort();
            String jkUrl = "http://" + jkInfo.getJenkinsUrl() + ":" + serverPort;
            jkInfo.setJenkinsUrl(jkUrl);
            jkInfo.setRemarks("" + branch.getBranchType());
        }
        return jkInfo;
    }

    public void saveBranchShell(long pipelineId, long branchId, String shell, AdBranchShell adBranchShell) {
        if (adBranchShell != null) {
            adBranchShell.setShell(shell);
            adBranchShell.save();
        } else {
            adBranchShell = new AdBranchShell();
            adBranchShell.setBranchId(branchId);
            adBranchShell.setPipelineId(pipelineId);
            adBranchShell.setShell(shell);
            adBranchShell.save();
        }
    }

    public String UpdateJkJob(List<Map<String, String>> detail, long branchId) throws Exception {
        String jobName;
        String stagecode;
        String shell = "";
        String oldShell = "";
        String old_branch = "";
        String branch = "";
        String spec = "";
        Injector injector;
        String xml = "";
        int i = 0;
        String oldJobName = "";
        String ischoosestage = "";
        String downpath = "";
        List<JobProperty> jobPropertyList = new ArrayList<JobProperty>();
        List<Shell> shellList = null;
        JSONObject namejson = new JSONObject();
        JSONObject scriptjson = null;
        JSONObject branchjson = null;
        JSONObject dowpathjson = null;
        AdBranch adBranch = branchDAO.getEnvById(branchId);
        AdJenkinsInfo jenkinsInfo = qryJkInfoByBranch(branchId);
        if (detail != null) {
            String downStageName = "";
            for (Map<String, String> hmap : detail) {
                scriptjson = new JSONObject();
                branchjson = new JSONObject();
                dowpathjson = new JSONObject();
                jobName = hmap.get("name");
                stagecode = hmap.get("stagecode");
                shell = hmap.get("script");
                downpath = hmap.get("downpath");
                ischoosestage = hmap.get("ischoosestage");
                spec = hmap.get("spec");
                if (("00000").equals(spec)) {
                    spec = "H/2 * * * *";
                    AdStaticData jobChedule = bsStaticDataDAO.qryStaticDataByCodeValue("JOB_SCHEDULE", "DOWNSTAGE");
                    if (jobChedule != null) {
                        spec = jobChedule.getCodeName();
                    }
                }
                if (shell != null) {
                    shell = shell.replaceAll("\\\\n", "\\\r\\\n");
                }
                if (StringUtils.isEmpty(stagecode)) {
                    continue;
                }
                if (("false").equals(ischoosestage)) {
                    if (StringUtils.isNotEmpty(jobName)) {//删除环节
                        injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                            jenkinsInfo.getJenkinsUrl()), jenkinsInfo.getJenkinsUsername(), jenkinsInfo.getJenkinsPassword()));
                        JenkinsClient client = injector.getInstance(JenkinsClient.class);
                        Job job = new JobImpl(jobName);
                        client.deleteJob(job);
                        client.close();
                        continue;
                    } else {
                        continue;
                    }
                }
                i = i + 1;
                if (StringUtils.isEmpty(jobName)) {//新增环节
                    try {
                        jobName = "test";
                        if (adBranch != null) {
                            jobName = adBranch.getBranchName();
                        }
                        String para_type = "";
                        para_type = CommConstants.qryBuildType();
                        AdStaticData adStaticData = bsStaticDataDAO.qryStaticDataByCodeValue(para_type, stagecode);
                        if (adStaticData != null) {
                            jobName = jobName + "-" + adStaticData.getCodeTypeAlias();
                        }
                        hmap.put("name", jobName);
                        injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                            jenkinsInfo.getJenkinsUrl()), jenkinsInfo.getJenkinsUsername(), jenkinsInfo.getJenkinsPassword()));
                        JenkinsClient client = injector.getInstance(JenkinsClient.class);
                        JobConfig jobConfig = new JobConfig();
                        jobConfig.setProperties(jobPropertyList);

                        shellList = new ArrayList<Shell>();
                        Shell shell1 = new Shell();
                        if (i > 1) {        //关联上级job
                            shell1.setCommand(CommConstants.getDownJobName(jenkinsInfo.getPathShell(), downStageName) + shell);
                        } else {
                            downStageName = jobName;
                            shell1.setCommand(shell);
                            if (StringUtils.isNotEmpty(spec)) {
                                SCMTrigger scmTrigger = new SCMTrigger();
                                scmTrigger.setSpec(spec);
                                scmTrigger.setIgnorePostCommitHooks(false);
                                jobConfig.addTrigger(scmTrigger);
                            }
                        }
                        shellList.add(shell1);
                        jobConfig.setBuilders(shellList);
                        jobConfig.setDescription(jobName);
                        ExtendedEmailPublisher extendedEmailPublisher = new ExtendedEmailPublisher();
                        extendedEmailPublisher.addConfiguredTrigger(new FailureTrigger());
                        jobConfig.addPublisher(extendedEmailPublisher);
                        if ("mesos".equals(jenkinsInfo.getJenkinsMode())) {
                            jobConfig.setAssignedNode("mesos");
                            jobConfig.setCanRoam(false);
                        }
                        xml = JobConfigUtil.toXMLString(jobConfig);
                        xml = "<?xml version='1.0' encoding='UTF-8'?>" + xml;
                        client.createJob(jobName, XmlUtils.string2Doc(xml));
                        client.close();
                        hmap.put("stageconfig", xml);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                        jenkinsInfo.getJenkinsUrl()), jenkinsInfo.getJenkinsUsername(), jenkinsInfo.getJenkinsPassword()));
                    JenkinsClient client = injector.getInstance(JenkinsClient.class);
                    Job job = client.retrieveJob(jobName);
                    JobConfig jobinfo = job.getJobinfo();
                    if (jobinfo.getBuilders() != null && jobinfo.getBuilders().size() > 0) {
                        oldShell = jobinfo.getBuilders().get(0).getCommand();
                    }
                    if (("1").equals(stagecode)) {//表示第一个环节
                        branch = hmap.get("branch");
                        if (((GitSCM) jobinfo.getScm()).getUserRemoteConfigs().size() > 0) {
                            old_branch = ((GitSCM) jobinfo.getScm()).getBranches().get(0).getName();
                        }
                        downStageName = jobName;
                    }
                    jobPropertyList = jobinfo.getProperties();
                    shellList = jobinfo.getBuilders();
                    client.close();
                    injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                        jenkinsInfo.getJenkinsUrl()), jenkinsInfo.getJenkinsUsername(), jenkinsInfo.getJenkinsPassword()));
                    client = injector.getInstance(JenkinsClient.class);
                    if (("1").equals(stagecode)) {
                        if (StringUtils.isNotEmpty(branch) && !(branch).equals(old_branch)) {
                            ((GitSCM) jobinfo.getScm()).getBranches().get(0).setName(branch);
                        }
                    }
                    if (StringUtils.isNotEmpty(shell) && !(shell).equals(oldShell)) {
                        if (jobinfo.getBuilders() != null && jobinfo.getBuilders().size() > 0) {
                            if (oldShell != null && oldShell.indexOf(CommConstants.getDownJobName(jenkinsInfo.getPathShell(), downStageName)) >= 0) {
                                jobinfo.getBuilders().get(0).setCommand(CommConstants.getDownJobName(jenkinsInfo.getPathShell(), downStageName) + shell);
                            } else {
                                jobinfo.getBuilders().get(0).setCommand(shell);
                            }
                        }
                    }
                    if (i > 1) {        //关联上级job
                    } else {
                        if (StringUtils.isNotEmpty(spec)) {
                            if (jobinfo.getTriggers() != null && jobinfo.getTriggers().size() > 0 && jobinfo.getTriggers().get(0) instanceof SCMTrigger) {
                                jobinfo.getTriggers().get(0).setSpec(spec);
                            } else {
                                SCMTrigger scmTrigger = new SCMTrigger();
                                scmTrigger.setSpec(spec);
                                scmTrigger.setIgnorePostCommitHooks(false);
                                jobinfo.addTrigger(scmTrigger);
                            }
                        } else {
                            if (jobinfo.getTriggers() != null) {
                                List<Trigger> triggers = new ArrayList<Trigger>();
                                jobinfo.setTriggers(triggers);
                            }
                        }
                    }
                    xml = JobConfigUtil.toXMLString(jobinfo);
                    job = new JobImpl(jobName);
                    System.out.print(xml);
                    xml = "<?xml version='1.0' encoding='UTF-8'?>" + xml;
                    client.updateJob(job, xml);
                    client.close();
                    hmap.put("stageconfig", xml);
                }
                oldJobName = jobName;
                scriptjson.put("script", shell);
                if (("1").equals(stagecode)) {
                    branchjson.put("git clone", downpath);
                    dowpathjson.put("branch", branch);
                    scriptjson.putAll(branchjson);
                    scriptjson.putAll(dowpathjson);
                }
                if (scriptjson != null) {
                    shell = scriptjson.toString();
                }
                namejson.put(jobName, shell);
            }
        }
        return namejson.toString();
    }


    public boolean ManualDeploy(long projectId, String tag) throws Exception {
        String jobName = "";
        AdBranch adBranch;
        AdJenkinsInfo jkInfo = null;
        boolean seccessFlag = false;
        AdProject adProject;
        String codeStore = "";
        List<AdBranch> adBranchList = branchDAO.qryBranchByProject(projectId, -1);
        if (adBranchList != null && adBranchList.size() > 0) {
            adBranch = adBranchList.get(0);
            if (adBranch != null) {
                jkInfo = jkDAO.qryByJkId(adBranch.getAdJenkinsInfo().getJenkinsId());
            } else {
                throw new Exception("获取分支信息失败");
            }
            List<AdStage> adStageList = adStageDAO.QryValidAdOperationByEnvId(adBranch.getBranchId());
            if (adStageList != null) {
                jobName = adStageList.get(0).getJenkinsJobName();
            } else {
                throw new Exception("获取jobname失败");
            }
        } else {
            throw new Exception("获取分支信息失败");
        }
        adProject = adProjectDAO.getSystemById(projectId);
        if (adProject != null) {
            codeStore = adProject.getCodeStore();
        } else {
            throw new Exception("获取项目仓库失败");
        }
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(codeStore)) {
            codeStore = codeStore.substring(codeStore.lastIndexOf("/") + 1, codeStore.lastIndexOf("."));
        } else {
            throw new Exception("获取项目仓库失败");
        }
        if (jkInfo != null && StringUtils.isNotEmpty(jobName)) {
            JobData jd = new JobData();
            jd.setServer("http://" + jkInfo.getJenkinsUrl());
            jd.setPort("" + jkInfo.getServerPort());
            jd.setUsername(jkInfo.getJenkinsUsername());
            jd.setPassword(jkInfo.getJenkinsPassword());
            List<NameValuePair> parameters = jd.getParameters();
            parameters.add(new BasicNameValuePair("tag", tag));
            jd.setJob(jobName);
            JenkinsPoster jp = new JenkinsPoster(jd);
            seccessFlag = jp.postJenkinsJob();
            if (seccessFlag) {
                AdDockImages adDockImages;
                List<AdDockImages> adDockImagesList = adDockImagesDAO.getDockImagsByTag(tag);
                if (adDockImagesList != null && adDockImagesList.size() > 0) {
                    adDockImages = adDockImagesList.get(0);
                    adDockImages.setHasImage(0);
                    adDockImages.setImageStatus(1);
                } else {
                    adDockImages = new AdDockImages();
                    adDockImages.setTag(tag);
                    adDockImages.setImageStatus(1);
                    adDockImages.setHasImage(0);
                    adDockImages.setProjectName(codeStore + "");
                }
                adDockImagesDAO.updateDockImagsByTag(adDockImages);
            }
        } else {
            throw new Exception("获取jenkins信息失败");
        }
        return seccessFlag;
    }

    public CommonPojo StopPipeline(long envId, int buildType) {
        CommonPojo commonPojo = new CommonPojo();
        try {
            System.out.print("jdisajdajsdiasjdpajsdasd");
            AdPipeLineState adPipeLineState = adPipeLineStateDAO.qryByBranch(envId).get(0);
            if (envId != 0 && (adPipeLineState.getStopState() == null || adPipeLineState.getStopState() == 2)) {
                AdStage adStage = null;
                AdBranch env = branchDAO.getEnvById(envId);
                AdPipeLineState envState = stateDAO.qryByEnvIdBuildType(envId, buildType);
                if (env != null) {
                    log.error("******************************环境编号***********************" + env.getBranchId());
                    if (envState != null) {
                        envState.setBranchState(1);
                        envState.save();
                    }
                    AdJenkinsInfo jkInfo = jkDAO.qryByJkId(env.getAdJenkinsInfo().getJenkinsId());
                    if (jkInfo == null) {
                        throw new Exception("获取jenkins登陆信息失败");
                    }
                    String jkIp = jkInfo.getJenkinsUrl();
                    String jkUserName = jkInfo.getJenkinsUsername();
                    String jkPassword = jkInfo.getJenkinsPassword();
                    int serverPort = jkInfo.getServerPort();
                    String jkUrl = "http://" + jkIp + ":" + serverPort/*serverPort*/;
                    int[] result = {0, 1};
                    Thread urunable = new Thread(new stopStage(envId, result, jkUserName, jkPassword, jkUrl, adPipeLineState));
                    urunable.start();

                }
            }
        } catch (Exception e) {
            log.error("", e);
            e.printStackTrace();
            commonPojo.setRetCode("500");
            commonPojo.setRetMessage(e.getMessage().trim());
        } finally {
            return commonPojo;
        }
    }

    public boolean checkTrigger(long branchId, long triggerBranch) throws Exception {
        AdBranchList branchList = adBranchDAO.qryAllTriggerBranch(triggerBranch);
        return checkTriggerCycle(branchId, branchList);

    }

    public boolean checkTriggerCycle(long branchId, AdBranchList branchList) throws Exception {
        if (branchList == null) {
            throw new Exception("流转分支不存在");
        } else if (branchList.getTriggerBranch() == null || branchList.getTriggerBranch().getBranchId()==-1 || branchList.getTriggerBranch().getState() == 0 || branchList.getTriggerBranch().getBranchId() < 1 || branchList.getTriggerBranch().getBranchType() > branchList.getBranchType()) {
            return branchList.getBranchId() != branchId;
        } else {
            return checkTriggerCycle(branchId, branchList.getTriggerBranch());
        }
    }

    /***
     * @param req 传递的信息
     * @throws Exception
     */
    public void updateReformSystemDeploy(SysReformProjectPojoExt req) throws Exception {
        List detail;
        String shell = "";
        Map map = null;
        List detailList;
        String type = "";
        Map<String, String> deailMap;
        int stagenum = 0;
        long branchId;
        long pipelineId;
        String branchDesc = "";
        String buildCron = "";
        SysReformBranchPojoExt[] sysBranchPojoExts;
        String originPath = "";
        String buildFileType = "";
        String spec = "";
        String triggerBranch = "";
        AdVirtualEnvironment adVirtualEnvironment;
        AdProject adProject = null;
        if (null != req) {
            adProject = adProjectDAO.qryById(Long.parseLong(req.getProjectId()));
        }
        String projectName = "";
        if (null != adProject) {
            projectName = adProject.getProjectName();
        }
        String wvsScript = bsParaDetailDAO.qryByParams("X", "WVS_SCRIPT", "WVS").getPara2();
        String webScanScript = bsParaDetailDAO.qryByParams("X", "WebScan_SCRIPT", "WebScan").getPara2();
        if (req != null && req.getObj() != null) {
            sysBranchPojoExts = req.getObj();
            if (sysBranchPojoExts != null) {
                for (SysReformBranchPojoExt sysBranchPojoExt : sysBranchPojoExts) {
                    if (sysBranchPojoExt != null) {
                        spec = "";
                        map = new HashMap<>();
                        map.put("pipelineId", sysBranchPojoExt.getPipelineId() + "");
                        map.put("branchId", sysBranchPojoExt.getBranchId() + "");
                        map.put("branchDesc", sysBranchPojoExt.getBranchDesc() + "");
                        map.put("triggerBranch", sysBranchPojoExt.getTriggerBranch());
                        map.put("envId", sysBranchPojoExt.getEnvId());
                        map.put("buildCron", sysBranchPojoExt.getBuildCron());
                        if (StringUtils.isNotEmpty(sysBranchPojoExt.getBuildFileType())) {
                            map.put("buildFileType", sysBranchPojoExt.getBuildFileType());
                        }
                        if (StringUtils.isNotEmpty(sysBranchPojoExt.getOriginPath())) {
                            map.put("originPath", sysBranchPojoExt.getOriginPath());
                        }
                        type = sysBranchPojoExt.getBranchType();
                        detailList = new ArrayList<Map>();
                        if (sysBranchPojoExt.getStages() != null) {
                            for (SysStagePojoExt stagePojoExt : sysBranchPojoExt.getStages()) {
                                deailMap = new HashMap<String, String>();
                                if (!stagePojoExt.isCheck() && StringUtils.isEmpty(stagePojoExt.getJkJobName())) {
                                    continue;
                                }
                                if (!stagePojoExt.isCheck() && StringUtils.isNotEmpty(stagePojoExt.getJkJobName())) {
                                } else {
                                    stagenum++;
                                }
                                deailMap.put("name", stagePojoExt.getJkJobName());
                                deailMap.put("stagecode", stagePojoExt.getStagecode());
                                if (!("5").equals(sysBranchPojoExt.getBranchType())) {
                                    if (StringUtils.isNotEmpty(sysBranchPojoExt.getEnvId())) {
                                        if (("" + CommConstants.STAGE_CODE.deploy).equals(stagePojoExt.getStagecode())) {
                                            stagePojoExt.setShellCommand(CommConstants.mergeDeployScript(bsParaDetailDAO, "DEPLOY_SCRIPT", sysBranchPojoExt.getEnvId(), virtualEnvironmentDAO, adDcosDeployDtlDAO, sysBranchPojoExt.getOriginPath(), null, null, adGroupImpl, dcosDeployInfoDAO, adProject));
                                        }
                                        if (("" + CommConstants.STAGE_CODE.restart).equals(stagePojoExt.getStagecode())) {
                                            stagePojoExt.setShellCommand(CommConstants.mergeDeployScript(bsParaDetailDAO, "RESTART_SCRIPT", sysBranchPojoExt.getEnvId(), virtualEnvironmentDAO, adDcosDeployDtlDAO, sysBranchPojoExt.getOriginPath(), null, null, adGroupImpl, dcosDeployInfoDAO, adProject));
                                        }
                                    }
                                    if (("" + CommConstants.STAGE_CODE.wvs).equals(stagePojoExt.getStagecode())) {
                                        String stageShellCommend = stagePojoExt.getShellCommand();
                                        if (stageShellCommend.contains("，")) {
                                            stageShellCommend = stageShellCommend.replace("，", ",");
                                        }
                                        if (stageShellCommend.contains(",")) {
                                            String[] wvsShell = new String[15];
                                            StringBuffer sbf = new StringBuffer();
                                            String[] wvsShellList = stagePojoExt.getShellCommand().split(",");
                                            for (int count = 0; count < wvsShellList.length; count++) {
                                                wvsShell[count] = wvsScript + " " + sysBranchPojoExt.getBranchName() + " " + projectName + " " + wvsShellList[count];
                                                sbf.append(wvsShell[count]).append(";");
                                            }
                                            stagePojoExt.setShellCommand(sbf + "");
                                        } else {
                                            stagePojoExt.setShellCommand(wvsScript + " " + sysBranchPojoExt.getBranchName() + " " + projectName + " " + stageShellCommend);
                                        }
                                    }
                                    if (("" + CommConstants.STAGE_CODE.webScan).equals(stagePojoExt.getStagecode())) {
                                        if (stagePojoExt.getShellCommand().contains("，")) {
                                            stagePojoExt.setShellCommand(stagePojoExt.getShellCommand().replace("，", ","));
                                        }
                                        stagePojoExt.setShellCommand(webScanScript + stagePojoExt.getShellCommand().trim() + "@3");
                                    }
                                }
                                deailMap.put("script", stagePojoExt.getShellCommand());
                                deailMap.put("branch", sysBranchPojoExt.getBranchPath());
                                deailMap.put("ischoosestage", stagePojoExt.isCheck() + "");
                                deailMap.put("downpath", sysBranchPojoExt.getBranchPath());
                                if (stagePojoExt.isCheck() && StringUtils.isNotEmpty(stagePojoExt.getSpec())) {
                                    if ( !"00000".equals(stagePojoExt.getSpec()) && ("自动化测试".equals(stagePojoExt.getStageConfig()))) {
                                        String specIsValid = "0 " + stagePojoExt.getSpec().trim();
                                        int spaceCount = 0;
                                        for (int i = 0; i < specIsValid.length(); i++) {
                                            char tem = specIsValid.charAt(i);
                                            if (tem == ' ') // 空格
                                                spaceCount++;
                                        }
                                        if (spaceCount != 5) {
                                            throw new Exception("自动化测试时间不规范，请输入正确的表达式，如“5 8 * * *”");
                                        }
                                        String middle = specIsValid.substring(0, specIsValid.length() - 1) + '?';
                                        boolean cronExpressionFlag = CronExpression.isValidExpression(middle);
                                        if (!cronExpressionFlag) {
                                            throw new Exception("自动化测试时间不规范，请输入正确的表达式，如“5 8 * * *”");
                                        } else {
                                            spec = stagePojoExt.getSpec().trim();
                                        }
                                    } else {
                                        spec = stagePojoExt.getSpec().trim();
                                    }
                                } else {
                                    spec = "";
                                }

                                deailMap.put("spec", spec);
                                detailList.add(deailMap);
                            }
                        }
                        map.put("stageinfo", detailList);
                    }
                    if (map != null) {
                        if (map.get("pipelineId") != null && StringUtils.isNotEmpty((String) map.get("pipelineId"))) {
                            pipelineId = Long.valueOf((String) map.get("pipelineId"));
                        } else {
                            throw new Exception("pipelineId不正确");
                        }
                        if (map.get("branchId") != null && StringUtils.isNotEmpty((String) map.get("branchId"))) {
                            branchId = Long.valueOf((String) map.get("branchId"));
                        } else {
                            throw new Exception("branchId不正确");
                        }
                        triggerBranch = (String) map.get("triggerBranch");
                        Long triggerBranchId = null;
                        if (org.apache.commons.lang3.StringUtils.isNotEmpty(triggerBranch)) {
                            triggerBranchId = Long.parseLong(triggerBranch);
                            if (triggerBranchId > 0) {
                                if (!checkTrigger(branchId, triggerBranchId)) {
                                    throw new Exception("错误：流水流转将产生死锁！");
                                }
                            }
                        }
                        if (map.get("stageinfo") != null) {
                            detail = (ArrayList) map.get("stageinfo");
                        } else {
                            throw new Exception("stageinfo不正确");
                        }
                        if (detail == null) {
                            throw new Exception("dstageinfo不正确");
                        }
                        if (map.get("originPath") != null && StringUtils.isNotEmpty((String) map.get("originPath"))) {
                            originPath = (String) map.get("originPath");
                        }
                        if (map.get("buildFileType") != null && StringUtils.isNotEmpty((String) map.get("buildFileType"))) {
                            buildFileType = (String) map.get("buildFileType");
                        }
                        if (map.get("buildCron") != null && StringUtils.isNotEmpty((String) map.get("buildCron"))) {
                            buildCron = (String) map.get("buildCron");
                        }
                        AdPipeLineState adPipeLineState = stateDAO.qryEnvById(branchId);
                        if (adPipeLineState.getBranchState() == 2) {
                            throw new Exception("该环境正在构建中不能修改配置");
                        }
                        AdBranch adBranch = branchDAO.qryAdBranchByname(branchId, sysBranchPojoExt.getBranchDesc(), adPipeLineState.getAdProject().getProjectId());
                        if (adBranch != null) {
                            throw new Exception("该分支描述已存在");
                        }
                        shell = UpdateJkJob(detail, branchId);
                        AdBranchShell adBranchShell;
                        adBranchShell = adBranchRelatDAO.qryBranchShell(branchId, pipelineId);
                        saveBranchShell(pipelineId, branchId, shell, adBranchShell);
                        branchDesc = sysBranchPojoExt.getBranchDesc();
                        AdBranch adBranch1 = new AdBranch();
                        adBranch1.setBranchId(branchId);
                        adBranch1.setBranchDesc(branchDesc);
                        adBranch1.setOriginPath(originPath);
                        adBranch1.setBuildFileType(buildFileType);
                        adBranch1.setBuildCron(buildCron);
                        adStageDAO.updateAdStage(pipelineId, detail, triggerBranchId, adBranch1, sysBranchPojoExt.getEnvId());
                    }
                }
            }
        }
    }

    /****************************************
     * 改造方法
     ********************************************/

    public String qryCompileScript(String compileTool, String compileVersion, String buildTool) {
        String buildShell = "";
        if (StringUtils.isNotEmpty(compileTool) && StringUtils.isNotEmpty(compileVersion)) {                        //判断编译工具和版本是否存在
            List<AdParaDetail> compileScript = bsParaDetailDAO.qryListByDetails("X", "COMPILE_SCRIPT", compileVersion, compileTool);//？？？？
            if (CollectionUtils.isNotEmpty(compileScript)) {
                buildShell = compileScript.get(0).getPara2();
            }
        }
        if (StringUtils.isNotEmpty(buildTool)) {
            if (buildTool.split(" ").length > 1) {
                List<AdParaDetail> compileScript = bsParaDetailDAO.qryListByDetails("X", "COMPILE_SCRIPT", buildTool.split(" ")[1], buildTool.split(" ")[0]);
                if (CollectionUtils.isNotEmpty(compileScript)) {
                    StringBuffer buf = new StringBuffer();
                    buf.append(buildShell);
                    buf.append("\r\n");
                    buf.append(compileScript.get(0).getPara2());
                    buildShell = buf.toString();
                }
            }
        }
        return buildShell + "\r\n";
    }

    class stopStage implements Runnable {

        long envId;
        int[] result;
        String jkUserName;
        String jkPassword;
        String jkUrl;
        AdPipeLineState adPipeLineState;

        public stopStage(long envId, int[] result, String jkUserName, String jkPassword, String jkUrl, AdPipeLineState adPipeLineState) {
            this.envId = envId;
            this.result = result;
            this.jkPassword = jkPassword;
            this.jkUrl = jkUrl;
            this.jkUserName = jkUserName;
            this.adPipeLineState = adPipeLineState;
        }

        @Override
        public void run() {
            int falg = 5;
            int a = 0;
            adPipeLineState.setStopState(1L);
            adPipeLineState.save();
            while (falg >= 0) {
                List<AdStage> adStageList = adStageDAO.QryBuildingStage(envId, result);
                int count = 0;
                log.error("falg==！!!!!!!!!!!!!!!！" + falg);
                for (AdStage adStage : adStageList) {
                    count++;
                    System.out.println("count===" + count);
                    if (adStage != null) {
                        try {
                            a = 1;
                            Job job = new JobImpl(adStage.getJenkinsJobName());
                            Injector injector = null;
                            injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                                jkUrl), jkUserName, jkPassword));
                            JenkinsClient client = injector.getInstance(JenkinsClient.class);
                            long buildnum = Long.valueOf(client.getCurBuildNum(job));

                            System.out.println("buildnum===" + buildnum + "====" + adStage.getJenkinsJobName());
                            if (buildnum > 0) {
                                client.stopJob(adStage.getJenkinsJobName(), buildnum + 1);
                                client.close();
                            }
                        } catch (Exception e2) {
                            a = 0;
                            log.error(e2.getLocalizedMessage());
                            log.error("找不到正在跑的任务！！falg===" + falg);
                        }

                    } else {
                        log.error("找不到正在跑的job！!!!!!!!!!!!!!!！falg=" + falg);
                    }
                    if (count == 2 || a == 1) {
                        break;
                    }
                }
                if (a == 1) {
                    break;
                }
                falg--;
            }
            adPipeLineState.setStopState(2L);
            adPipeLineState.save();
            adStageDAO.updateDealResult(envId);
        }
    }
}



