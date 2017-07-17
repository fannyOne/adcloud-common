package com.asiainfo.comm.module.deploy.dao.impl;

import com.asiainfo.comm.common.pojo.pojoExt.*;
import com.asiainfo.comm.module.build.dao.impl.*;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.models.query.QAdDcosDeployInfo;
import com.asiainfo.comm.module.models.query.QAdVirtualEnvironment;
import com.asiainfo.comm.module.common.AdParaDetailDAO;
import com.asiainfo.comm.module.common.AdStaticDataDAO;
import com.avaje.ebean.Ebean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by weif on 2016/7/24.
 */
@Component
public class SystemDeployDAO {
    @Autowired
    AdProjectDAO adProjectDAO;
    @Autowired
    AdBranchDAO adBranchDAO;
    @Autowired
    AdStageDAO adStageDAO;
    @Autowired
    AdUserDAO adUserDAO;
    @Autowired
    AdJenkinsInfoDAO adJenkinsInfoDAO;
    @Autowired
    AdPipeLineStateDAO adPipeLineStateDAO;
    @Autowired
    AdStaticDataDAO bsStaticDataDAO;
    @Autowired
    AdVirtualEnvironmentDAO adVirtualEnvironmentDAO;
    @Autowired
    AdDcosDeployInfoDAO adDcosDeployInfoDAO;
    @Autowired
    AdParaDetailDAO bsParaDetailDAO;
    @Autowired
    private AdDcosBranchRelateDAO adDcosBranchRelateDAO;
    @Autowired
    private AdVirtualBranchRelateDAO adVirtualBranchRelateDAO;

