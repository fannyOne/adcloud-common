package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.auth.sso.gitlib.api.GitlabAPI;
import com.asiainfo.auth.sso.gitlib.api.models.GitlabCommit;
import com.asiainfo.comm.common.enums.Authorization;
import com.asiainfo.comm.common.pojo.pojoExt.*;
import com.asiainfo.comm.common.pojo.pojoMaster.OperationNowPojo;
import com.asiainfo.util.CommConstants;
import com.asiainfo.util.DateConvertUtils;
import com.asiainfo.util.HttpUtil;
import com.asiainfo.comm.externalservice.jenkins.client.JenkinsClient;
import com.asiainfo.comm.externalservice.jenkins.guice.JenkinsWsClientGuiceModule;
import com.asiainfo.comm.externalservice.jenkins.jobs.Job;
import com.asiainfo.comm.externalservice.jenkins.jobs.JobImpl;
import com.asiainfo.comm.module.autoTest.dao.impl.AdAutoTestLogDAO;
import com.asiainfo.comm.module.autoTest.service.impl.AdSeqTestRelateImpl;
import com.asiainfo.comm.module.build.dao.impl.*;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.role.dao.impl.AdUserRoleRelDAO;
import com.asiainfo.comm.module.role.service.impl.AdProjectImpl;
import com.asiainfo.comm.module.role.service.impl.RightManagerImpl;
import com.asiainfo.comm.module.role.service.impl.VerifyRightImpl;
import com.asiainfo.util.JerseyClient;
import com.asiainfo.comm.module.build.controller.WebScanController;
import com.asiainfo.comm.module.common.AdParaDetailDAO;
import com.asiainfo.comm.module.common.AdStaticDataDAO;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import com.avaje.ebean.SqlRow;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yangry on 2016/6/15 0015.
 */
@Component
@lombok.extern.slf4j.Slf4j
public class AdOperationImpl {
    @Autowired
    AdProjectImpl adProjectImpl;
    @Autowired
    AdBranchDAO envDAO;
    @Autowired
    AdBranchImpl adBranchImpl;
    @Autowired
    AdStageImpl adStageImpl;
    @Autowired
    AdStageLogDtlImpl adStageLogDtlImpl;
    @Autowired
    AdBuildLogImpl adBuildLogImpl;
    @Autowired
    AdStageDAO optDAO;
    @Autowired
    AdPipeLineStateDAO stateDAO;
    @Autowired
    AdStaticDataImpl bsStaticDataImpl;
    @Autowired
    AdStageDAO adStageDAO;
    @Autowired
    AdStageLogDtlDAO adStageLogDtlDAO;
    @Autowired
    AdBranchDAO branchDAO;
    @Autowired
    AdStageDAO adstageDAO;
    @Autowired
    AdBuildLogDAO adBuildLogDAO;
    @Autowired
    AdPipeLineStateDAO adPipeLineStateDAO;
    @Autowired
    AdUserRoleRelDAO userRoleRelDAO;
    @Autowired
    AdParaDetailDAO paraDetailDAO;
    @Autowired
    AdProjectDeployPackageDAO adProjectDeployPackageDAO;
    @Autowired
    AdBuildLogDAO buildLogDAO;
    @Autowired
    AdStaticDataDAO bsStaticDataDAO;
    @Autowired
    AdAutoTestLogDAO adAutoTestLogDAO;
    @Autowired
    RightManagerImpl rightManagerImpl;
    @Autowired
    VerifyRightImpl verifyRightImpl;
    @Value("${gitlab.server.url}")
    String gitUrl;
    @Autowired
    AdVirtualEnvironmentDAO adVirtualEnvironmentDAO;
    @Autowired
    AdSeqTestRelateImpl adSeqTestRelateImpl;
    @Autowired
    AdTreeDataDAO adTreeDataDAO;
    @Autowired
    JenkinsImpl jenkinsImpl;
    @Value("${gitlab.admin.token}")
    private String token;
    private Map<String, String> dealRestul;

    public Map<String, String> getdealRestul() {
        if (null == dealRestul) {
            dealRestul = bsStaticDataImpl.qryStaticDatas("BUILDER_TYPE");
            log.error("查询BUILDER_TYPE");
        }
        return dealRestul;
    }

    public List<SqlRow> qryBranchByProjectId(long projectId, long buildType) {
        return branchDAO.qryBranchByProecIdBranchType(projectId, buildType);
    }

    public List<SqlRow> qryBranchByBranchType(long projectId, String branchType) {
        return branchDAO.qryBranchByBranchType(projectId, branchType);
    }

