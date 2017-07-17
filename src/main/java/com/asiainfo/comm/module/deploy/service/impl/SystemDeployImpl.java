package com.asiainfo.comm.module.deploy.service.impl;

import com.asiainfo.auth.sso.gitlib.api.GitlabAPI;
import com.asiainfo.auth.sso.gitlib.api.models.GitlabProject;
import com.asiainfo.comm.common.pojo.pojoExt.*;
import com.asiainfo.comm.externalservice.jenkins.client.JenkinsClient;
import com.asiainfo.comm.externalservice.jenkins.guice.JenkinsWsClientGuiceModule;
import com.asiainfo.comm.externalservice.jenkins.jobconfig.JobConfigUtil;
import com.asiainfo.comm.externalservice.jenkins.jobconfig.entity.*;
import com.asiainfo.comm.externalservice.jenkins.jobs.Job;
import com.asiainfo.comm.externalservice.jenkins.jobs.JobImpl;
import com.asiainfo.comm.module.build.dao.impl.*;
import com.asiainfo.comm.module.build.service.impl.AdDcosDeployInfoImpl;
import com.asiainfo.comm.module.build.service.impl.AdFastenSignImpl;
import com.asiainfo.comm.module.build.service.impl.AdGroupImpl;
import com.asiainfo.comm.module.build.service.impl.GitServiceImpl;
import com.asiainfo.comm.module.common.AdParaDetailDAO;
import com.asiainfo.comm.module.common.AdStaticDataDAO;
import com.asiainfo.comm.module.deploy.dao.impl.AdDcosBranchRelateDAO;
import com.asiainfo.comm.module.deploy.dao.impl.AdDcosDeployInfoDAO;
import com.asiainfo.comm.module.deploy.dao.impl.AdVirtualBranchRelateDAO;
import com.asiainfo.comm.module.deploy.dao.impl.SystemDeployDAO;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.models.query.QAdBranch;
import com.asiainfo.comm.module.models.query.QAdStage;
import com.asiainfo.comm.module.role.dao.impl.AdAuthorDAO;
import com.asiainfo.comm.module.role.dao.impl.AdGroupUserDAO;
import com.asiainfo.comm.module.role.dao.impl.AdRoleDAO;
import com.asiainfo.comm.module.role.service.impl.AdGroupUserImpl;
import com.asiainfo.util.CommConstants;
import com.asiainfo.util.CronUtil;
import com.asiainfo.util.XmlUtils;
import com.avaje.ebean.annotation.Transactional;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


@Component
@lombok.extern.slf4j.Slf4j
public class SystemDeployImpl {
    @Autowired
    SystemDeployDAO systemDeployDAO;
    @Autowired
    AdJenkinsInfoDAO adJenkinsInfoDAO;
    @Autowired
    AdStaticDataDAO bsStaticDataDAO;
    @Autowired
    AdGroupDAO adGroupDAO;
    @Autowired
    AdProjectDAO adProjectDAO;
    @Autowired
    AdUserDAO adUserDAO;
    @Value("${gitlab.server.url}")
    String gitUrl;
    @Autowired
    AdBranchDAO adBranchDAO;
    @Autowired
    AdStageDAO adStageDAO;
    @Autowired
    AdPipeLineStateDAO adPipeLineStateDAO;
    @Autowired
    AdBranchRelatDAO adBranchRelatDAO;
    @Autowired
    AdAuthorDAO adAuthorDAO;
    @Autowired
    AdRoleDAO adRoleDAO;
    @Autowired
    AdGroupUserDAO adGroupUserDAO;
    @Autowired
    AdGroupUserImpl adGroupUserImpl;
    @Autowired
    AdParaDetailDAO bsParaDetailDAO;
    @Autowired
    GitServiceImpl gitServiceImpl;
    @Autowired
    AdJenkinsInfoDAO jkDAO;
    @Autowired
    AdVirtualEnvironmentDAO virtualEnvironmentDAO;
    @Autowired
    AdDcosDeployInfoDAO dcosDeployInfoDAO;
    @Autowired
    AdVirtualEnvironmentDAO adVirtualEnvironmentDAO;
    @Autowired
    AdDcosDeployInfoImpl adDcosDeployInfoImpl;
    @Autowired
    AdDcosDeployDtlDAO adDcosDeployDtlDAO;
    @Autowired
    AdDcosBranchRelateDAO adDcosBranchRelateDAO;
    @Autowired
    AdVirtualBranchRelateDAO adVirtualBranchRelateDAO;
    @Autowired
    AdFastenSignImpl adFastenSignImpl;
    @Autowired
    AdGroupImpl adGroupImpl;
    @Value("${gitlab.admin.token}")
    private String token;

    public String qryBuildScriptByCode(String buildTool, String compileTool) {
        String buildShell = "";
        List<AdParaDetail> adParaDetailList = bsParaDetailDAO.qryListByDetails("X", "BUILD_SCRIPT", buildTool, compileTool);
        if (CollectionUtils.isNotEmpty(adParaDetailList)) {
            buildShell = adParaDetailList.get(0).getPara2();
        }
        return buildShell;
    }

    public SysReformProjectPojoExt qrySystemDeploy(long projectId) throws Exception {
        List<AdBranch> adBranchList = adBranchDAO.qryBranchByProject(projectId);
        Map<String, List<AdStage>> stringListMap = adStageDAO.QryAdOperationByProjectId(projectId);
        SysReformProjectPojoExt sysProjectPojoExt = new SysReformProjectPojoExt();
        List<SysReformBranchPojoExt> sysBranchPojoExtList = new ArrayList<SysReformBranchPojoExt>();
        List<AdStage> adStageList;
        if (adBranchList != null) {
            for (AdBranch adBranch : adBranchList) {
                adStageList = stringListMap.get("" + adBranch.getBranchId());
                sysBranchPojoExtList.add(initBranchPojo(adStageList, adBranch));
            }
        }
        sysProjectPojoExt.setProjectId(projectId + "");
        if (sysBranchPojoExtList.size() > 0) {
            sysProjectPojoExt.setObj(sysBranchPojoExtList.toArray(new SysReformBranchPojoExt[sysBranchPojoExtList.size()]));
        }
        return sysProjectPojoExt;
    }

    public SysReformBranchPojoExt qryStageJkInfo(long branchId) throws Exception {

        List<AdStage> stageList = adStageDAO.qryAdStage(branchId);
        AdBranch adBranch = adBranchDAO.qryById(branchId);
        return initsBranchPojo(stageList, adBranch);
    }

