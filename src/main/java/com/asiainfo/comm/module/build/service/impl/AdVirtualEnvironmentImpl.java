package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.*;
import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.externalservice.jenkins.client.JenkinsClient;
import com.asiainfo.comm.externalservice.jenkins.guice.JenkinsWsClientGuiceModule;
import com.asiainfo.comm.externalservice.jenkins.jobconfig.JobConfigUtil;
import com.asiainfo.comm.externalservice.jenkins.jobconfig.entity.JobConfig;
import com.asiainfo.comm.externalservice.jenkins.jobs.Job;
import com.asiainfo.comm.externalservice.jenkins.jobs.JobImpl;
import com.asiainfo.comm.module.build.dao.impl.*;
import com.asiainfo.comm.module.deploy.dao.impl.AdDcosDeployInfoDAO;
import com.asiainfo.comm.module.deploy.service.impl.SystemDeployImpl;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.common.AdParaDetailDAO;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by weif on 2017/3/22.
 */
@Component
public class AdVirtualEnvironmentImpl {
    @Autowired
    AdStageDAO adStageDAO;
    @Autowired
    AdJenkinsInfoDAO jkDAO;
    @Autowired
    AdParaDetailDAO bsParaDetailDAO;
    @Autowired
    AdDcosDeployDtlDAO adDcosDeployDtlDAO;
    @Autowired
    AdBranchDAO adBranchDAO;
    @Autowired
    JenkinsImpl jenkins;
    @Autowired
    SystemDeployImpl systemDeployImpl;
    @Autowired
    AdJenkinsInfoDAO adJenkinsInfoDAO;
    @Autowired
    AdPipeLineStateDAO adPipeLineStateDAO;
    @Autowired
    AdGroupImpl adGroupImpl;
    @Autowired
    AdDcosDeployInfoDAO dcosDeployInfoDAO;
    @Autowired
    private AdVirtualEnvironmentDAO adVirtualEnvironmentDAO;

    public AdVirtualEnvironmentPojo getSingleVmEvn(long projectId, long virtualId) throws Exception {
        AdVirtualEnvironmentPojo adVirtualEnvironmentPojo = new AdVirtualEnvironmentPojo();
        AdVirtualEnvironment adVirtualEnvironment = adVirtualEnvironmentDAO.getSingleVmEvn(projectId, virtualId);
        if (adVirtualEnvironment != null) {
            adVirtualEnvironmentPojo.setVirtualName(adVirtualEnvironment.getVirtualName());
            adVirtualEnvironmentPojo.setSourceAddress(adVirtualEnvironment.getSourceAddress());
            adVirtualEnvironmentPojo.setServerUsername(adVirtualEnvironment.getServerUsername());
            adVirtualEnvironmentPojo.setServerPassword(adVirtualEnvironment.getServerPassword());
            adVirtualEnvironmentPojo.setServerUrl(adVirtualEnvironment.getServerUrl());
            adVirtualEnvironmentPojo.setRestartShell(adVirtualEnvironment.getRestartShell());
            adVirtualEnvironmentPojo.setState(adVirtualEnvironment.getState());
            adVirtualEnvironmentPojo.setRegion(adVirtualEnvironment.getRegion());
            adVirtualEnvironmentPojo.setPackageName(adVirtualEnvironment.getPackageName());
            adVirtualEnvironmentPojo.setFileName(adVirtualEnvironment.getFileName());
            adVirtualEnvironmentPojo.setFilePath(adVirtualEnvironment.getFilePath());
            adVirtualEnvironmentPojo.setAdBranch(adVirtualEnvironment.getAdBranch());
            adVirtualEnvironmentPojo.setAdProject(adVirtualEnvironment.getAdProject());
            adVirtualEnvironmentPojo.setDestinationAddress(adVirtualEnvironment.getDestinationAddress());
            adVirtualEnvironmentPojo.setEnv_type(adVirtualEnvironment.getEnv_type());
        }
        return adVirtualEnvironmentPojo;
    }

    public List<AdBranchCheckPojoExt> qryEnvRlateBranchByRegion(long projectId, int region, String envType, long virId) {
        return adVirtualEnvironmentDAO.qryEnvRlateBranchByRegion(projectId, region, envType, virId);
    }