    public OperationNowPojo qryPips(long system_id) {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest();
        HttpSession httpSession = httpServletRequest.getSession();
        String username = (String) httpSession.getAttribute("username");
        log.error("this is the opts server for " + username);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<AdStaticData> staticDatas = bsStaticDataImpl.qryByCodeType("BUILDER_TYPE");
        Map<String, String> optMp = new HashMap<String, String>();
        Map<String, String> dealMap = new HashMap<String, String>();
        if (staticDatas != null) {
            for (AdStaticData adStaticData : staticDatas) {
                System.out.println(adStaticData.getCodeName());
                optMp.put(adStaticData.getCodeValue(), adStaticData.getCodeName());
            }
        }
        dealMap.put("0", "wait");
        dealMap.put("1", "run");
        dealMap.put("2", "success");
        dealMap.put("3", "fail");
        OperationNowPojo pojo = new OperationNowPojo();
        try {
            AdProject project = adProjectImpl.qryProject(system_id);
            /**
             * 临时的各个中心权限管控代码段
             */
            //获取权限
            List<Authorization> rights = Lists.newArrayList();
            if (null != project && null != project.getAdGroup()) {
                rights = rightManagerImpl.qryRight(username, project.getAdGroup().getGroupId());
            }

            /**
             * 临时的各个中心权限管控代码段Over
             */
            Map<String, StageinfoExtPojo> stageMap;
            List<PipelineExtPojo> pips = new ArrayList<>();

            if (project != null) {
                List<AdBranch> envs = envDAO.getEnvsBySysId(system_id);
                if (envs != null && envs.size() > 0) {
                    stageMap = qryStageInfo(system_id);
                    for (AdBranch env : envs) {

                        PipelineExtPojo pip = new PipelineExtPojo();

                        pip.setCanOperation(verifyRightImpl.verifyBranchRight(env, rights));

                        AdPipeLineState state = stateDAO.qryEnvById(env.getBranchId());
                        List<AdStage> opts = optDAO.QryAdOperationByEnvIdType(env.getBranchId());
                        List<JobExtPojo> jobs = new ArrayList<>();
                        if (state.getLastBuildDate() != null) {
                            pip.setDate(sdf.format(state.getLastBuildDate()));
                        }
                        pip.setName(env.getBranchName());
                        pip.setBranchDesc(env.getBranchDesc());//流水线中的详情
                        pip.setDate(env.getDoneDateString());
                        pip.setBuildType(env.getBranchType());
                        pip.setSeqId(adStageLogDtlDAO.getSeqByBranchId(env.getBranchId()));
                        for (int i = 0, j = opts.size(); i < j; i++) {
                            AdStage opt = opts.get(i);
                            JobExtPojo job = new JobExtPojo();
                            job.setName(optMp.get(opt.getStageCode() + ""));
                            job.setState(dealMap.get(opt.getDealResult() + ""));
                            job.setStageId(opt.getStageId() + "");
                            jobs.add(job);
                            if (opt.getStageCode() == 1 && (opt.getPreCommitId() == null || opt.getCommitId().equals(opt.getPreCommitId()))) {
                                pip.setCanRollBack(false);
                            }
                        }
                        pip.setJob(jobs);
                        pip.setBranchId(env.getBranchId());
                        pip.setStageInfo(stageMap.get("" + env.getBranchId()));
                        pips.add(pip);
                    }
                    if (pips != null) {
                        Collections.reverse(pips);
                    }
                    pojo.setName(project.getProjectName());
                    pojo.setProjectId(project.getProjectId());
                    pojo.setPipeline(pips);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            pojo = new OperationNowPojo();
            pojo.setRetCode("500");
            pojo.setRetMessage(e.getMessage().replaceAll("\n", "") + e.toString());
        } finally {
            return pojo;
        }
    }

    public OperationNowPojo qryBranchOpts(long branchId) throws Exception {
        OperationNowPojo pojo = new OperationNowPojo();
        AdBranch adbranch = adBranchImpl.qryById(branchId);
        if (null == adbranch) {
            throw new Exception("根据流水线编号：" + branchId + "没有查到流水信息");
        }
        AdProject project = adProjectImpl.qryProject(adbranch.getAdProject().getProjectId());
        if (project == null) {
            throw new Exception("根据项目编号：" + adbranch.getAdProject().getProjectId() + "没有查到项目信息");
        }
        pojo.setName(project.getProjectName());
        pojo.setProjectId(project.getProjectId());
        List<PipelineExtPojo> pips = new ArrayList<>();
        pips.add(qryBranchOpt(adbranch));
        pojo.setPipeline(pips);
        pojo.setSystemTime(new Date());
        return pojo;
    }

    public OperationNowPojo qryProjectOpts(long projectid) throws Exception {
        OperationNowPojo pojo = new OperationNowPojo();
        AdProject project = adProjectImpl.qryProject(projectid);
        if (project == null) {
            throw new Exception("根据项目编号：" + projectid + "没有查到项目信息");
        }
        pojo.setName(project.getProjectName());
        pojo.setProjectId(project.getProjectId());

        List<AdBranch> adbranchs = adBranchImpl.qryBranchByProject(projectid);
        List<PipelineExtPojo> pips = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(adbranchs)) {
            for (AdBranch adbranch : adbranchs) {
                try {
                    pips.add(qryBranchOpt(adbranch));
                } catch (Exception e) {
                    log.error("查询" + adbranch.getBranchId() + "流水失败", e);
                }
            }
        }
        //排序
        Collections.sort(pips);
        pojo.setPipeline(pips);
        pojo.setSystemTime(new Date());
        return pojo;
    }

    private PipelineExtPojo qryBranchOpt(AdBranch adbranch) {
        PipelineExtPojo pip = new PipelineExtPojo();
        pip.setBranchId(adbranch.getBranchId());
        pip.setName(adbranch.getBranchName());
        pip.setBranchDesc(adbranch.getBranchDesc());
        pip.setDate(adbranch.getDoneDateString());
        pip.setBuildType(adbranch.getBranchType());
        pip.setBranchType(adbranch.getBranchType());
        AdPipeLineState state = adPipeLineStateDAO.qryEnvById(adbranch.getBranchId());
        switch (state.getBranchState()) {
            case 2:
                pip.setState("running");
                break;
            default:
                pip.setState("static");
        }
        //添加环节信息
        List<JobExtPojo> jobs = new ArrayList<>();
        List<AdStage> opts = adStageImpl.qryAdStage(adbranch.getBranchId());
        if (CollectionUtils.isNotEmpty(opts)) {
            List<BuildStageInfoDtlExtPojo> infos = null;
            try {
                infos = qryPipeInfo(opts, adbranch);
            } catch (Exception e) {
                log.error("查询" + adbranch.getBranchId() + "流水日志信息失败", e);
            }
            for (AdStage opt : opts) {
                JobExtPojo job = new JobExtPojo();
                job.setName(converterStageCode(opt));
                job.setState(converterDealResult(opt));
                job.setStageId(String.valueOf(opt.getStageId()));
                job.setStageCode(opt.getStageCode());
                //添加运行中环节信息
                if (1 == opt.getDealResult()) {
                    AdStageLogDtl adStageLogDtl = adStageLogDtlImpl.qryLastAdstage(opt.getBuildSeq(), opt.getStageId());
                    if (null != adStageLogDtl) {
                        job.setStartTime(adStageLogDtl.getBeginDate());
                        job.setAverageTime(adStageLogDtlImpl.qryAvgTime(adbranch.getBranchId(), opt.getStageId()));
                        job.setProcess(calculProgress(job.getStartTime(), job.getAverageTime()));
                    }
                }
                addJobExt(job, infos);
                jobs.add(job);
            }
        }
        pip.setJob(jobs);
        //添加流水构建信息
        AdBuildLog adbuildlog = adBuildLogImpl.qryLast(adbranch.getBranchId());
        if (null != adbuildlog) {
            pip.setSeqId(adbuildlog.getTotalStep());
            if (0 == adbuildlog.getBuildResult() || 1 == adbuildlog.getBuildResult()) {//运行状态
                pip.setStartTime(adbuildlog.getCreateDate());
                pip.setAverageTime(adBuildLogImpl.qryAvgTimeByBranchId(adbranch.getBranchId()));
                pip.setProcess(calculProgress(pip.getStartTime(), pip.getAverageTime()));
            }
        }
        pip.setCanRollBack(rollBackFlag(opts));
        return pip;
    }

    public boolean rollBackFlag(List<AdStage> opts) {
        if (CollectionUtils.isNotEmpty(opts)) {
            for (AdStage opt : opts) {
                if (opt.getStageCode() == 1) {
                    if (opt.getPreCommitId() == null ||
                        opt.getCommitId().equals(opt.getPreCommitId())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    String converterStageCode(AdStage stage) {
        String stageName = "";
        if (null != stage.getStageCode()) {
            stageName = MapUtils.getString(getdealRestul(), String.valueOf(stage.getStageCode()), stageName);
        }
        return stageName;
    }

    String converterDealResult(AdStage stage) {
        String dealResult = "";
        if (null != stage.getDealResult()) {
            dealResult = MapUtils.getString(CommConstants.STAGE_EALRESULT.dealMap, String.valueOf(stage.getDealResult()), dealResult);
        }
        return dealResult;
    }

    void addJobExt(JobExtPojo job, List<BuildStageInfoDtlExtPojo> infos) {
        if (CollectionUtils.isNotEmpty(infos)) {
            for (BuildStageInfoDtlExtPojo info : infos) {
                if (String.valueOf(info.getStageId()).equals(job.getStageId())) {
                    job.setBeginTime(info.getBeginTime());
                    job.setDurationTime(info.getDurationTime());
                    if (job.getStageCode() == CommConstants.STAGE_CODE.deploy || job.getStageCode() == CommConstants.STAGE_CODE.wvs) {
                        job.setPackages(info.getPackages());
                    }
                    List<JobDtlPojo> jobDtls = Lists.newArrayList();
                    if (CommConstants.STAGE_INFO.stageInfo.get(job.getStageCode() + "") != null) {
                        JobDtlPojo jobDtl = new JobDtlPojo();
                        if (StringUtils.isNotEmpty(info.getFailRate())) {
                            jobDtl.setKey("单测成功率");
                            jobDtl.setValue(info.getFailRate());
                            jobDtls.add(jobDtl);
                            jobDtl = new JobDtlPojo();
                        }
                        jobDtl.setKey(CommConstants.STAGE_INFO.stageInfo.get(job.getStageCode() + ""));
                        jobDtl.setValue(info.getFileNum());
                        jobDtls.add(jobDtl);
                    }
                    if (CollectionUtils.isNotEmpty(info.getKeyValues())) {
                        for (StageKeyValue keyValue : info.getKeyValues()) {
                            JobDtlPojo jobDtl = new JobDtlPojo();
                            jobDtl.setKey(keyValue.getKey());
                            jobDtl.setValue(keyValue.getValue());
                            jobDtls.add(jobDtl);
                        }
                    }
                    job.setJobDtls(jobDtls);
                    break;
                }
            }
        }
    }

    public boolean dealWebScan(AdStage stage, AdStageLogDtl logDtl, String sessionId, SimpleDateFormat sdf) throws SQLException {
        String failLog = logDtl.getFailLog();
        int startNum = failLog.lastIndexOf("scanCode:") + 9;
        String taskId = failLog.substring(startNum, startNum + 13);
        String result = new WebScanController().getResult(sessionId, taskId) + "\nupdate in " + sdf.format(new Date());
        String scanState = JerseyClient.getState(taskId);
        logDtl.setFailLog(failLog.substring(0, failLog.lastIndexOf("-------------------\n") + 20) + result);
        if (scanState.equals("success") || scanState.equals("failed")) {
            int dealResult = scanState.equals("success") ? 2 : 3;
            adStageDAO.updateState(stage, dealResult);
            logDtl.setStageResult(dealResult + "");
            logDtl.setFinishDate(new Date());
            adStageLogDtlDAO.insertStageLogDtl(logDtl);
            AdPipeLineState pipeLineState = stage.getAdPipeLineState();
            if (pipeLineState != null) {
                pipeLineState.setLastBuildResult(dealResult);
                pipeLineState.setBuildSeqId(stage.getBuildSeq());
                pipeLineState.setBranchState(1);
                pipeLineState.setLastBuildDate(new java.util.Date());
                adPipeLineStateDAO.updatePipeLineState(pipeLineState);
            }
            AdBuildLog adBuildLog = buildLogDAO.qryBuildLog(Math.toIntExact(stage.getBuildSeq()), stage.getAdBranch().getBranchId());
            if (adBuildLog != null) {
                adBuildLog.setBuildDate(new Date());
                adBuildLog.setBuildResult(dealResult);
                adBuildLog.setLastStep(stage.getStep());
                adBuildLog.setLastStageId(stage.getStageId());
                buildLogDAO.insertBuildLog(adBuildLog);
            }
            return false;
        } else {
            adStageLogDtlDAO.insertStageLogDtl(logDtl);
            return true;
        }
    }

    void enhanceResult(String result, BuildStageInfoDtlExtPojo buildStageInfoDtlExtPojo) {
        String[] resultList = result.split("\n");
        List<StageKeyValue> keyValues = new ArrayList<>();
        for (String str : resultList) {
            String[] strList = str.split(":");
            if (strList.length < 2) {
                continue;
            }
            StageKeyValue keyValue = new StageKeyValue();
            keyValue.setKey(strList[0]);
            keyValue.setValue(strList[1]);
            keyValues.add(keyValue);
            break;
        }
        buildStageInfoDtlExtPojo.setKeyValues(keyValues);
    }

    public long calculProgress(Date beging, long average) {
        if (0 == average) return 50;
        Date systemDate = new Date();
        if (null == beging || systemDate.before(beging)) return 1;
        //已用（毫秒）*1000/平均时间*100（恢复百分比）
        long progress = (systemDate.getTime() - beging.getTime()) / 10 / average;
        return progress <= 100 ? progress : 99;
    }

    public Map<String, String> qryStageLogDtl(long branchId, long stageId, long startSize, long jenkinsnum) {
        Long buildSeq = 0L;
        Map<String, String> hmap = new HashMap<String, String>();
        Map<String, String> builderType = bsStaticDataDAO.qryStaticDatas("BUILDER_TYPE");
        AdStage adStage = adstageDAO.qryById(stageId);
        if (adStage.getBuildSeq() != null) {
            buildSeq = adStage.getBuildSeq();
        }
        String[] ret;
        String log = "";
        int contentLength = 0;
        if (buildSeq != 0) {
            AdStageLogDtl adStageLogDtl = adStageLogDtlDAO.qryStageLogByTotalstep(buildSeq, branchId, stageId);
            if (adStageLogDtl != null) {
                String result = adStageLogDtl.getStageResult();
                hmap.put("state", "2".equals(result) ? "success" :
                    ("3".equals(result) ? "failed" : "running"));
                hmap.put("stageId", "" + adStageLogDtl.getAdStage().getStageId());
                hmap.put("stageName", "" + builderType.get("" + adStage.getStageCode()));
                if (startSize == -1) {
                    hmap.put("log", adStageLogDtl.getFailLog().replaceFirst("Started", "<p>Started").replace("\n", "</p><p>").replaceAll("<p>\\+", "<p class=\"command-color\">").replaceAll("<p>errorJenkins", "<p class=\"error-color\">") + "</p>");
                } else {
                    AdBranch adBranch = branchDAO.getEnvById(branchId);
                    if (adBranch != null) {
                        AdJenkinsInfo jenkinsInfo = adBranch.getAdJenkinsInfo();
                        Injector injector;
                        try {
                            long buildnum = 1;
                            if (jenkinsnum == 0) {
                                injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                                    "http://" + jenkinsInfo.getJenkinsUrl() + ":" + jenkinsInfo.getServerPort()), jenkinsInfo.getJenkinsUsername(), jenkinsInfo.getJenkinsPassword()));
                                JenkinsClient client = injector.getInstance(JenkinsClient.class);
                                Job job = new JobImpl(adStageLogDtl.getAdStage().getJenkinsJobName());
                                buildnum = Long.valueOf(client.getCompletedBuildNum(job));
                            } else {
                                buildnum = jenkinsnum;
                            }
                            ret = HttpUtil.sendPostGetStyleAndReplace("http://" + jenkinsInfo.getJenkinsUrl() + ":" + jenkinsInfo.getServerPort() + "/job/" + adStageLogDtl.getAdStage().getJenkinsJobName() + "/" + (buildnum - 1) + "/logText/progressiveHtml", "start=" + startSize);
                            if (ret.length == 2) {
                                log = ret[0];
                                if (StringUtils.isNotEmpty(ret[1])) {
                                    contentLength = Integer.valueOf(ret[1]);
                                } else {
                                    hmap.put("state", "success");
                                }
                            }
                            if (!log.contains("Finished:") && contentLength != -1) {
                                hmap.put("startSize", "" + contentLength);
                            }
                            hmap.put("jenkinsNum", "" + buildnum);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                    hmap.put("log", log.replaceFirst("Finished: SUCCESS", ""));
                }
            } else {
                if (startSize < 1) {
                    hmap.put("state", "running");
                    hmap.put("startSize", 0 + "");
                }
            }
        }
        return hmap;
    }


    Map<String, StageinfoExtPojo> qryStageInfo(long projectId) throws Exception {
        Map<String, StageinfoExtPojo> hmap = new HashMap<String, StageinfoExtPojo>();
        List<AdStage> adStageList;
        AdProject adProject = adProjectImpl.qryProject(projectId);
        String codeStore = adProject.getCodeStore();
        String gitUrl = "";
        String commitUrl = "";
        if (StringUtils.isNotEmpty(codeStore)) {
            gitUrl = codeStore.replace(":", "/").replace("git@", "http://").replace(".git", "/") + "commit" + "/" + "tree";
            if (codeStore.startsWith("http")) {
                commitUrl = codeStore.replace(".git", "/") + "commit" + "/";
            } else {
                commitUrl = codeStore.replace(":", "/").replace("git@", "http://").replace(".git", "/") + "commit" + "/";
            }
        }
        int buildSeq = 0;
        List<AdBranch> adBranchList = envDAO.getEnvsBySysId(projectId);
        if (adBranchList != null && adBranchList.size() > 0) {
            for (AdBranch adBranch : adBranchList) {
                String commitId = "";
                StageinfoExtPojo stageinfoExtPojo = new StageinfoExtPojo();
                adStageList = adStageDAO.QryValidAdOperationByEnvId(adBranch.getBranchId());
                buildSeq = enhanceStageInfo(adStageList, adProject, gitUrl, commitUrl, buildSeq, adBranch, commitId, stageinfoExtPojo);
                AdBuildLog adBuildLog = adBuildLogDAO.qryBuildLog(buildSeq, adBranch.getBranchId());
                setStageDate(stageinfoExtPojo, adBuildLog);
                hmap.put("" + adBranch.getBranchId(), stageinfoExtPojo);
            }
        }
        return hmap;
    }

    private int enhanceStageInfo(List<AdStage> adStageList, AdProject adProject, String gitUrl, String commitUrl, int buildSeq, AdBranch adBranch, String commitId, StageinfoExtPojo stageinfoExtPojo) {
        List<AdProjectDeployPackage> adProjectDeployPackageList;
        int deployPackageLength;
        boolean include_deploy = false;
        if (adStageList != null) {
            for (AdStage adStage : adStageList) {
                if (StringUtils.isNotEmpty(adStage.getCommitId())) {
                    commitId = adStage.getCommitId();
                    stageinfoExtPojo.setCommitUrl(commitUrl + "/" + adStage.getCommitId() + " ");
                    stageinfoExtPojo.setCommitPerson(adStage.getCommitOperator() + " ");
                    buildSeq = adStage.getBuildSeq().intValue();
                    break;
                }
            }
            for (AdStage adStage : adStageList) {
                if (adStage.getStageCode() == CommConstants.STAGE_CODE.deploy) {
                    include_deploy = true;
                    break;
                }
            }
            if (include_deploy) {
                stageinfoExtPojo.setAppMachine((adBranch.getAppMachineInfo() == null ? "" : adBranch.getAppMachineInfo()) + "");
            }
            stageinfoExtPojo.setBranchPath(adBranch.getBranchPath() == null ? "" : adBranch.getBranchPath());
            stageinfoExtPojo.setGitUrl((gitUrl + (adBranch.getBranchPath() == null ? "" : adBranch.getBranchPath()).replaceAll("\\*", "")));
            if (StringUtils.isNotEmpty(commitId)) {
                String ret[] = qryCommitLog(adProject.getGitProjectid(), commitId);
                if (ret.length > 0)
                    stageinfoExtPojo.setCommitLog(ret[0]);
                if (ret.length > 1)
                    stageinfoExtPojo.setCommitPerson(ret[1]);
            }
            List<DeployPackagesExtPojo> packages = new ArrayList<>();
            if (include_deploy) {//判断是否有部署节点，有的话就显示部署包信息
                if (StringUtils.isNotEmpty(commitId)) {
                    adProjectDeployPackageList = adProjectDeployPackageDAO.qryByCommitAndBranch(commitId, adBranch.getBranchId());
                    System.out.println("commitId===" + commitId + "adBranch.getBranchId()" + adBranch.getBranchId());
                    if (adProjectDeployPackageList != null && adProjectDeployPackageList.size() > 0) {
                        for (AdProjectDeployPackage adPackage : adProjectDeployPackageList) {
                            DeployPackagesExtPojo deployPackagesExtPojo = new DeployPackagesExtPojo();
                            deployPackagesExtPojo.setPackageUrl(adPackage.getPackagePath().split("http://")[1]);
                            deployPackageLength = adPackage.getPackagePath().split("/").length;
                            deployPackagesExtPojo.setPackageName(adPackage.getPackagePath().split("/")[deployPackageLength - 1]);
                            packages.add(deployPackagesExtPojo);
                        }
                        stageinfoExtPojo.setPackages(packages);
                    } else {
                        System.out.println("没有下载包！！！！！！！！！！！");
                        DeployPackagesExtPojo deployPackagesExtPojo = new DeployPackagesExtPojo();
                        deployPackagesExtPojo.setPackageName("");
                        deployPackagesExtPojo.setPackageUrl("");
                        packages.add(deployPackagesExtPojo);
                        stageinfoExtPojo.setPackages(packages);
                    }
                } else {
                    DeployPackagesExtPojo deployPackagesExtPojo = new DeployPackagesExtPojo();
                    deployPackagesExtPojo.setPackageName("");
                    deployPackagesExtPojo.setPackageUrl("");
                    packages.add(deployPackagesExtPojo);
                    stageinfoExtPojo.setPackages(packages);
                }
            } else {
                DeployPackagesExtPojo deployPackagesExtPojo = new DeployPackagesExtPojo();
                deployPackagesExtPojo.setPackageName("-1");
                deployPackagesExtPojo.setPackageUrl("-1");
                packages.add(deployPackagesExtPojo);
                stageinfoExtPojo.setPackages(packages);
            }
        }
        return buildSeq;
    }


    public String[] qryCommitLog(String projectId, String commitId) {
        String[] ret = new String[2];
        try {
            GitlabAPI gitlabAPI = new GitlabAPI(gitUrl, token);
            GitlabCommit gitlabCommit = gitlabAPI.getCommit(projectId, commitId);
            if (gitlabCommit != null) {
                ret[0] = gitlabCommit.getTitle();
                ret[1] = gitlabCommit.getAuthorName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public OperationNowPojo qryPipsByBranch(long branchId) {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest();
        HttpSession httpSession = httpServletRequest.getSession();
        String username = (String) httpSession.getAttribute("username");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<AdStaticData> staticDatas = bsStaticDataImpl.qryByCodeType("BUILDER_TYPE");
        Map<String, String> optMp = new HashMap<String, String>();
        Map<String, String> dealMap = new HashMap<String, String>();
        if (staticDatas != null) {
            for (AdStaticData adStaticData : staticDatas) {
                System.out.println(adStaticData.getCodeName());
                optMp.put(adStaticData.getCodeValue(), adStaticData.getCodeName());
            }
        }
        dealMap.put("0", "wait");
        dealMap.put("1", "run");
        dealMap.put("2", "success");
        dealMap.put("3", "fail");
        OperationNowPojo pojo = new OperationNowPojo();
        try {
            AdBranch env = envDAO.getEnvById(branchId);
            AdProject project = null;
            if (env != null) {
                project = env.getAdProject();
            }
            /**
             * 临时的各个中心权限管控代码段
             */
            List<Authorization> rights = Lists.newArrayList();
            if (null != project && null != project.getAdGroup()) {
                rights = rightManagerImpl.qryRight(username, project.getAdGroup().getGroupId());
            }
            /**
             * 临时的各个中心权限管控代码段Over
             */
            List<PipelineExtPojo> pips = new ArrayList<>();
            if (env != null) {
                StageinfoExtPojo stageInfo = qryStageInfoByBranch(env);
                PipelineExtPojo pip = new PipelineExtPojo();
                pip.setCanOperation(verifyRightImpl.verifyBranchRight(env, rights));
                AdPipeLineState state = stateDAO.qryEnvById(env.getBranchId());
                List<AdStage> opts = optDAO.QryAdOperationByEnvIdType(env.getBranchId());
                List<JobExtPojo> jobs = new ArrayList<>();
                if (state.getLastBuildDate() != null) {
                    pip.setDate(sdf.format(state.getLastBuildDate()));
                }
                pip.setName(env.getBranchName());
                pip.setBranchDesc(env.getBranchDesc());//流水线中的详情
                pip.setDate(env.getDoneDateString());
                pip.setBuildType(env.getBranchType());
                pip.setSeqId(adStageLogDtlDAO.getSeqByBranchId(env.getBranchId()));
                if (opts != null && opts.size() > 0) {
                    for (AdStage opt : opts) {
                        JobExtPojo job = new JobExtPojo();
                        job.setName(optMp.get(opt.getStageCode() + ""));
                        job.setState(dealMap.get(opt.getDealResult() + ""));
                        job.setStageId(opt.getStageId() + "");
                        jobs.add(job);
                        if (opt.getStageCode() == 1 && (opt.getPreCommitId() == null || opt.getCommitId().equals(opt.getPreCommitId()))) {
                            pip.setCanRollBack(false);
                        }
                    }
                }
                pip.setJob(jobs);
                pip.setBranchId(env.getBranchId());
                pip.setStageInfo(stageInfo);
                pips.add(pip);
                if (project != null) {
                    pojo.setName(project.getProjectName());
                    if (project.getAdGroup() != null) {
                        pojo.setGroupId(project.getAdGroup().getGroupId());
                    }
                    pojo.setProjectId(project.getProjectId());
                }
            }
            pojo.setPipeline(pips);
        } catch (Exception e) {
            e.printStackTrace();
            pojo = new OperationNowPojo();
            pojo.setRetCode("500");
            pojo.setRetMessage(e.getMessage().trim());
        } finally {
            return pojo;
        }
    }

    StageinfoExtPojo qryStageInfoByBranch(AdBranch adBranch) throws Exception {
        AdProject adProject = adBranch.getAdProject();
        String commitUrl;
        String gitUrl = "";
        List<AdStage> adStageList;
        String commitId = "";
        if (adProject != null) {
            commitUrl = adProject.getCodeStore();
        } else {
            throw new Exception("项目不存在");
        }
        if (StringUtils.isNotEmpty(commitUrl)) {
            if (commitUrl.startsWith("http")) {
                gitUrl = commitUrl.replace(".git", "/") + "commit" + "/";
            } else {
                gitUrl = commitUrl.replace(":", "/").replace("git@", "http://").replace(".git", "/") + "commit" + "/";
            }
        }
        StageinfoExtPojo stageinfoExtPojo;
        int buildSeq = 0;
        commitId = "";
        stageinfoExtPojo = new StageinfoExtPojo();
        adStageList = adStageDAO.QryValidAdOperationByEnvId(adBranch.getBranchId());
        buildSeq = enhanceStageInfo(adStageList, adProject, gitUrl, commitUrl, buildSeq, adBranch, commitId, stageinfoExtPojo);

        AdBuildLog adBuildLog = adBuildLogDAO.qryBuildLog(buildSeq, adBranch.getBranchId());
        setStageDate(stageinfoExtPojo, adBuildLog);
        return stageinfoExtPojo;
    }

    private void setStageDate(StageinfoExtPojo stageinfoExtPojo, AdBuildLog adBuildLog) {
        if (adBuildLog != null) {
            stageinfoExtPojo.setBeginTime(DateConvertUtils.date2String(adBuildLog.getCreateDate(), "yyyy-MM-dd HH:mm:ss"));
            if (adBuildLog.getBuildDate() != null) {
                stageinfoExtPojo.setDelayTime(DateConvertUtils.tranTime(adBuildLog.getBuildDate().getTime() - adBuildLog.getCreateDate().getTime()) + " ");
            } else {
                stageinfoExtPojo.setDelayTime("");
            }
        }
    }

    /***
     * @param adStages
     * @return
     * @function 查询构建环节信息
     */
    public List<BuildStageInfoDtlExtPojo> qryPipeInfo(List<AdStage> adStages, AdBranch adBranch) throws SQLException {
        List<BuildStageInfoDtlExtPojo> buildStageInfoDtlExtPojoList = new ArrayList<BuildStageInfoDtlExtPojo>();
        BuildStageInfoDtlExtPojo buildStageInfoDtlExtPojo;
        AdTreeData adTreeData = adTreeDataDAO.qryByTreePara(adBranch.getAdProject().getAdGroup().getGroupId());
        AdStaticData adStaticData = bsStaticDataDAO.qryByCodeType("SONAR_IP").get(0);
        if (CollectionUtils.isNotEmpty(adStages)) {
            for (AdStage adStage : adStages) {
                buildStageInfoDtlExtPojo = new BuildStageInfoDtlExtPojo();
                buildStageInfoDtlExtPojo.setStageId(adStage.getStageId());
                if (adStage.getDealResult() != 0) {
                    if (null == adStage.getBuildSeq()) {
                        continue;
                    }
                    AdStageLogDtl adStageLogDtl = adStageLogDtlImpl.qryLastAdstage(adStage.getBuildSeq(), adStage.getStageId());
                    if (null != adStageLogDtl) {
                        if (adStage.getStageCode() == CommConstants.STAGE_CODE.webScan) {
                            if (adStageLogDtl.getFailLog().contains("-------------------\n")) {
                                enhanceResult(adStageLogDtl.getFailLog().split("-------------------\n")[1], buildStageInfoDtlExtPojo);
                            }
                        } else if (adStage.getStageCode() == CommConstants.STAGE_CODE.wvs) {
                            if (adStage.getDealResult() == 2) {
                                String url = "";
                                String packageName = "";
                                String[] pnameList = new String[100];
                                List<DeployPackagesExtPojo> deployPackagesExtPojoList = new ArrayList<DeployPackagesExtPojo>();
                                DeployPackagesExtPojo deployPackagesExtPojo;
                                if (StringUtils.isNotEmpty(adStageLogDtl.getFailLog())) {
                                    String failLog = adStageLogDtl.getFailLog();
                                    for (; failLog.contains("downloadUri"); ) {
                                        deployPackagesExtPojo = new DeployPackagesExtPojo();
                                        failLog = failLog.substring(failLog.indexOf("downloadUri") + 16);
                                        url = failLog.substring(0, failLog.indexOf("\n") - 2).replace("http://", "");
                                        pnameList = url.split("/");
                                        packageName = pnameList[pnameList.length - 1];
                                        deployPackagesExtPojo.setPackageUrl(url);
                                        deployPackagesExtPojo.setPackageName(packageName);
                                        deployPackagesExtPojoList.add(deployPackagesExtPojo);
                                    }
                                    buildStageInfoDtlExtPojo.setPackages(deployPackagesExtPojoList);
                                }
                            }
                        } else if (adStage.getStageCode() == CommConstants.STAGE_CODE.downLoad) {
                            String codeStore = adBranch.getAdProject().getCodeStore();
                            List<StageKeyValue> list = new ArrayList<StageKeyValue>();
                            StageKeyValue keyValue = new StageKeyValue();
                            String commitUrl = "";
                            String commitId1 = "";
                            if (StringUtils.isNotEmpty(codeStore)) {
                                if (codeStore.startsWith("http")) {
                                    commitUrl = codeStore.replace(".git", "/") + "commit" + "/";
                                } else {
                                    commitUrl = codeStore.replace(":", "/").replace("git@", "http://").replace(".git", "/") + "commit" + "/";
                                }
                            }
                            if (StringUtils.isNotEmpty(adStage.getCommitId()) && adStage.getCommitId().length() >= 7) {
                                commitId1 = adStage.getCommitId().substring(0, 7);
                                String commitId = "<a  href=" + commitUrl + adStage.getCommitId() + " target=\"_blank\" >" + commitId1 + "</a>";
                                keyValue.setKey("commitId");
                                keyValue.setValue(commitId);
                                list.add(keyValue);
                                buildStageInfoDtlExtPojo.setKeyValues(list);
                            }
                        } else if (adStage.getStageCode() == CommConstants.STAGE_CODE.proScan) {
                            List<StageKeyValue> list = new ArrayList<StageKeyValue>();
                            StageKeyValue keyValue = new StageKeyValue();
                            String dealScan = "";
                            if (adTreeData != null) {
                                long sonarId = adTreeData.getNumberPara();
                                dealScan = "<a target=\"_blank\" href=" + adStaticData.getCodeValue() + "id=" + sonarId + " >" + "查看详情" + "</a>";
                                keyValue.setKey("扫描详情");
                                keyValue.setValue(dealScan);
                                list.add(keyValue);
                                buildStageInfoDtlExtPojo.setKeyValues(list);
                            }
                        } else if (adStage.getStageCode() == CommConstants.STAGE_CODE.autoTest) {
                            List<StageKeyValue> list = new ArrayList<StageKeyValue>();
                            StageKeyValue keyValue = new StageKeyValue();
                            long buildSeq = adStage.getBuildSeq();
                            AdAutoTestLog dealTest;
                            if (buildSeq != 0) {
                                dealTest = adAutoTestLogDAO.qryAutoTestLogBySeqId(buildSeq);
                                if (dealTest != null) {
                                    keyValue.setKey("测试详情");
                                    keyValue.setValue(dealTest.getTestLog());
                                    list.add(keyValue);
                                    buildStageInfoDtlExtPojo.setKeyValues(list);
                                }

                            }


                        }
                        buildStageInfoDtlExtPojo.setBeginTime(DateConvertUtils.date2String(adStageLogDtl.getBeginDate(), "yyyy-MM-dd HH:mm:ss"));
                        try {
                            Date finishDate = adStageLogDtl.getFinishDate() == null ? new Date() : adStageLogDtl.getFinishDate();
                            buildStageInfoDtlExtPojo.setDurationTime(DateConvertUtils.tranTime(finishDate.getTime() - adStageLogDtl.getBeginDate().getTime()));
                            String fileNum = getBuildFileNum(adBranch, adStage, adStageLogDtl);                     //2017-5-19由王昊一将getBuildFileNum（adBranch.getbranchid...）改为传递对象
                            if (fileNum != null && fileNum.contains("|||")) {
                                buildStageInfoDtlExtPojo.setFileNum(fileNum.split("\\|\\|\\|")[0]);
                                buildStageInfoDtlExtPojo.setFailRate(fileNum.split("\\|\\|\\|")[1]);
                            } else {
                                buildStageInfoDtlExtPojo.setFileNum(fileNum);
                            }
                            if (adStage.getStageCode() == CommConstants.STAGE_CODE.deploy) {
                                String commitId = "";
                                for (AdStage adStage2 : adStages) {
                                    if (StringUtils.isNotEmpty(adStage2.getCommitId())) {
                                        commitId = adStage2.getCommitId();
                                        break;
                                    }
                                }
                                buildStageInfoDtlExtPojo.setPackages(qryDownLoadPackage(commitId, adBranch));
                            }
                        } catch (Exception e) {
                            log.error("查询stage扩展信息失败state_id=" + adStage.getStageId() + " total_step:" + adStage.getBuildSeq(), e);
                        }
                    }
                }
                buildStageInfoDtlExtPojoList.add(buildStageInfoDtlExtPojo);
            }
        }
        return buildStageInfoDtlExtPojoList;
    }


    /***
     * @function 获取文件数
     */

    public String getBuildFileNum(AdBranch adBranch, AdStage adStage, AdStageLogDtl adStageLogDtl) {
        String regx = "";
        String buildLog = "";
        String changeLog = "";
        String fileNum = "";
        String failFileNum = "";
        if (StringUtils.isEmpty(adStageLogDtl.getFailLog())) {
            return "";
        } else {
            buildLog = adStageLogDtl.getFailLog();
        }
        int stageCode = adStage.getStageCode() == null ? 0 : adStage.getStageCode();
        if (stageCode == CommConstants.STAGE_CODE.downLoad) {   //下载环境
            regx = "files changed|file changed";
            Pattern p = Pattern.compile(regx);
            Matcher m = p.matcher(buildLog);
            boolean mflag = m.find();
            if (mflag) {
                changeLog = buildLog.substring(0, m.start());
                fileNum = changeLog.substring(changeLog.lastIndexOf("\n") + 1);
                fileNum = fileNum.trim();
            } else {
                if (StringUtils.isEmpty(adStage.getPreCommitId())) {
                    regx = "Total Number of Files = ";
                    p = Pattern.compile(regx);
                    m = p.matcher(buildLog);
                    mflag = m.find();
                    if (mflag) {
                        changeLog = buildLog.substring(m.start());
                        fileNum = changeLog.substring(regx.length(), changeLog.indexOf("\n"));
                        fileNum = fileNum.trim();
                    }
                }
            }
            if (StringUtils.isEmpty(fileNum)) {
                fileNum = "0";
            }
        } else if (stageCode == CommConstants.STAGE_CODE.building || stageCode == CommConstants.STAGE_CODE.builded) {//编译发布环节
            regx = " Compiling ";
            Pattern p = Pattern.compile(regx);
            Matcher m = p.matcher(buildLog);
            int li_fileNum = 0;
            while (m.find()) {
                changeLog = buildLog.substring(m.start());
                if (changeLog.indexOf("source files") > 0) {
                    fileNum = changeLog.substring(regx.length(), changeLog.indexOf("source files"));
                    fileNum = fileNum.trim();
                    if (StringUtils.isNotEmpty(fileNum)) {
                        li_fileNum = li_fileNum + Integer.parseInt(fileNum);
                    }
                }
            }
            fileNum = li_fileNum + "";
        } else if (stageCode == CommConstants.STAGE_CODE.unitTest) {
            regx = "Tests run:";
            Pattern p = Pattern.compile(regx);
            Matcher m = p.matcher(buildLog);
            int li_fileNum = 0;
            int li_failFileNum = 0;
            String failLog = "";
            while (m.find()) {
                changeLog = buildLog.substring(m.start());
                if (changeLog.indexOf("Failures:") > 0) {
                    failLog = changeLog.substring(changeLog.indexOf("Failures:"));
                    failFileNum = failLog.substring(("Failures:").length(), failLog.indexOf(","));
                    failFileNum = failFileNum.trim();
                    if (StringUtils.isNotEmpty(failFileNum)) {
                        li_failFileNum = Integer.parseInt(failFileNum);//li_failFileNum +
                    }
                }
                if (changeLog.indexOf(",") > 0) {
                    fileNum = changeLog.substring(regx.length(), changeLog.indexOf(","));
                    fileNum = fileNum.trim();
                    if (StringUtils.isNotEmpty(fileNum)) {
                        li_fileNum = Integer.parseInt(fileNum);//li_fileNum +
                    }
                }
            }
            if (li_fileNum != 0)
                fileNum = li_fileNum + "|||" + ((li_fileNum - li_failFileNum) * 100 / li_fileNum) + "%";
        } else if (stageCode == CommConstants.STAGE_CODE.deploy) {
            fileNum = adVirtualEnvironmentDAO.getServerUrl(adBranch);
        } else if (stageCode == CommConstants.STAGE_CODE.wvs) {
            regx = "alerts found";
            Pattern p = Pattern.compile(regx);
            Matcher m = p.matcher(buildLog);
            int li_fileNum = 0;
            String failLog = "";
            boolean mflag = m.find();
            if (mflag) {
                failLog = buildLog.substring(0, m.start());
                fileNum = failLog.substring(failLog.lastIndexOf("-") + 1);
                fileNum = fileNum.trim();
                if (StringUtils.isNotEmpty(fileNum) && !("NO").equals(fileNum.toUpperCase())) {
                    li_fileNum = li_fileNum + Integer.parseInt(fileNum);
                }
            }
            fileNum = li_fileNum + "";
        } else if (stageCode == CommConstants.STAGE_CODE.autoTest) {
            AdSeqTestRelate adSeqTestRelate = adSeqTestRelateImpl.qryByStageId(adStageLogDtl.getTotalStep(), adStageLogDtl.getAdStage().getStageId());
            if (adSeqTestRelate != null) {
                fileNum = adSeqTestRelate.getTotalNum() + "";
            }
            if (StringUtils.isEmpty(fileNum)) {
                fileNum = "0";
            }
        } else if (stageCode == CommConstants.STAGE_CODE.webScan && buildLog.contains("the scan details : ")) {
            fileNum = buildLog.substring(buildLog.lastIndexOf("<a"), buildLog.lastIndexOf("</a>") + 4);
        }
        return fileNum;
    }

    public List<DeployPackagesExtPojo> qryDownLoadPackage(String commitId, AdBranch adBranch) {
        List<AdProjectDeployPackage> adProjectDeployPackageList;
        List<DeployPackagesExtPojo> packages = new ArrayList<>();
        if (StringUtils.isNotEmpty(commitId)) {
            adProjectDeployPackageList = adProjectDeployPackageDAO.qryByCommitAndBranch(commitId, adBranch.getBranchId());
            System.out.println("commitId===" + commitId + "adBranch.getBranchId()" + adBranch.getBranchId());
            if (adProjectDeployPackageList != null && adProjectDeployPackageList.size() > 0) {
                for (AdProjectDeployPackage adPackage : adProjectDeployPackageList) {
                    DeployPackagesExtPojo deployPackagesExtPojo = new DeployPackagesExtPojo();
                    deployPackagesExtPojo.setPackageUrl(adPackage.getPackagePath().split("http://")[1]);
                    int deployPackageLength = adPackage.getPackagePath().split("/").length;
                    deployPackagesExtPojo.setPackageName(adPackage.getPackagePath().split("/")[deployPackageLength - 1]);
                    packages.add(deployPackagesExtPojo);
                }
            } else {
                System.out.println("没有下载包！！！！！！！！！！！");
                DeployPackagesExtPojo deployPackagesExtPojo = new DeployPackagesExtPojo();
                deployPackagesExtPojo.setPackageName("");
                deployPackagesExtPojo.setPackageUrl("");
                packages.add(deployPackagesExtPojo);
            }
        } else {
            DeployPackagesExtPojo deployPackagesExtPojo = new DeployPackagesExtPojo();
            deployPackagesExtPojo.setPackageName("");
            deployPackagesExtPojo.setPackageUrl("");
            packages.add(deployPackagesExtPojo);
        }
        return packages;
    }
}