    /**************************************改造方法**********************************************/
    /**
     * 涉及 AD_BRANCH，AD_STAGE,AD_PIPELINE_STATE
     *
     * @param sysReformProjectPojoExt 传递的项目信息
     * @param adProject               项目相信信息
     */
    public void addReformSystemDeploy(SysReformProjectPojoExt sysReformProjectPojoExt, AdProject adProject) {
        Ebean.execute(() -> {
            AdBranch adBranch;                                                                                          //分支
            AdPipeLineState adPipeLineState;                                                                            //流水
            AdStage adStage;                                                                                            //节点
            List<AdStage> adStageList;                                                                                  //获取所有节点
            int i = 0;                                                                                                  //判断节点步骤用
            int is_spec = 0;
            long dvId = 0L;//判断是开始还是结束节点
            AdUser adUser = adUserDAO.getUserById(1);                                                             //根据id查询用户
            AdStaticData adStaticData = bsStaticDataDAO.qryStaticDataByCodeValue("JENKINS_SET", "USERJK");
            if (sysReformProjectPojoExt.getObj() != null) {                                                             //分支存在
                if (sysReformProjectPojoExt.getObj() != null) {
                    for (SysReformBranchPojoExt sysReformBranchPojoExt : sysReformProjectPojoExt.getObj()) {            //遍历所有分支
                        if (sysReformBranchPojoExt.getStages() == null || sysReformBranchPojoExt.getStages().length < 1) {  //分支不存在节点，直接跳过
                            continue;
                        }
                        //获取envID，形式 ID_vm or Id_dcos
                        adBranch = new AdBranch();                                                                      //**********开始设置分支信息
                        adStageList = new ArrayList<AdStage>();                                                         //保存所有节点信息
                        adBranch.setBranchName(sysReformBranchPojoExt.getBranchName());                                 //设置分支名称
                        adBranch.setBranchDesc(sysReformBranchPojoExt.getBranchDesc());                                 //设置分支描述
                        adBranch.setBranchType(Integer.parseInt(sysReformBranchPojoExt.getBranchType()));               //设置分支类型
                        adBranch.setBranchPath(sysReformBranchPojoExt.getBranchPath());                                 //设置分支路径
                        adBranch.setTriggerBranch(Long.parseLong(sysReformBranchPojoExt.getTriggerBranch()));           //
                        adBranch.setBuildCron(sysReformBranchPojoExt.getBuildCron());
                        adBranch.setEnvType("none");

                        //设置环境类型
                        adBranch.setAdProject(adProject);                                                               //设置项目信息
                        if (StringUtils.isNotEmpty(sysReformBranchPojoExt.getOriginPath())) {                           //远端路径存在，设置远端路径
                            adBranch.setOriginPath(sysReformBranchPojoExt.getOriginPath());
                        }
                        if (StringUtils.isNotEmpty(sysReformBranchPojoExt.getBuildFileType())) {                        //构建文件类型存在，设置构建文件类型
                            adBranch.setBuildFileType(sysReformBranchPojoExt.getBuildFileType());
                        }
                        adBranch.setDoneDate(new Date());                                                               //设置完成日期
                        adBranch.setState(1L);                                                                          //设置状态
                        if (StringUtils.isNotEmpty(sysReformBranchPojoExt.getJkId())) {                                 //jkId存在，根据jkId查询jenkinsinfo设置jkId
                            adBranch.setAdJenkinsInfo(adJenkinsInfoDAO.qryByJkId(Long.valueOf(sysReformBranchPojoExt.getJkId())));
                        } else {                                                                                        //jkId不存在，bsStaticData存在，
                            if (adStaticData != null) {                                                                 //判断codeName，codeName存在，根据codename查询
                                if (StringUtils.isNotEmpty(adStaticData.getCodeName())) {
                                    AdJenkinsInfo jenkinsInfo = adJenkinsInfoDAO.qryByJkId(Long.valueOf(adStaticData.getCodeName()));
                                    if ("mesos".equals(jenkinsInfo.getJenkinsMode())) {//判断是否是老版group
                                        if (bsStaticDataDAO.qryStaticDataByCodeValue("OLD_GROUP", String.valueOf(adProject.getAdGroup().getGroupId())) != null) {
                                            AdStaticData adStaticDataSecondUserjk = bsStaticDataDAO.qryStaticDataByCodeValue("JENKINS_SET", "SECONDUSERJK");
                                            adBranch.setAdJenkinsInfo(adJenkinsInfoDAO.qryByJkId(Long.valueOf(adStaticDataSecondUserjk.getCodeName())));
                                        } else {
                                            adBranch.setAdJenkinsInfo(adJenkinsInfoDAO.qryByJkId(Long.valueOf(adStaticData.getCodeName())));
                                        }
                                        ;
                                    } else {
                                        adBranch.setAdJenkinsInfo(adJenkinsInfoDAO.qryByJkId(Long.valueOf(adStaticData.getCodeName())));
                                    }
                                }
                            }
                        }
                        if (StringUtils.isNotEmpty(sysReformBranchPojoExt.getEnvId()) && sysReformBranchPojoExt.getEnvId().indexOf("_") > 0) {
                            String[] envId = sysReformBranchPojoExt.getEnvId().split("_");
                            dvId = Long.parseLong(envId[0]);
                            adBranch.setEnvId(Long.parseLong(envId[0]));                                                    //设置ID
                            if ("vm".equals(envId[1])) {
                                adBranch.setEnvType("vm");
                                adBranch = adBranchDAO.saveAdBranch(adBranch);
                                AdVirtualEnvironment adVirtualEnvironment = adVirtualEnvironmentDAO.qryVmById(Long.parseLong(envId[0]));
                                adVirtualBranchRelateDAO.addVirtualBranchRelate(adBranch, adVirtualEnvironment, 1L, new Date());
                            } else if ("dcos".equals(envId[1])) {
                                adBranch.setEnvType("dcos");
                                adBranch = adBranchDAO.saveAdBranch(adBranch);
                                AdDcosDeployInfo adDcosDeployInfo = adDcosDeployInfoDAO.qryDcosDeployInfoById(Long.parseLong(envId[0]));
                                adDcosBranchRelateDAO.addDcosBranchRelate(adBranch, adDcosDeployInfo, 1L, new Date());
                            } else {
                                adBranch = adBranchDAO.saveAdBranch(adBranch);
                            }
                        } else {
                            adBranch = adBranchDAO.saveAdBranch(adBranch);
                        }
                        //**********设置分支信息结束存储分支
                        adPipeLineState = new AdPipeLineState();                                                        //设置流水信息
                        adPipeLineState.setAdProject(adProject);                                                        //设置项目信息
                        adPipeLineState.setAdBranch(adBranch);                                                          //设置分支信息
                        adPipeLineState.setBranchState(1);                                                              //设置分支状态
                        adPipeLineState.setState(1);                                                                    //设置节点状态
                        adPipeLineState.setBuildType(Integer.parseInt(sysReformBranchPojoExt.getBranchType()));         //设置构建类型
                        adPipeLineStateDAO.savePipeLineState(adPipeLineState);                                          //存储流水
                        SysStagePojoExt[] sysStagePojoExts = sysReformBranchPojoExt.getStages();                        //获取节点
                        i = 0;                                                                                          //遍历节点，设置步骤号，判断是第几步
                        is_spec = 0;                                                                                    //步骤，1-开始，2-结束
                        List<SysStagePojoExt> sysStagePojoExtList = new ArrayList<SysStagePojoExt>();
                        for (SysStagePojoExt sysStagePojoExt : sysStagePojoExts) {                                      //遍历节点信息
                            if (sysStagePojoExt.isCheck()) {                                                            //如果节点被勾选
                                sysStagePojoExtList.add(sysStagePojoExt);                                               //存入到list中
                            }
                        }
                        for (SysStagePojoExt sysStagePojoExt : sysStagePojoExtList) {                                   //遍历所有被选中的节点
                            i++;                                                                                        //使用后节点+1
                            is_spec = 0;                                                                                //默认步骤是0
                            adStage = new AdStage();                                                                    //设置节点信息
                            adStage.setJenkinsJobName(sysStagePojoExt.getJkJobName());                                  //
                            adStage.setState(1L);
                            if (StringUtils.isNotEmpty(sysStagePojoExt.getStagecode()))                                 //步骤对应码
                                adStage.setStageCode(Integer.parseInt(sysStagePojoExt.getStagecode()));
                            adStage.setStep(i);                                                                         //设置步骤序号
                            if (i == 1) {                                                                               //i==1，是第一步
                                is_spec = 1;
                            }
                            if (i == sysStagePojoExtList.size()) {                                                      //遍历所有节点之后，是最后一步
                                is_spec = 2;
                            }
                            adStage.setIsSpec(is_spec);
                            adStage.setAdBranch(adBranch);
                            adStage.setAdPipeLineState(adPipeLineState);
                            adStage.setDealResult(0);
                            adStage.setCreateDate(new Date());
                            adStage.setJobSchedule(sysStagePojoExt.getSpec());
                            adStage.setStageConfig(sysStagePojoExt.getStageConfig());
                            adStageList.add(adStage);
                        }
                        adStageDAO.saveAdStages(adStageList);                                                           //
                    }
                }
            }
        });
    }