    private SysReformBranchPojoExt initBranchPojo(List<AdStage> adStageList, AdBranch adBranch) throws Exception {
        String downStageName;
        SysReformBranchPojoExt sysBranchPojoExt;
        List<SysStagePojoExt> sysStagePojoExtList;
        List<AdStaticData> adStaticDataList;
        boolean check;
        String shell;
        String jenkinJob;
        String stageId;
        String jobSchedule;
        String stageCode;
        long pipelineId;
        SysStagePojoExt sysStagePojoExt;
        Injector injector;
        downStageName = "";
        sysBranchPojoExt = new SysReformBranchPojoExt();
        sysStagePojoExtList = new ArrayList<SysStagePojoExt>();
        sysBranchPojoExt.setJkId(adBranch.getAdJenkinsInfo().getJenkinsId() + "");
        sysBranchPojoExt.setBranchPath(adBranch.getBranchPath());
        sysBranchPojoExt.setBranchName(adBranch.getBranchName());
        sysBranchPojoExt.setBranchDesc(adBranch.getBranchDesc());
        sysBranchPojoExt.setBranchType(adBranch.getBranchType() + "");
        sysBranchPojoExt.setBranchId(adBranch.getBranchId() + "");
        sysBranchPojoExt.setTriggerBranch(adBranch.getTriggerBranch() + "");
        sysBranchPojoExt.setBuildCron(adBranch.getBuildCron() + "");
        sysBranchPojoExt.setOriginPath(adBranch.getOriginPath());
        if (StringUtils.isNotEmpty(adBranch.getBuildFileType())) {
            String[] buildFileTypes = adBranch.getBuildFileType().split(",");
            Map<String, Boolean> typeMap = new HashMap<String, Boolean>();
            for (String buildFile : buildFileTypes) {
                typeMap.put(buildFile, true);
            }
            sysBranchPojoExt.setBuildFileTypes(typeMap);
        }
        adStaticDataList = qryDefaultStage();
        AdJenkinsInfo jenkinsInfo = adJenkinsInfoDAO.qryByJkId(Long.valueOf(sysBranchPojoExt.getJkId()));
        for (AdStaticData adStaticData : adStaticDataList) {
            check = false;
            shell = "";
            jenkinJob = "";
            stageId = "";
            jobSchedule = "";
            stageCode = adStaticData.getCodeValue();
            pipelineId = 0;
            sysStagePojoExt = new SysStagePojoExt();
            if (CollectionUtils.isNotEmpty(adStageList)) {
                for (AdStage adStage : adStageList) {
                    if (adStaticData.getCodeValue().equals(adStage.getStageCode() + "")) {
                        check = true;
                        jenkinJob = adStage.getJenkinsJobName();
                        stageId = "" + adStage.getStageId();
                        pipelineId = adStage.getAdPipeLineState().getPipelineId();
                        stageCode = "" + adStage.getStageCode();
                        jobSchedule = adStage.getJobSchedule();
                        if (("1").equals(stageCode)) {
                            downStageName = adStage.getJenkinsJobName();
                        }
                        JobConfig jobinfo;
                        if (StringUtils.isEmpty(adStage.getStageConfig())) {
                            injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                                "http://" + jenkinsInfo.getJenkinsUrl() + ":" + jenkinsInfo.getServerPort()), jenkinsInfo.getJenkinsUsername(), jenkinsInfo.getJenkinsPassword()));
                            JenkinsClient client = injector.getInstance(JenkinsClient.class);
                            Job job = client.retrieveJob(adStage.getJenkinsJobName());
                            jobinfo = job.getJobinfo();
                            client.close();
                            adStageDAO.updateStageConfig(adStage.getStageId().intValue(), job.asXml());
                        } else {
                            Job job = JobImpl.fromXml(jenkinJob, adStage.getStageConfig());
                            jobinfo = job.getJobinfo();
                        }

                        if (jobinfo.getBuilders() != null && jobinfo.getBuilders().size() > 0) {
                            shell = jobinfo.getBuilders().get(0).getCommand();
                        }
                        break;
                    }
                }
            }
            if (pipelineId == 0) {
                AdPipeLineState adPipeLineState = adPipeLineStateDAO.qryEnvById(adBranch.getBranchId());
                if (adPipeLineState != null) {
                    pipelineId = adPipeLineState.getPipelineId();
                }
            }
            sysBranchPojoExt.setPipelineId("" + pipelineId);
            if (shell != null) {
                shell = shell.replaceAll(CommConstants.getDownJobName(adBranch.getAdJenkinsInfo().getPathShell(), downStageName), "");
                if (("" + CommConstants.STAGE_CODE.wvs).equals(stageCode)) {
                    if (shell.contains(";")) {
                        String[] shellList = shell.split(";");
                        String[] wvsShellList;
                        StringBuffer sbff = new StringBuffer();
                        for (int count = 0; count < shellList.length; count++) {
                            wvsShellList = shellList[count].split(" ");
                            sbff = sbff.append(wvsShellList[wvsShellList.length - 1]);
                            if (count != shellList.length - 1) {
                                sbff = sbff.append(",");
                            } else {
                                sbff = sbff.append("");
                            }
                            shell = sbff + "";
                        }
                    } else {
                        String[] wvsShellList = shell.split(" ");
                        shell = wvsShellList[wvsShellList.length - 1];
                    }
                }
                if (("" + CommConstants.STAGE_CODE.webScan).equals(stageCode)) {
                    String[] webScanList = shell.split("=");
                    shell = webScanList[webScanList.length - 1].replace("@3", "");
                }
            }
            sysStagePojoExt.setShellCommand(shell);
            sysStagePojoExt.setCheck(check);
            sysStagePojoExt.setStageId(stageId);
            sysStagePojoExt.setJkJobName(jenkinJob);
            sysStagePojoExt.setStageConfig(adStaticData.getCodeName());
            sysStagePojoExt.setStagecode(stageCode);
            sysStagePojoExt.setStageShow(false);
            sysStagePojoExt.setSpec(jobSchedule);
            sysStagePojoExtList.add(sysStagePojoExt);
        }
        sysBranchPojoExt.setStages(sysStagePojoExtList.toArray(new SysStagePojoExt[sysStagePojoExtList.size()]));
        if (StringUtils.isNotEmpty(adBranch.getEnvType())) {
            if ("none".equals(adBranch.getEnvType()) || adBranch.getEnvId() == null) {
                sysBranchPojoExt.setEnvId("");
            } else {
                sysBranchPojoExt.setEnvId(adBranch.getEnvId() + "_" + adBranch.getEnvType());
            }
        } else {
            sysBranchPojoExt.setEnvId("null");
        }

        return sysBranchPojoExt;
    }

    public void initVmData(SysEnvConfigExt envConfigExt, AdVirtualEnvironment vm) {
        SysVmDataExt ext = new SysVmDataExt();
        ext.setVmId(vm.getVirtualId());
        ext.setSourceAddress(vm.getSourceAddress());
        ext.setDestAddress(vm.getDestinationAddress());
        ext.setFileName(vm.getFileName());
        ext.setFilePath(vm.getFilePath());
        ext.setPackName(vm.getPackageName());
        ext.setServerUrl(vm.getServerUrl());
        ext.setServerUsername(vm.getServerUsername());
        envConfigExt.setVmData(ext);
    }

    public SysEnvConfigExt getSysEnvConfigExt(AdBranch adBranch) {
        SysEnvConfigExt envConfigExt = new SysEnvConfigExt();
        switch (adBranch.getEnvType()) {
            case "vm":
                envConfigExt.setEnvType("vm");
                AdVirtualEnvironment vm = virtualEnvironmentDAO.qryIinfoByBranchID(adBranch.getBranchId());
                if (vm != null) {
                    initVmData(envConfigExt, vm);
                }
                break;
            case "dcos":
                envConfigExt.setEnvType("dcos");
                List<AdDcosDeployDtl> dcosList = dcosDeployInfoDAO.qryDcosDeployDtlByBranchId(adBranch.getBranchId());
                if (dcosList != null) {
                    initDcosData(envConfigExt, dcosList);
                }
                break;
            default:
                envConfigExt.setEnvType("none");
                break;
        }
        return envConfigExt;
    }

    public void initDcosData(SysEnvConfigExt envConfigExt, List<AdDcosDeployDtl> dcosList) {
        List<SysDcosDataExt> dcosDataExts = new ArrayList<>();
        for (AdDcosDeployDtl dcos : dcosList) {
            SysDcosDataExt ext = new SysDcosDataExt();
            ext.setAppid(dcos.getAppid());
            ext.setPackageName(dcos.getPackageName());
            dcosDataExts.add(ext);
        }
        envConfigExt.setDcosData(dcosDataExts.toArray(new SysDcosDataExt[dcosDataExts.size()]));
    }

    public void addJenkinsCallbackUrl(JobConfig jobConfig, String callbackUrl) throws Exception {
        List<Endpoint> alist = new ArrayList<Endpoint>();
        HudsonNotificationProperty hudsonNotificationProperty = new HudsonNotificationProperty();
        Endpoint endpoint = new Endpoint();
        endpoint.setProtocol("HTTP");
        endpoint.setFormat("JSON");
        endpoint.setUrl(callbackUrl);
        endpoint.setEvent("started");
        endpoint.setTimeout(30000);
        endpoint.setLoglines(-1);
        alist.add(endpoint);
        endpoint = new Endpoint();
        endpoint.setProtocol("HTTP");
        endpoint.setFormat("JSON");
        endpoint.setUrl(callbackUrl);
        endpoint.setEvent("completed");
        endpoint.setTimeout(30000);
        endpoint.setLoglines(-1);
        alist.add(endpoint);
        hudsonNotificationProperty.setEndpoints(alist);
        jobConfig.addProperty(hudsonNotificationProperty);
    }

    public void addJenkinsAssignedNode(JobConfig jobConfig) throws Exception {
        jobConfig.setAssignedNode("mesos");
        jobConfig.setCanRoam(false);
    }

    public void addDownJenkinsJob(JobConfig jobConfig, String downpath, String branch) throws Exception {
        GitSCM scm = new GitSCM();
        List<UserRemoteConfig> userRemoteConfigList = new ArrayList<UserRemoteConfig>();
        UserRemoteConfig userRemoteConfig = new UserRemoteConfig();
        userRemoteConfig.setUrl(downpath);
        userRemoteConfigList.add(userRemoteConfig);
        scm.setUserRemoteConfigs(userRemoteConfigList);
        if (StringUtils.isNotEmpty(branch)) {
            BranchSpec branchSpec = new BranchSpec(branch);
            List<BranchSpec> branchSpecList = new ArrayList<BranchSpec>();
            branchSpecList.add(branchSpec);
            scm.setBranches(branchSpecList);
        }
        scm.setDoGenerateSubmoduleConfigurations(false);
        scm.setConfigVersion(2l);
        scm.setPlugin("git@2.4.0");
        jobConfig.setScm(scm);
    }

    public void addJenkinsDownTime(JobConfig jobConfig, String spec) throws Exception {
        SCMTrigger scmTrigger = new SCMTrigger();
        scmTrigger.setSpec(spec);
        scmTrigger.setIgnorePostCommitHooks(false);
        jobConfig.addTrigger(scmTrigger);
    }


    public void addJenkinsEmail(JobConfig jobConfig) throws Exception {
        ExtendedEmailPublisher extendedEmailPublisher = new ExtendedEmailPublisher();
        extendedEmailPublisher.addConfiguredTrigger(new FailureTrigger());
        jobConfig.addPublisher(extendedEmailPublisher);
    }

    public void addJenkinsJobShell(JobConfig jobConfig, String shell) throws Exception {
        List<Shell> shellList = new ArrayList<Shell>();
        Shell shell1 = new Shell();
        shell1.setCommand(shell);
        shellList.add(shell1);
        jobConfig.setBuilders(shellList);
    }

    public AdGroup addGroup(AdGroup adGroup) throws Exception {
        AdGroup adGroup1 = adGroupDAO.qryAdGroupByname(adGroup.getGroupName());
        if (adGroup1 != null) {
            throw new Exception("相同的group已经存在");
        }
        String opUserName = "test";
        adGroup.setGroupType(1);
        adGroup.setCreateDate(new Date());
        adGroup.setState(1);
        adGroup.setOpUser(opUserName);
        return adGroupDAO.saveAdGroup(adGroup);
    }

    public AdGroup updateGroup(AdGroup adGroup) throws Exception {
        if (adGroup != null) {
            adGroup.update();
        }
        return adGroup;
    }

    public void addProject(ProjectInputExtPojo projectInputExtPojo) throws Exception {
        long opId = projectInputExtPojo.getOpId();
        long roleId = projectInputExtPojo.getRoleId();
        int groupId = projectInputExtPojo.getGroupId();
        String projectName = projectInputExtPojo.getProjectName();
        String ls_codeStore = projectInputExtPojo.getCodeStore();
        AdProject adProject = new AdProject();
        AdUser adUser = adUserDAO.getUserById(opId);
        adProject.setState(1l);
        adProject.setAdUser(adUser);
        adProject.setProjectName(projectName);
        adProject.setGitProjectid(String.valueOf(projectInputExtPojo.getGitProjectid()));
        if (StringUtils.isNotEmpty(projectInputExtPojo.getCompileTool())) {
            adProject.setCompileTool(projectInputExtPojo.getCompileTool());
            adProject.setCompileVersion(projectInputExtPojo.getCompileToolVersion());
        }
        if (StringUtils.isNotEmpty(projectInputExtPojo.getBuildTool())) {
            adProject.setBuildTool(projectInputExtPojo.getBuildTool());
        }
        adProject.setCodeStore(ls_codeStore);
        adProject.setCreateDate(new Date());
        adProject.setAdGroup(adGroupDAO.qryAdGroupById(groupId));
        AdProject adProject1 = adProjectDAO.saveAdProject(adProject);
        List<AdAuthor> adAuthorList = adAuthorDAO.qryroleAndSignCheck(roleId, adProject.getProjectId());
        if (adAuthorList == null || adAuthorList.size() == 0) {
            if (roleId != 0) {
                AdAuthor adAuthor = new AdAuthor();
                adAuthor.setAdRole(adRoleDAO.qryById(roleId));
                adAuthor.setState(1);
                adAuthor.setAdUser(adUser);
                adAuthor.setCreateDate(new Date());
                adAuthor.setAdProject(adProject1);
                adAuthor.save();
            }
        }
    }

    public int getGitProjectId(String codeStore, String privateToken) throws IOException {
        if (StringUtils.isNotEmpty(codeStore)) {
            GitlabAPI gitlabAPI = new GitlabAPI(gitUrl, privateToken);
//            codeStore = gitServiceImpl.replaceUrl(codeStore);
            List<GitlabProject> gitlabProjectList = gitlabAPI.getProjects();
            if (gitlabProjectList != null) {
                for (GitlabProject gitlabProject : gitlabProjectList) {
                    if (codeStore.equals(gitlabProject.getSshUrl()) || codeStore.equals(gitlabProject.getHttpUrl())) {
                        return gitlabProject.getId() == null ? 0 : gitlabProject.getId();
                    }
                }
            }
        }
        return 0;
    }

    public List<Map<String, String>> qryProjectList(int groupId) {
        List<AdProject> adProjectList = adProjectDAO.qryProjectByGroupId(groupId);
        Map<String, String> hmap = null;
        List<Map<String, String>> projectList = null;
        if (adProjectList != null) {
            projectList = new ArrayList<Map<String, String>>();
            for (AdProject adProject : adProjectList) {
                hmap = new HashMap<String, String>();
                hmap.put("projectId", "" + adProject.getProjectId());
                hmap.put("projectName", "" + adProject.getProjectName());
                hmap.put("buildTool", adProject.getBuildTool());
                hmap.put("compileTool", adProject.getCompileTool());
                hmap.put("compileVersion", adProject.getCompileVersion());
                projectList.add(hmap);
            }
        }
        return projectList;
    }

    public List<AdStaticData> qryDefaultStage() {
        String paraType = "";
        paraType = CommConstants.qryBuildType();
        List<AdStaticData> staticDataList = bsStaticDataDAO.qryByCodeType(paraType);
        return staticDataList;
    }

    public List<AdJenkinsInfo> qryAllJenkins() {
        List<AdJenkinsInfo> adJenkinsInfoList = adJenkinsInfoDAO.qryAllJenkins();
        return adJenkinsInfoList;
    }

    @Transactional
    public void deleteStageByGroupId(int groupId) throws Exception {
        if (groupId != 0) {
            List<AdProject> adProjectList = adProjectDAO.qryProjectByGroupId(groupId);
            if (adProjectList != null) {
                for (AdProject adProject : adProjectList) {
                    deleteStageByProjectId(adProject.getProjectId());
                }
                AdGroup adGroup = adGroupDAO.qryAdGroupById(groupId);
                if (adGroup != null) {
                    adGroup.setState(0);
                    adGroup.save();
                    adGroupUserImpl.delete(groupId);
                    // 删除相应的项目 订阅信息
                    adFastenSignImpl.deleteByGroupId(adGroup.getGroupId());
                }

            }
        }

    }

    public void deleteStageByProjectId(long projectId) throws Exception {
        try {
            List<AdBranch> adBranchList = adBranchDAO.getEnvsBySysId(projectId);
            if (adBranchList != null) {
                for (AdBranch adBranch : adBranchList) {
                    if (adBranch != null) {
                        delBranchInfo(adBranch);
//                         删除相应的流水订阅信息
                        adFastenSignImpl.deleteByBranchId(adBranch.getBranchId());
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }
        adStageDAO.deleteStageByProjectId(projectId);
    }

    public void deleteBranchById(long branchId) throws Exception {
        AdBranch adBranch = new QAdBranch().branchId.eq(branchId).findUnique();
        try {
            if (adBranch != null) delBranchInfo(adBranch);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        adStageDAO.deleteStageByBranchId(branchId);
        adDcosBranchRelateDAO.deleteRelationByBranchId(branchId);
        adVirtualBranchRelateDAO.deleteRelationByBranchId(branchId);
        // 删除相应的流水订阅信息
        adFastenSignImpl.deleteByBranchId(branchId);
//        adDcosDeployInfoImpl.deleteDcosSigleInfo(branchId); //删除环境管理里面的
//        adVirtualEnvironmentDAO.deleteSingleInfo(branchId);

    }

    private void delBranchInfo(AdBranch adBranch) throws MalformedURLException {
        Injector injector;
        AdJenkinsInfo jkInfo = adJenkinsInfoDAO.qryByJkId(adBranch.getAdJenkinsInfo().getJenkinsId());
        if (jkInfo != null) {
            List<AdStage> adStageList = new QAdStage().state.eq(1).adBranch.branchId.eq(adBranch.getBranchId()).findList();
            if (adStageList != null) {
                for (AdStage adStage : adStageList) {
                    injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                        "http://" + jkInfo.getJenkinsUrl() + ":" + jkInfo.getServerPort()), jkInfo.getJenkinsUsername(), jkInfo.getJenkinsPassword()));
                    JenkinsClient client = injector.getInstance(JenkinsClient.class);
                    Job job = new JobImpl(adStage.getJenkinsJobName());
                    client.deleteJob(job);
                    client.close();
                }
            }
        }
    }

    public AdGroup addGroup(AdGroup adGroup, AdGroupUser groupUser) throws Exception {
        if (null != adGroup) {
            addGroup(adGroup);
            //添加用户与group关系
            if (adGroup.getGroupId() > 0) {
                groupUser.setGroupId(adGroup.getGroupId());
                adGroupUserDAO.save(groupUser);
            }
        }
        return adGroup;
    }

    public List<SysBuildTool> qryCompileTools() {
        List al = new ArrayList<SysBuildTool>();
        SysBuildTool sysBuildTool;
        List<AdStaticData> adStaticDataList = bsStaticDataDAO.qryByCodeType("COMPILE_TOOL");
        for (AdStaticData adStaticData : adStaticDataList) {
            sysBuildTool = new SysBuildTool();
            sysBuildTool.setToolName(adStaticData.getCodeValue());
            if (StringUtils.isNotEmpty(adStaticData.getCodeName())) {
                String[] toolVersion = adStaticData.getCodeName().split(",");
                sysBuildTool.setToolVersion(toolVersion);
            }
            al.add(sysBuildTool);
        }
        return al;
    }

    public List<SysBuildTool> qryBuildTools(String compileTool, String compileVersion) {
        List al = new ArrayList<SysBuildTool>();
        SysBuildTool sysBuildTool;
        List<AdParaDetail> adParaDetailList = bsParaDetailDAO.qryListByDetails("X", "BUILD_TOOL", compileTool, compileVersion);
        for (AdParaDetail adParaDetail : adParaDetailList) {
            sysBuildTool = new SysBuildTool();
            sysBuildTool.setToolName(adParaDetail.getPara1());
            if (StringUtils.isNotEmpty(adParaDetail.getPara2())) {
                String[] toolVersion = adParaDetail.getPara2().split(",");
                sysBuildTool.setToolVersion(toolVersion);
            }
            al.add(sysBuildTool);
        }
        return al;
    }


    public void updateProjectJobname(String codeStore, long projectId, String gitProjectId) throws Exception {
        List<AdStage> adStageList = adStageDAO.qryDownAdStage(projectId);
        String xml = "";
        for (AdStage adStage : adStageList) {
            xml = updateJobCodestore(codeStore, adStage.getAdBranch(), adStage.getJenkinsJobName());
            adStage.setStageConfig(xml);
            adStage.save();
        }
        updateAdProjectCodestore(codeStore, projectId, gitProjectId);
    }

    public void updateAdProjectCodestore(String codeStore, long projectId, String gitProjectId) throws Exception {
        AdProject adProject = adProjectDAO.getValidSystemById(projectId);
        adProject.setCodeStore(codeStore);
        adProject.setGitProjectid(gitProjectId);
        adProject.save();
    }

    public String updateJobCodestore(String codeStore, AdBranch branch, String jobName) throws Exception {
        Injector injector;
        String xml = "";
        if (branch != null) {
            AdJenkinsInfo jenkinsInfo = jkDAO.qryByJkId(branch.getAdJenkinsInfo().getJenkinsId());
            injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                "http://" + jenkinsInfo.getJenkinsUrl() + ":" + jenkinsInfo.getServerPort()), jenkinsInfo.getJenkinsUsername(), jenkinsInfo.getJenkinsPassword()));
            JenkinsClient client = injector.getInstance(JenkinsClient.class);
            Job job = client.retrieveJob(jobName);
            JobConfig jobinfo = job.getJobinfo();
            addDownJenkinsJob(jobinfo, codeStore, branch.getBranchPath());
            String downShell = null;
            if (jobinfo.getBuilders() != null && jobinfo.getBuilders().size() > 0) {
                downShell = jobinfo.getBuilders().get(0).getCommand();
            }
            if (StringUtils.isNotEmpty(downShell)) {
                if (downShell.indexOf("_commitidfile") > 0) {
                    Random rand = new Random();
                    int randnum = rand.nextInt(9) + 1;
                    downShell = downShell.replaceAll("_commitidfile", "_commitidfile" + randnum);
                    jobinfo.getBuilders().get(0).setCommand(downShell);
                }
            }
            xml = JobConfigUtil.toXMLString(jobinfo);
            job = new JobImpl(jobName);
            System.out.print(xml);
            xml = "<?xml version='1.0' encoding='UTF-8'?>" + xml;
            client.updateJob(job, xml);
            client.close();
        }
        return xml;
    }

    public List<SysBuildTool> qryAllBuildTools() {
        List al = new ArrayList<SysBuildTool>();
        SysBuildTool sysBuildTool;
        List<AdParaDetail> adParaDetailList = bsParaDetailDAO.qryListByParaType("X", "BUILD_TOOL");
        for (AdParaDetail adParaDetail : adParaDetailList) {
            sysBuildTool = new SysBuildTool();
            sysBuildTool.setToolName(adParaDetail.getParaCode());
            if (StringUtils.isNotEmpty(adParaDetail.getPara1())) {
                String[] toolVersion = adParaDetail.getPara1().split(",");
                sysBuildTool.setToolVersion(toolVersion);
            }
            al.add(sysBuildTool);
        }
        return al;
    }

    /**
     * mark：*****************************改造方法*****************************
     *
     * @param sysReformProjectPojoExt 项目信息
     * @throws Exception
     */
    public void addReformSystemDeploy(SysReformProjectPojoExt sysReformProjectPojoExt) throws Exception {
        if (sysReformProjectPojoExt != null) {                                                                          //项目信息不为null
            AdProject adProject;                                                                                        //项目详细信息
            adProject = adProjectDAO.getSystemById(Long.valueOf(sysReformProjectPojoExt.getProjectId()));               //根据项目ID查找项目
            for (SysReformBranchPojoExt sysReformBranchPojoExt : sysReformProjectPojoExt.getObj()) {                    //遍历所有项目分支
                for (SysStagePojoExt sysStagePojoExt : sysReformBranchPojoExt.getStages()) {                            //遍历所有分支节点
                    if (StringUtils.isNotEmpty(sysStagePojoExt.getSpec())) {                                            //判断？？？？
                        if (!("00000").equals(sysStagePojoExt.getSpec())) {                                             //判断？？？
                            String specIsValid = "0 " + sysStagePojoExt.getSpec().trim();                               //待理解*****************
                            int spaceCount = 0;
                            for (int i = 0; i < specIsValid.length(); i++) {
                                char tem = specIsValid.charAt(i);
                                if (tem == ' ') // 空格
                                    spaceCount++;
                            }
                            if (spaceCount != 5) {                                                                      //
                                throw new Exception("自动化测试时间不规范，请输入正确的表达式，如“5 8 * * *”");
                            }
                            String middle = specIsValid.substring(0, specIsValid.length() - 1) + '?';
                            boolean cronExpressionFlag = CronExpression.isValidExpression(middle);                 //
                            if (!cronExpressionFlag) {
                                throw new Exception("自动化测试时间不规范，请输入正确的表达式，如“5 8 * * *”");
                            }
                            String middleSpec = "";
                            middleSpec = sysStagePojoExt.getSpec().trim();
                            sysStagePojoExt.setSpec(middleSpec);
                        }
                    }
                }
            }
            if (adProject != null) {                                                                                    //如果项目不为null
                addReformJenkinsJob(sysReformProjectPojoExt, adProject);                                                //加入到jenkins任务中
                systemDeployDAO.addReformSystemDeploy(sysReformProjectPojoExt, adProject);                              //

            } else {
                throw new Exception("project不存在");
            }
        } else {
            throw new Exception("参数输入不正确");
        }
    }

    /**
     * mark：*******************************************改造方法******************************************
     *
     * @param sysReformProjectPojoExt
     * @param adProject
     * @throws Exception
     */
    public void addReformJenkinsJob(SysReformProjectPojoExt sysReformProjectPojoExt, AdProject adProject) throws Exception {
        String jobName;                                                                                                 //任务名
        String branchName;                                                                                              //分支名
        String para_type = "";                                                                                          //
        Injector injector;                                                                                              //？？？？
        String oldJobName = "";                                                                                         //旧任务名
        String branchType = "";                                                                                         //分支类型
        if (sysReformProjectPojoExt.getObj() != null) {                                                                 //分支不为null
            AdStaticData callBackUrl = bsStaticDataDAO.qryStaticDataByCodeValue("JENKINS_SET", "BACKURL");
            AdStaticData adStaticData = bsStaticDataDAO.qryStaticDataByCodeValue("JENKINS_SET", "USERJK");
            StringBuffer buf = new StringBuffer();
            String buildTool = sysReformProjectPojoExt.getBuildTool();
            String compileTool = sysReformProjectPojoExt.getCompileTool();                                              //编译工具类型
            String compileVersion = sysReformProjectPojoExt.getCompileToolVersion();                                    //编译工具版本
            String buildShell = "";
            String wvsScript = bsParaDetailDAO.qryByParams("X", "WVS_SCRIPT", "WVS").getPara2();
            String webScanScript = bsParaDetailDAO.qryByParams("X", "WebScan_SCRIPT", "WebScan").getPara2();
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
                        buf.append(buildShell);
                        buf.append("\r\n");
                        buf.append(compileScript.get(0).getPara2());
                        buildShell = buf.toString();
                    }
                }
            }
            for (SysReformBranchPojoExt sysReformBranchPojoExt : sysReformProjectPojoExt.getObj()) {                    //遍历项目的分支
                branchName = sysReformBranchPojoExt.getBranchName();                                                    //得到分支的名字
                if (sysReformBranchPojoExt.getStages() == null || sysReformBranchPojoExt.getStages().length < 1) {      //判断分支的节点
                    continue;                                                                                           //为空不存在直接跳过
                }
                AdBranch adBranch = adBranchDAO.qryAdBranchByname(branchName, sysReformBranchPojoExt.getBranchDesc(), adProject.getProjectId());//根据条件查询分支
                if (adBranch != null) {                                                                                 //不为null已经存在
                    throw new Exception("流水编码或流水名称已存在");
                }
                branchType = sysReformBranchPojoExt.getBranchType();                                                    //获得分支类型
                para_type = CommConstants.qryBuildType();                         //构建类型？
                int i = 0;
                Map<String, String> staticDatas = bsStaticDataDAO.qryStaticDataAlias(para_type);                        //？？？？？？
                AdJenkinsInfo jenkinsInfo = null;
                if (StringUtils.isNotEmpty(sysReformBranchPojoExt.getJkId())) {
                    jenkinsInfo = adJenkinsInfoDAO.qryByJkId(Long.valueOf(sysReformBranchPojoExt.getJkId()));           //根据jkid查询jenkinsinfo信息
                } else {
                    if (adStaticData != null) {
                        if (StringUtils.isNotEmpty(adStaticData.getCodeName())) {
                            jenkinsInfo = adJenkinsInfoDAO.qryByJkId(Long.valueOf(adStaticData.getCodeName()));
                            if ("mesos".equals(jenkinsInfo.getJenkinsMode())) {//判断是否是老版group
                                if (bsStaticDataDAO.qryStaticDataByCodeValue("OLD_GROUP", String.valueOf(adProject.getAdGroup().getGroupId())) != null) {
                                    AdStaticData adStaticDataSecondUserjk = bsStaticDataDAO.qryStaticDataByCodeValue("JENKINS_SET", "SECONDUSERJK");
                                    jenkinsInfo = adJenkinsInfoDAO.qryByJkId(Long.valueOf(adStaticDataSecondUserjk.getCodeName()));
                                }
                                ;
                            }
                        }
                    }
                }
                String downStageName = "";                                                                              //
                for (SysStagePojoExt sysStagePojoExt : sysReformBranchPojoExt.getStages()) {                            //遍历节点
                    if (sysStagePojoExt.isCheck() && staticDatas != null && staticDatas.containsKey("" + sysStagePojoExt.getStagecode())) {
                        jobName = branchName + "-" + staticDatas.get("" + sysStagePojoExt.getStagecode());
                        i = i + 1;
                        injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                            "http://" + jenkinsInfo.getJenkinsUrl() + ":" + jenkinsInfo.getServerPort()), jenkinsInfo.getJenkinsUsername(), jenkinsInfo.getJenkinsPassword()));
                        JenkinsClient client = injector.getInstance(JenkinsClient.class);
                        JobConfig jobConfig = new JobConfig();
                        addJenkinsCallbackUrl(jobConfig, callBackUrl.getCodeName());                                    //增加回调地址
                        if ("mesos".equals(jenkinsInfo.getJenkinsMode())) {
                            addJenkinsAssignedNode(jobConfig);
                        }

                        if (("" + CommConstants.STAGE_CODE.downLoad).equals(sysStagePojoExt.getStagecode())) {          //下载环节
                            addDownJenkinsJob(jobConfig, adProject.getCodeStore(), sysReformBranchPojoExt.getBranchPath());
                            downStageName = jobName;
                        }
                        if (StringUtils.isEmpty(sysStagePojoExt.getShellCommand())) {
                            sysStagePojoExt.setShellCommand("");
                        }
                        if (("" + CommConstants.STAGE_CODE.builded).equals(sysStagePojoExt.getStagecode())) {
                            if ("mesos".equals(jenkinsInfo.getJenkinsMode())) {
                                sysStagePojoExt.setShellCommand(buildShell + '\n' + "cd /app/aideploy/jenkins/jobs/workspace/" + branchName + "-down" + '\n' + sysStagePojoExt.getShellCommand());
                            } else {
                                sysStagePojoExt.setShellCommand(buildShell + '\n' + sysStagePojoExt.getShellCommand());
                            }
                        }
                        if (("" + CommConstants.STAGE_CODE.wvs).equals(sysStagePojoExt.getStagecode())) {
                            String stageShellCommend = sysStagePojoExt.getShellCommand();
                            if (stageShellCommend.contains("，")) {
                                stageShellCommend = stageShellCommend.replace("，", ",");
                            }
                            if (stageShellCommend.contains(",")) {
                                String[] wvsShell = new String[15];
                                StringBuffer sbf = new StringBuffer();
                                String[] wvsShellList = stageShellCommend.split(",");
                                for (int count = 0; count < wvsShellList.length; count++) {
                                    wvsShell[count] = wvsScript + " " + sysReformBranchPojoExt.getBranchName() + " " + adProject.getProjectName() + " " + wvsShellList[count];
                                    if (count != wvsShellList.length - 1) {
                                        sbf.append(wvsShell[count]).append(";");
                                    } else {
                                        sbf.append(wvsShell[count]).append("");
                                    }

                                }
                                sysStagePojoExt.setShellCommand(sbf + "");
                            } else {
                                sysStagePojoExt.setShellCommand(wvsScript + " " + sysReformBranchPojoExt.getBranchName() + " " + adProject.getProjectName() + " " + stageShellCommend);
                            }
                        }
                        if (("" + CommConstants.STAGE_CODE.webScan).equals(sysStagePojoExt.getStagecode())) {
                            if (sysStagePojoExt.getShellCommand().contains("，")) {
                                sysStagePojoExt.setShellCommand(sysStagePojoExt.getShellCommand().replace("，", ","));
                            }
                            sysStagePojoExt.setShellCommand(webScanScript + sysStagePojoExt.getShellCommand().trim() + "@3");
                        }
                        if (!("5").equals(branchType)) {
                            if (("" + CommConstants.STAGE_CODE.deploy).equals(sysStagePojoExt.getStagecode())) {
                                sysStagePojoExt.setShellCommand(sysStagePojoExt.getShellCommand() + '\n' + CommConstants.mergeDeployScript(bsParaDetailDAO, "DEPLOY_SCRIPT", sysReformBranchPojoExt.getEnvId(), adVirtualEnvironmentDAO, adDcosDeployDtlDAO, sysReformBranchPojoExt.getOriginPath(), null, null, adGroupImpl, dcosDeployInfoDAO, adProject));
                            }
                            if (("" + CommConstants.STAGE_CODE.restart).equals(sysStagePojoExt.getStagecode())) {
                                sysStagePojoExt.setShellCommand(sysStagePojoExt.getShellCommand() + '\n' + CommConstants.mergeDeployScript(bsParaDetailDAO, "RESTART_SCRIPT", sysReformBranchPojoExt.getEnvId(), adVirtualEnvironmentDAO, adDcosDeployDtlDAO, sysReformBranchPojoExt.getOriginPath(), null, null, adGroupImpl, dcosDeployInfoDAO, adProject));
                            }

                        }
                        if (("" + CommConstants.STAGE_CODE.downLoad).equals(sysStagePojoExt.getStagecode())) {
                            if ("mesos".equals(jenkinsInfo.getJenkinsMode())) {
                                addJenkinsJobShell(jobConfig, sysStagePojoExt.getShellCommand() + " \n sloccount --filecount /app/aideploy/jenkins/jobs/workspace/" + jobName + " \n git diff --stat `cat /app/aideploy/sbin/commitidfile/" + jobName + "_commitidfile`\n" +
                                    "git show | head -n 1 | awk '{print $2}' > /app/aideploy/sbin/commitidfile/" + jobName + "_commitidfile ");
                            } else {
                                addJenkinsJobShell(jobConfig, sysStagePojoExt.getShellCommand() + " \n sloccount --filecount ~/jenkins/jobs/" + jobName + "/workspace \n git diff --stat `cat ~/sbin/commitidfile/" + jobName + "_commitidfile`\n" +
                                    "git show | head -n 1 | awk '{print $2}' > ~/sbin/commitidfile/" + jobName + "_commitidfile ");
                            }
                        } else {
                            sysStagePojoExt.setShellCommand(CommConstants.getDownJobName(jenkinsInfo.getPathShell(), downStageName) + sysStagePojoExt.getShellCommand());
                            addJenkinsJobShell(jobConfig, sysStagePojoExt.getShellCommand());
                        }
                        if (StringUtils.isNotEmpty(sysStagePojoExt.getSpec())) {
                            String ls_spec = "";
                            ls_spec = sysStagePojoExt.getSpec();
                            if (("00000").equals(sysStagePojoExt.getSpec())) {
                                ls_spec = "H/2 * * * *";
                                AdStaticData jobChedule = bsStaticDataDAO.qryStaticDataByCodeValue("JOB_SCHEDULE", "DOWNSTAGE");
                                if (jobChedule != null) {
                                    ls_spec = jobChedule.getCodeName();
                                }
                                addJenkinsDownTime(jobConfig, ls_spec);
                            } else {
                                String specIsValid = "0 " + ls_spec;
                                String middle = specIsValid.substring(0, specIsValid.length() - 1) + '?';
                                boolean cronExpressionFlag = CronExpression.isValidExpression(middle);
                                if (!cronExpressionFlag) {
                                    throw new Exception("自动化测试时间不规范，请输入正确的表达式，如“5 8 * * *”");
                                }
                                ls_spec = ls_spec.trim();
                                sysStagePojoExt.setSpec(ls_spec);
                            }

                        }
                        addJenkinsEmail(jobConfig);
                        jobConfig.setDescription(jobName);
                        String newxml = JobConfigUtil.toXMLString(jobConfig);
                        newxml = "<?xml version='1.0' encoding='UTF-8'?>" + newxml;
                        client.createJob(jobName, XmlUtils.string2Doc(newxml));
                        client.close();
                        oldJobName = jobName;
                        sysStagePojoExt.setJkJobName(jobName);
                        sysStagePojoExt.setStageConfig(newxml);
                    }

                }

            }
        }
    }

    private SysReformBranchPojoExt initsBranchPojo(List<AdStage> adStageList, AdBranch adBranch) throws Exception {

        String downStageName;
        SysReformBranchPojoExt sysBranchPojoExt;
        List<SysStagePojoExt> sysStagePojoExtList;
        List<AdStaticData> adStaticDataList;
        boolean check;
        String shell;
        String jenkinJob;
        String stageId;
        String jobSchedule;
        String stageCode;
        long pipelineId;
        SysStagePojoExt sysStagePojoExt;
        Injector injector;
        downStageName = "";
        sysBranchPojoExt = new SysReformBranchPojoExt();
        sysStagePojoExtList = new ArrayList<SysStagePojoExt>();
        sysBranchPojoExt.setJkId(adBranch.getAdJenkinsInfo().getJenkinsId() + "");
        sysBranchPojoExt.setBranchPath(adBranch.getBranchPath());
        sysBranchPojoExt.setBranchName(adBranch.getBranchName());
        sysBranchPojoExt.setBranchDesc(adBranch.getBranchDesc());
        sysBranchPojoExt.setBranchType(adBranch.getBranchType() + "");
        sysBranchPojoExt.setBranchId(adBranch.getBranchId() + "");
        sysBranchPojoExt.setTriggerBranch(adBranch.getTriggerBranch() + "");
        sysBranchPojoExt.setBuildCron(adBranch.getBuildCron());
        sysBranchPojoExt.setOriginPath(adBranch.getOriginPath());
        if (StringUtils.isNotEmpty(adBranch.getBuildFileType())) {
            String[] buildFileTypes = adBranch.getBuildFileType().split(",");
            Map<String, Boolean> typeMap = new HashMap<String, Boolean>();
            for (String buildFile : buildFileTypes) {
                typeMap.put(buildFile, true);
            }
            sysBranchPojoExt.setBuildFileTypes(typeMap);
        }
        adStaticDataList = qryDefaultStage();
        AdJenkinsInfo jenkinsInfo = adJenkinsInfoDAO.qryByJkId(Long.valueOf(sysBranchPojoExt.getJkId()));
        for (AdStaticData adStaticData : adStaticDataList) {
            check = false;
            shell = "";
            jenkinJob = "";
            stageId = "";
            jobSchedule = "";
            stageCode = adStaticData.getCodeValue();
            pipelineId = 0;
            sysStagePojoExt = new SysStagePojoExt();
            if (CollectionUtils.isNotEmpty(adStageList)) {
                for (AdStage adStage : adStageList) {
                    if (adStaticData.getCodeValue().equals(adStage.getStageCode() + "")) {
                        check = true;
                        jenkinJob = adStage.getJenkinsJobName();
                        stageId = "" + adStage.getStageId();
                        pipelineId = adStage.getAdPipeLineState().getPipelineId();
                        stageCode = "" + adStage.getStageCode();
                        jobSchedule = adStage.getJobSchedule();
                        if (("1").equals(stageCode)) {
                            downStageName = adStage.getJenkinsJobName();
                        }
                        JobConfig jobinfo;
                        if (StringUtils.isEmpty(adStage.getStageConfig())) {
                            injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                                "http://" + jenkinsInfo.getJenkinsUrl() + ":" + jenkinsInfo.getServerPort()), jenkinsInfo.getJenkinsUsername(), jenkinsInfo.getJenkinsPassword()));
                            JenkinsClient client = injector.getInstance(JenkinsClient.class);
                            Job job = client.retrieveJob(adStage.getJenkinsJobName());
                            jobinfo = job.getJobinfo();
                            client.close();
                            adStageDAO.updateStageConfig(adStage.getStageId().intValue(), job.asXml());
                        } else {
                            Job job = JobImpl.fromXml(jenkinJob, adStage.getStageConfig());
                            jobinfo = job.getJobinfo();
                        }

                        if (jobinfo.getBuilders() != null && jobinfo.getBuilders().size() > 0) {
                            shell = jobinfo.getBuilders().get(0).getCommand();
                        }
                        break;
                    }
                }
            }
            if (pipelineId == 0) {
                AdPipeLineState adPipeLineState = adPipeLineStateDAO.qryEnvById(adBranch.getBranchId());
                if (adPipeLineState != null) {
                    pipelineId = adPipeLineState.getPipelineId();
                }
            }
            sysBranchPojoExt.setPipelineId("" + pipelineId);
            if (shell != null) {
                shell = shell.replaceAll(CommConstants.getDownJobName(adBranch.getAdJenkinsInfo().getPathShell(), downStageName), "");
                if (("" + CommConstants.STAGE_CODE.wvs).equals(stageCode)) {
                    if (shell.contains(";")) {
                        String[] shellList = shell.split(";");
                        String[] wvsShellList;
                        StringBuffer sbff = new StringBuffer();
                        for (int count = 0; count < shellList.length; count++) {
                            wvsShellList = shellList[count].split(" ");
                            sbff = sbff.append(wvsShellList[wvsShellList.length - 1]);
                            if (count != shellList.length - 1) {
                                sbff = sbff.append(",");
                            } else {
                                sbff = sbff.append("");
                            }
                            shell = sbff + "";
                        }
                    } else {
                        String[] wvsShellList = shell.split(" ");
                        shell = wvsShellList[wvsShellList.length - 1];
                    }
                }
                if (("" + CommConstants.STAGE_CODE.webScan).equals(stageCode)) {
                    String[] webScanList = shell.split("=");
                    shell = webScanList[webScanList.length - 1].replace("@3", "");
                }
            }
            sysStagePojoExt.setShellCommand(shell);
            sysStagePojoExt.setCheck(check);
            sysStagePojoExt.setStageId(stageId);
            sysStagePojoExt.setJkJobName(jenkinJob);
            sysStagePojoExt.setStageConfig(adStaticData.getCodeName());
            sysStagePojoExt.setStagecode(stageCode);
            sysStagePojoExt.setSpec(jobSchedule);
            sysStagePojoExtList.add(sysStagePojoExt);
        }
        sysBranchPojoExt.setStages(sysStagePojoExtList.toArray(new SysStagePojoExt[sysStagePojoExtList.size()]));

        //添加环境信息
        if (adBranch.getEnvType() != null && !adBranch.getEnvType().equals("")) {
            if (("none").equals(adBranch.getEnvType()) || adBranch.getEnvId() == null) {
            } else {
                String envId = adBranch.getEnvId() + "_" + adBranch.getEnvType();
                sysBranchPojoExt.setEnvId(envId);
            }
        } else {
            sysBranchPojoExt.setEnvId("");
        }
        return sysBranchPojoExt;
    }

    public boolean checkCron(Map map) throws Exception {
        String cron = (String) map.get("cron");
        return "".equals(cron) || CronUtil.checkCron(cron);
    }
}
