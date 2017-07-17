package com.asiainfo.comm.module.build.service.impl;

import com.asiainfo.comm.common.pojo.pojoExt.*;
import com.asiainfo.util.CommConstants;
import com.asiainfo.comm.externalservice.jenkins.client.JenkinsClient;
import com.asiainfo.comm.externalservice.jenkins.guice.JenkinsWsClientGuiceModule;
import com.asiainfo.comm.externalservice.jenkins.jobconfig.entity.JobConfig;
import com.asiainfo.comm.externalservice.jenkins.jobs.Job;
import com.asiainfo.comm.externalservice.jenkins.jobs.JobImpl;
import com.asiainfo.comm.module.build.dao.impl.AdBranchDAO;
import com.asiainfo.comm.module.build.dao.impl.AdJenkinsInfoDAO;
import com.asiainfo.comm.module.build.dao.impl.AdPipeLineStateDAO;
import com.asiainfo.comm.module.build.dao.impl.AdStageDAO;
import com.asiainfo.comm.module.deploy.dao.impl.AdDcosDeployInfoDAO;
import com.asiainfo.comm.module.deploy.service.impl.SystemDeployImpl;
import com.asiainfo.comm.module.models.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangpeng on 2016/10/20.
 */
@Component
public class AdDcosDeployInfoImpl {
    @Autowired
    AdDcosDeployInfoDAO adDcosDeployInfoDAO;
    @Autowired
    AdVirtualEnvironmentImpl adVirtualEnvironmentImpl;
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
    AdStageDAO adStageDAO;

    public List<AdDcosDeployInfo> getAllDcosInfo(int pageNum, int pageSize, String[] projects) {
        return adDcosDeployInfoDAO.getAllDcosInfo(pageNum, pageSize, projects);
    }

    public long getDcosConut(String[] projects) {
        return adDcosDeployInfoDAO.getDcosConut(projects);
    }

    public AdDcosDeployInfo qryById(Long envId) {
        return adDcosDeployInfoDAO.qryById(envId);
    }

    public void updateDcosEvnById(dcosEnvPojoExt req) throws Exception {
        if (req != null && req.getObj() != null) {                              //要修改的参数存在
            List<AdDcosDeployDtl> adDcosDeployDtlList = new ArrayList();
            dcosEnvInfoPojoExt envInfoPojoExt = req.getObj()[0];
            for (int i = 0; i < envInfoPojoExt.getAppids().size(); i++) { //appid存在，设置dtl信息
                AdDcosDeployDtl adDcosDeployDtl = new AdDcosDeployDtl();
                adDcosDeployDtl.setPackageName(envInfoPojoExt.getAppids().get(i).getPackageName());
                adDcosDeployDtl.setAppid(envInfoPojoExt.getAppids().get(i).getAppid());
                adDcosDeployDtl.setPriorityNum(Integer.valueOf(envInfoPojoExt.getAppids().get(i)
                    .getPriorityNum()));
                adDcosDeployDtl.setState(1);
                adDcosDeployDtlList.add(adDcosDeployDtl);
            }
            adVirtualEnvironmentImpl.updateStageScript(req.getInfoId(), "dcos", null, adDcosDeployDtlList);
            adDcosDeployInfoDAO.updateDcosEvnById(req);
            List<AdBranch> adBranchList = adBranchDAO.qryBranchRelateByEnvIddcos(req.getInfoId());
            Map<String, List<AdStage>> stringListMap = adStageDAO.QryAdOperationByProjectId(req.getProjectId());
            SysReformProjectPojoExt sysProjectPojoExt = new SysReformProjectPojoExt();
            List<SysReformBranchPojoExt> sysBranchPojoExtList = new ArrayList<SysReformBranchPojoExt>();
            List<AdStage> adStageList;
            if (CollectionUtils.isNotEmpty(adBranchList)) {
                if (stringListMap != null && stringListMap.isEmpty()) {
                    for (AdBranch adBranch : adBranchList) {
                        adStageList = stringListMap.get("" + adBranch.getBranchId());
                        sysBranchPojoExtList.add(initBranchPojo(adStageList, adBranch));
                    }
                }
            }
            sysProjectPojoExt.setProjectId(req.getProjectId() + "");
            if (sysBranchPojoExtList.size() > 0) {
                sysProjectPojoExt.setObj(sysBranchPojoExtList.toArray(new SysReformBranchPojoExt[sysBranchPojoExtList.size()]));
            }
            jenkins.updateReformSystemDeploy(sysProjectPojoExt);

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

    /************************改造方法**************************/
    /**
     * @param req 要增加环境信息
     * @return
     * @throws Exception
     */
    public AdDcosDeployInfo addReformDcosInfo(dcosEnvInfoPojoExt req) throws Exception {
        return adDcosDeployInfoDAO.addReformDcosInfo(req);
    }

    /**
     * @param deployInfoId 要删除的环境Id
     * @return
     */
    public int deleteReformDcosSigleInfo(long deployInfoId) {
        return adDcosDeployInfoDAO.deleteReformDcosSigleInfo(deployInfoId);
    }
}