    public void updateVmEvnById(vmEvnPojoExt req) throws Exception {
        try {
            vmEnvInfoPojoExt[] envInfoPojoExts;
            AdVirtualEnvironment adVirtualEnvironment;
            envInfoPojoExts = req.getObj();
            if (envInfoPojoExts != null) {
                adVirtualEnvironment = adVirtualEnvironmentDAO.getSingleVmEvn(req.getProjectId(), req.getVirtualId());
                adVirtualEnvironment.setFileName(envInfoPojoExts[0].getFileName());
                adVirtualEnvironment.setFilePath(envInfoPojoExts[0].getFilePath());
                if (envInfoPojoExts[0].getServerPassword() != null && !StringUtils.isEmpty(envInfoPojoExts[0].getServerPassword())) {
                    adVirtualEnvironment.setServerPassword(envInfoPojoExts[0].getServerPassword());
                }
                adVirtualEnvironment.setServerUrl(envInfoPojoExts[0].getServerUrl());
                adVirtualEnvironment.setVirtualName(envInfoPojoExts[0].getVirtualName());
                adVirtualEnvironment.setServerUsername(envInfoPojoExts[0].getServerUsername());
                adVirtualEnvironment.setDestinationAddress(envInfoPojoExts[0].getDestinationAddress());
                adVirtualEnvironment.setPackageName(envInfoPojoExts[0].getPackageName());
                adVirtualEnvironment.setSourceAddress(envInfoPojoExts[0].getSourceAddress());
                updateStageScript(req.getVirtualId(), "vm", adVirtualEnvironment, null);
            }
        } catch (Exception e) {
            throw e;
        }
        try {
            adVirtualEnvironmentDAO.beginTraction();
            adVirtualEnvironmentDAO.updateVmEvnById(req);
            adVirtualEnvironmentDAO.updateVmEvnRelateById(req);
            adVirtualEnvironmentDAO.commitTraction();
            List<AdBranch> adBranchList = adBranchDAO.qryBranchRelateByEnvId(req.getVirtualId());
            Map<String, List<AdStage>> stringListMap = adStageDAO.QryAdOperationByProjectId(req.getProjectId());
            SysReformProjectPojoExt sysProjectPojoExt = new SysReformProjectPojoExt();
            List<SysReformBranchPojoExt> sysBranchPojoExtList = new ArrayList<SysReformBranchPojoExt>();
            List<AdStage> adStageList;
            if (adBranchList != null) {
                for (AdBranch adBranch : adBranchList) {
                    adStageList = stringListMap.get("" + adBranch.getBranchId());
                    sysBranchPojoExtList.add(initBranchPojo(adStageList, adBranch));
                }
            }
            sysProjectPojoExt.setProjectId(req.getProjectId() + "");
            if (sysBranchPojoExtList.size() > 0) {
                sysProjectPojoExt.setObj(sysBranchPojoExtList.toArray(new SysReformBranchPojoExt[sysBranchPojoExtList.size()]));
            }
            jenkins.updateReformSystemDeploy(sysProjectPojoExt);
        } catch (Exception e) {
            throw e;
        } finally {
            adVirtualEnvironmentDAO.endTraction();

        }

    }

    public void updateStageScript(long envId, String envType, AdVirtualEnvironment adVirtualEnvironment, List<AdDcosDeployDtl> adDcosDeployDtlList) throws Exception {
        List<AdStage> adStageList = adStageDAO.qryStageByEnvIdType(envId, envType);
        Injector injector;
        String xml;
        if (CollectionUtils.isNotEmpty(adStageList)) {
            for (AdStage adStage : adStageList) {
                AdProject adProject = adStageDAO.qryAdProjectByStageId(adStage.getStageId());
                AdJenkinsInfo jkInfo = jkDAO.qryByJkId(adStage.getAdBranch().getAdJenkinsInfo().getJenkinsId());
                if (jkInfo != null) {
                    int serverPort = jkInfo.getServerPort();
                    String jkUrl = "http://" + jkInfo.getJenkinsUrl() + ":" + serverPort;
                    jkInfo.setJenkinsUrl(jkUrl);

                    injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                        jkInfo.getJenkinsUrl()), jkInfo.getJenkinsUsername(), jkInfo.getJenkinsPassword()));
                    if (injector != null) {
                        JenkinsClient client = injector.getInstance(JenkinsClient.class);
                        Job job = client.retrieveJob(adStage.getJenkinsJobName());
                        JobConfig jobinfo = job.getJobinfo();
                        client.close();
                        injector = Guice.createInjector(new JenkinsWsClientGuiceModule(new URL(
                            jkInfo.getJenkinsUrl()), jkInfo.getJenkinsUsername(), jkInfo.getJenkinsPassword()));
                        client = injector.getInstance(JenkinsClient.class);
                        if (CommConstants.STAGE_CODE.deploy == adStage.getStageCode()) {
                            jobinfo.getBuilders().get(0).setCommand(CommConstants.mergeDeployScript(bsParaDetailDAO, "DEPLOY_SCRIPT", envId + "_" + envType, adVirtualEnvironmentDAO, adDcosDeployDtlDAO, adStage.getAdBranch().getOriginPath(), adVirtualEnvironment, adDcosDeployDtlList, adGroupImpl, dcosDeployInfoDAO, adProject));
                        } else if ((CommConstants.STAGE_CODE.restart == adStage.getStageCode())) {
                            jobinfo.getBuilders().get(0).setCommand(CommConstants.mergeDeployScript(bsParaDetailDAO, "RESTART_SCRIPT", envId + "_" + envType, adVirtualEnvironmentDAO, adDcosDeployDtlDAO, adStage.getAdBranch().getOriginPath(), adVirtualEnvironment, adDcosDeployDtlList, adGroupImpl, dcosDeployInfoDAO, adProject));
                        }
                        xml = JobConfigUtil.toXMLString(jobinfo);
                        job = new JobImpl(adStage.getJenkinsJobName());
                        System.out.print(xml);
                        xml = "<?xml version='1.0' encoding='UTF-8'?>" + xml;
                        client.updateJob(job, xml);
                        client.close();
                    }
                }
            }
        }

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
        sysBranchPojoExt.setOriginPath(adBranch.getOriginPath());
        if (org.apache.commons.lang.StringUtils.isNotEmpty(adBranch.getBuildFileType())) {
            String[] buildFileTypes = adBranch.getBuildFileType().split(",");
            Map<String, Boolean> typeMap = new HashMap<String, Boolean>();
            for (String buildFile : buildFileTypes) {
                typeMap.put(buildFile, true);
            }
            sysBranchPojoExt.setBuildFileTypes(typeMap);
        }
        adStaticDataList = systemDeployImpl.qryDefaultStage();
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
                        if (org.apache.commons.lang.StringUtils.isEmpty(adStage.getStageConfig())) {
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
        if (org.apache.commons.lang.StringUtils.isNotEmpty(adBranch.getEnvType())) {
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

}