    /***********************************************************************************************************/
    /**
     * 根据envId获取环境信息
     *
     * @param sysReformBranchPojoExt 分支信息
     * @return 返回SysEnvConfigExt
     */

    public SysEnvConfigExt qryAdEnvPojoExt(SysReformBranchPojoExt sysReformBranchPojoExt) {
        SysEnvConfigExt sysEnvConfigExt = new SysEnvConfigExt();
        if (sysReformBranchPojoExt.getEnvId() != null) {
            String[] envId = sysReformBranchPojoExt.getEnvId().split("_");                                            //对envId进行分割，获得 id和类型，格式 id_type
            if ("vm".equals(envId[1]) || "virtual".equals(envId[1])) {                                                                                     //如果是vm
                sysEnvConfigExt.setEnvType("vm");                                                                           //设置环境类型为vm
                //根据id查询vm信息
                AdVirtualEnvironment virtualEnvironment = new QAdVirtualEnvironment().virtualId.eq(Long.parseLong(envId[0])).findUnique();
                SysVmDataExt sysVmDataExt = new SysVmDataExt();                                                             //开始***设置vm信息
                sysVmDataExt.setVmId(virtualEnvironment.getVirtualId());
                sysVmDataExt.setServerUrl(virtualEnvironment.getServerUrl());
                sysVmDataExt.setServerUsername(virtualEnvironment.getServerUsername());
                sysVmDataExt.setServerPassword(virtualEnvironment.getServerPassword());
                sysVmDataExt.setFileName(virtualEnvironment.getFileName());
                sysVmDataExt.setFilePath(virtualEnvironment.getFilePath());
                sysVmDataExt.setSourceAddress(virtualEnvironment.getSourceAddress());
                sysVmDataExt.setDestAddress(virtualEnvironment.getDestinationAddress());
                sysVmDataExt.setPackName(virtualEnvironment.getPackageName());                                              //结束**设置vm信息
                sysEnvConfigExt.setVmData(sysVmDataExt);
            } else {
                sysEnvConfigExt.setEnvType("dcos");
                long infoId = Long.parseLong(envId[0]);//如果是dcos
                QAdDcosDeployInfo qAdDcosDeployInfo = new QAdDcosDeployInfo();
                qAdDcosDeployInfo.deployInfoId.eq(infoId);
                AdDcosDeployInfo adDcosDeployInfo = qAdDcosDeployInfo.findUnique();
                SysDcosDataExt sysDcosDataExt = new SysDcosDataExt();
                SysDcosDataExt[] sysDcosDataExts = new SysDcosDataExt[1];
                if (adDcosDeployInfo.getAppid() != null) {
                    sysDcosDataExt.setAppid(adDcosDeployInfo.getAppid());
                }
                if (adDcosDeployInfo.getPackageName() != null) {
                    sysDcosDataExt.setPackageName(adDcosDeployInfo.getPackageName());
                }

                sysDcosDataExts[0] = sysDcosDataExt;
                sysEnvConfigExt.setDcosData(sysDcosDataExts);
            }
        }//存储环境信息

        return sysEnvConfigExt;
    }

}
