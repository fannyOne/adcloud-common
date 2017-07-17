package com.asiainfo.comm.module.deploy.controller;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.dataModel.BranchInfoPara;
import com.asiainfo.comm.common.pojo.pojoExt.*;
import com.asiainfo.comm.common.pojo.pojoMaster.BranchNameListPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.BranchNamePojo;
import com.asiainfo.comm.common.pojo.pojoMaster.dcosEvnAppIdPojo;
import com.asiainfo.util.HttpUtil;
import com.asiainfo.util.JsonUtil;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.comm.module.build.dao.impl.AdBranchDAO;
import com.asiainfo.comm.module.build.dao.impl.AdStageDAO;
import com.asiainfo.comm.module.build.dao.impl.AdVirtualEnvironmentDAO;
import com.asiainfo.comm.module.build.service.impl.AdDcosDeployDtlImpl;
import com.asiainfo.comm.module.build.service.impl.AdDcosDeployInfoImpl;
import com.asiainfo.comm.module.build.service.impl.AdVirtualEnvironmentImpl;
import com.asiainfo.comm.module.deploy.dao.impl.AdDcosBranchRelateDAO;
import com.asiainfo.comm.module.deploy.dao.impl.AdDcosDeployInfoDAO;
import com.asiainfo.comm.module.deploy.dao.impl.AdVirtualBranchRelateDAO;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Created by zhangpeng on 2016/10/19.
 */
@RestController
@lombok.extern.slf4j.Slf4j
@RequestMapping("/evn")
public class EvnSetController {
    @Autowired
    AdDcosDeployInfoDAO adDcosDeployInfoDAO;
    @Autowired
    AdDcosDeployDtlImpl adDcosDeployDtlImpl;
    @Value("${dcosApi.url}")
    String dcosApiUrl;
    @Value("${pagesize}")
    private int pageSize;
    @Autowired
    private AdVirtualEnvironmentDAO adVirtualEnvironmentDAO;
    @Autowired
    private AdBranchDAO branchDAO;
    @Autowired
    private AdStageDAO adStageDAO;
    @Autowired
    private AdDcosDeployInfoImpl adDcosDeployInfoImpl;
    @Autowired
    private AdUserRoleRelImpl userRoleRelImpl;
    @Autowired
    private AdVirtualBranchRelateDAO adVirtualBranchRelateDAO;
    @Autowired
    private AdDcosBranchRelateDAO adDcosBranchRelateDAO;
    @Autowired
    private AdVirtualEnvironmentImpl adVirtualEnvironmentImpl;

    @RequestMapping(value = "/qrySingleVmEnv", produces = "application/json")
    public String qrySingleVmEnv(@RequestParam Map map) {
        String ret = "";
        long projectId = 0L;
        Long virtualId = 0L;
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            if (map != null) {
                if (map.containsKey("projectId") && StringUtils.isNotEmpty(
                    (String) map.get("projectId"))) {
                    projectId = Long.valueOf((String) map.get("projectId"));
                } else {
                    throw new Exception("项目编号不正确");
                }
                if (map.containsKey("virtualId") && StringUtils.isNotEmpty(
                    (String) map.get("virtualId"))) {
                    virtualId = Long.valueOf((String) map.get("virtualId"));
                } else {
                    throw new Exception("环境名称不正确");
                }
            }
            AdVirtualEnvironmentPojo adVirtualEnvironmentPojo = adVirtualEnvironmentImpl.getSingleVmEvn(projectId, virtualId);
            if (adVirtualEnvironmentPojo != null) {
                adVirtualEnvironmentPojo.setServerPassword(null);// 密码替换为*号，默认8位字符串
                adVirtualEnvironmentPojo.setBranchs(adVirtualEnvironmentImpl.qryEnvRlateBranchByRegion(projectId, adVirtualEnvironmentPojo.getRegion(), "vm", virtualId));
                retMap.put("evn", adVirtualEnvironmentPojo);
            }
        } catch (Exception e) {
            log.error("*********qrySingleVmEnv*error", e);
            retMap.put("retCode", "500");
            retMap.put("m", e.getMessage());
        }
        ret = JsonUtil.mapToJson(retMap);
        return ret;
    }

    @RequestMapping(value = "/updateVmEnv", produces = "application/json")
    public String updateVmEnv(@RequestBody vmEvnPojoExt req) {
        String ret = "";
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            adVirtualEnvironmentImpl.updateVmEvnById(req);
            retMap.put("retCode", "200");
        } catch (Exception e) {
            retMap.put("retCode", "500");
            retMap.put("m", e.getMessage());
        }
        ret = JsonUtil.mapToJson(retMap);
        return ret;
    }

    /**
     * 新增虚机环境
     *
     * @param map
     * @return 新增结果
     */
    @RequestMapping(value = "/addReformVmEnv", produces = "application/json")
    public String addReformVmEnv(@RequestParam Map map) {
        String ret = "";                                        //返回结果
        String serverUrl = "";                                  //环境地址
        String serverUsername = "";                             //用户名
        String serverPassword = "";                             //密码
        String filePath = "";                                   //脚本路径
        String fileName = "";                                   //脚本名字
        Long projectId = 0L;                                    //应用Id
        String packName = "";                                   //发布包名称
        String sourceAddress = "";                              //源地址
        String destAddress = "";                                //目标地址
        String branchIds = "";                                  //对应的分支Id
        String envName = "";                                    //虚机名字
        Integer region = 0;

        Map<String, Object> retMap = new HashMap<String, Object>();  //存储结果

        try {
            if (map != null) {                                  //map存在，获得信息
                if (map.containsKey("vmprj") && StringUtils.isNotEmpty(map
                    .get("vmprj").toString())) {                //设置应用id
                    projectId = Long.valueOf((String) map.get("vmprj"));
                } else {
                    throw new Exception("请选择应用");
                }
                if (map.containsKey("serverUrl") && StringUtils.isNotEmpty
                    (map.get("serverUrl").toString())) {        //设置服务地址
                    serverUrl = (String) map.get("serverUrl");
                } else {
                    throw new Exception("请输入主机ip");
                }
                if (map.containsKey("serverUsername") && StringUtils
                    .isNotEmpty(map.get("serverUsername").toString())) {//设置用户名
                    serverUsername = (String) map.get("serverUsername");
                } else {
                    throw new Exception("请输入主机用户名");
                }
                if (map.containsKey("serverPassword") && StringUtils
                    .isNotEmpty(map.get("serverPassword").toString())) {//设置密码
                    serverPassword = (String) map.get("serverPassword");
                } else {
                    throw new Exception("请输入主机密码");
                }
                if (map.containsKey("filePath") && StringUtils.isNotEmpty(map
                    .get("filePath").toString())) {          //设置文件路径
                    filePath = (String) map.get("filePath");
                } else {
                    throw new Exception("请输入脚本路径");
                }
                if (map.containsKey("fileName") && StringUtils.isNotEmpty(map
                    .get("fileName").toString())) {          //设置脚本名称
                    fileName = (String) map.get("fileName");
                } else {
                    throw new Exception("请输入脚本名");
                }
                if (map.containsKey("packName") && StringUtils.isNotEmpty(map
                    .get("packName").toString())) {          //设置部署包名称
                    packName = (String) map.get("packName");
                } else {
                    throw new Exception("请输入部署包名称");
                }
                if (map.containsKey("sourceAddress") && StringUtils
                    .isNotEmpty(map.get("sourceAddress").toString())) {//设置源地址
                    sourceAddress = (String) map.get("sourceAddress");
                } else {
                    throw new Exception("请输入源地址");
                }
                if (map.containsKey("destAddress") && StringUtils.isNotEmpty
                    (map.get("destAddress").toString())) {    //设置，目标地址
                    destAddress = (String) map.get("destAddress");
                } else {
                    throw new Exception("请输入目的地址");
                }
                if (map.containsKey("region") && StringUtils.isNotEmpty(map
                    .get("region").toString())) {              //设置所属域
                    region = Integer.parseInt(map.get("region").toString());
                } else {
                    throw new Exception("请输入所属域");
                }
                if (map.containsKey("envName") && StringUtils.isNotEmpty(map
                    .get("envName").toString())) {            //设置环境名称
                    envName = map.get("envName").toString();
                } else {
                    throw new Exception("请输入虚机名称");
                }

                String[] branchIdArray = null;
                if (map.containsKey("branchIds")) {
                    //branchIds存在
                    if (StringUtils.isNotEmpty(map.get("branchIds").toString
                        ())) {                                        //分割存入数组中
                        branchIds = map.get("branchIds").toString();
                        branchIdArray = branchIds.split(",");
                    }
                }
                /**
                 * 存储环境信息
                 * Ebean save之后可以获得ID，save之后直接返回对象
                 */
                AdVirtualEnvironment adVirtualEnvironment =
                    adVirtualEnvironmentDAO.addReformVmEnvSet(projectId,
                        serverUsername, envName, serverPassword, serverUrl,
                        filePath, fileName, packName, sourceAddress,
                        destAddress, region);

                String reStr = adVirtualEnvironment.getVirtualId() + "_vm";
                //返回环境id
                if (branchIdArray != null && branchIdArray.length > 0) {
                    for (String branchId : branchIdArray) {
                        //数组不为空，遍历所有分支id
                        AdBranch adBranch = branchDAO.qryById(Long.parseLong
                            (branchId));
                        //根据id查询获得分支信息，存入虚机流水对应表中
                        adVirtualBranchRelateDAO.addVirtualBranchRelate
                            (adBranch, adVirtualEnvironment, 1L, new Date());
                        //新建对应关系
                    }
                }
                retMap.put("retCode", "200");
                retMap.put("m", reStr);
            }
        } catch (Exception e) {
            retMap.put("retCode", "500");
            retMap.put("m", e.getMessage());
        }
        ret = JsonUtil.mapToJson(retMap);
        return ret;
    }

    @RequestMapping(value = "/qrySingleBranch", produces = "application/json")
    public Pojo qrySingleBranch(@RequestParam Map map) {
        List<Long> branchArray = Lists.newArrayList();
        List<AdStage> adStageList;
        BranchNamePojo branchNamePojo = new BranchNamePojo();
        List<BranchNameListPojo> branchNameListPojos = new ArrayList<>();
        List<AdBranch> adBranches = branchDAO.getEnvsBySysId(Long.valueOf(
            (String) map.get("projectId")));
        for (AdBranch branch : adBranches) {
            branchArray.add(branch.getBranchId());
        }
        adStageList = adStageDAO.qryBranchIdByCode(branchArray);
        for (AdStage adStage : adStageList) {
            BranchNameListPojo branchNameListPojo = new BranchNameListPojo();
            branchNameListPojo.setBranchId(adStage.getAdBranch().getBranchId());
            branchNameListPojo.setBranchName(adStage.getAdBranch()
                .getBranchDesc());
            branchNameListPojos.add(branchNameListPojo);
        }
        branchNamePojo.setBranchName(branchNameListPojos);
        return branchNamePojo;
    }

    @RequestMapping(value = "/qrySingleDcosEnv", produces = "application/json")
    public String qrySingleDcosEnv(@RequestParam Map map) {
        String ret = "";
        Long projectId = 0L;
        long infoId = 0L;
        dcosEvnAppIdPojo dcosEvnAppIdPojo = new dcosEvnAppIdPojo();
        dcosEnvInfoPojoExt req = new dcosEnvInfoPojoExt();
        List<AppIdinfoPojoExt> appIdinfoPojoExts = Lists.newArrayList();
        try {
            if (map != null) {
                if (map.containsKey("projectId") && StringUtils.isNotEmpty(
                    (String) map.get("projectId"))) {
                    projectId = Long.valueOf((String) map.get("projectId"));
                } else {
                    throw new Exception("项目编号不正确");
                }
                if (map.containsKey("infoId") && StringUtils.isNotEmpty(
                    (String) map.get("infoId"))) {
                    infoId = Long.valueOf((String) map.get("infoId"));
                } else {
                    throw new Exception("环境名称不正确");
                }
            }
            System.out.println("查询DCos");
            AdDcosDeployInfo adDcosDeployInfo = adDcosDeployInfoDAO.getSingleDcosEvn(projectId, infoId);
            if (adDcosDeployInfo != null) {
                String groupIdExt = adDcosDeployInfo.getAdProject().getAdGroup().getGroupIdExt();
                List<AdDcosDeployDtl> adDcosDeployDtl = adDcosDeployDtlImpl.qryDcosDeployDtlByDcosInfoId(adDcosDeployInfo.getDeployInfoId());
                if (adDcosDeployDtl != null && !adDcosDeployDtl.isEmpty()) {
                    for (AdDcosDeployDtl adDcosDeployDtl1 : adDcosDeployDtl) {
                        AppIdinfoPojoExt appIdin = new AppIdinfoPojoExt();
                        appIdin.setAppid(adDcosDeployDtl1.getAppid());
                        appIdin.setAppName(adDcosDeployDtl1.getAppid());
                        appIdin.setPackageName(adDcosDeployDtl1.getPackageName());
                        appIdin.setPriorityNum(String.valueOf
                            (adDcosDeployDtl1.getPriorityNum()));

                        if (!StringUtils.isEmpty(groupIdExt)) {
                            JSONObject jsonResult = new JSONObject();
                            Map param = new HashMap<String, String>();
                            try {
                                param.put("appId", adDcosDeployDtl1.getAppid());
                                jsonResult = HttpUtil.httpGet(dcosApiUrl, param);
                                JSONObject entity = (JSONObject) ((JSONArray) jsonResult.get("entity")).get(0);
                                String appName = (String) entity.get("appName");
                                JSONObject deploy = new JSONObject((String) entity.get("deploy"));
                                JSONObject app_origin_detail = (JSONObject) deploy.get("app_origin_detail");
                                String fileName = (String) app_origin_detail.get("filename");

                                if (!StringUtils.isEmpty(appName)) {
                                    appIdin.setAppName(appName);
                                }
                                if (!StringUtils.isEmpty(fileName)) {
                                    appIdin.setPackageName(fileName);
                                }

                            } catch (Exception e) {
                            }
                        }
                        appIdinfoPojoExts.add(appIdin);
                    }
                }
                req.setDcosFtpUrl(adDcosDeployInfo.getDcosFtpUrl());
                req.setDcosFtpPath(adDcosDeployInfo.getDcosFtpPath());
                req.setDcosFtpPort(adDcosDeployInfo.getDcosFtpPort());
//                req.setDcosFtpPassword(adDcosDeployInfo.getDcosFtpPassword());
                req.setDcosFtpPassword(null);// 密码替换为*号，默认8位字符串
                req.setDcosFtpUsername(adDcosDeployInfo.getDcosFtpUsername());
                req.setDocsServerUrl(adDcosDeployInfo.getDocsServerUrl());
                req.setDocsUserName(adDcosDeployInfo.getDocsUserName());
                //req.setBranchDesc(adDcosDeployInfo.getAdBranch().getBranchDesc());
                req.setDeployInfoId(adDcosDeployInfo.getDeployInfoId());
                req.setProjectId(adDcosDeployInfo.getAdProject().getProjectId());
                req.setProjectName(adDcosDeployInfo.getAdProject().getProjectName());
                req.setEnvName(adDcosDeployInfo.getEnvName());
                req.setRegion(adDcosDeployInfo.getRegion());
//                req.setDocsUserPassword(adDcosDeployInfo
// .getDocsUserPassword());
                req.setDocsUserPassword(null);// 密码替换为*号，默认8位字符串
                req.setAppids(appIdinfoPojoExts);
                req.setBranchCheck(adVirtualEnvironmentImpl.qryEnvRlateBranchByRegion(projectId, adDcosDeployInfo.getRegion(), "dcos", infoId));

            }
        } catch (Exception e) {
            req.setM(e.getMessage());
        }
        dcosEvnAppIdPojo.setDcosEnvInfoPojoExt(req);
        return JsonpUtil.modelToJson(dcosEvnAppIdPojo);
    }

    @RequestMapping(value = "/qryDcosAllInfo", produces = "application/json")
    public String qryDcosAllInfo(@RequestParam Map map) {
        String ret = "";
        String projects = "";
        int pageNum = 0;
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            BranchInfoPara branchInfoPara = new BranchInfoPara(map, projects,
                pageNum).invoke();
            pageNum = branchInfoPara.getPageNum();
            projects = branchInfoPara.getProjects();
            /*if (map != null) {
                if (map.containsKey("pageNum") && StringUtils.isNotEmpty(
                (String) map.get("pageNum"))) {
                    pageNum = Integer.parseInt((String) map.get("pageNum"));
                } else {
                    throw new Exception("页数不正确");
                }
                if (map.containsKey("projectId") && StringUtils.isNotEmpty(
                (String) map.get("projectId"))) {
                    projects = (String) map.get("projectId");
                } else {
                    throw new Exception("项目名不正确");
                }
            }*/
            if (branchInfoPara.getPageNum() > 0) {
                pageNum = pageNum - 1;
            }
            String[] projectArray = projects.split(",");

        /* 资源隔离，权限验证 */
            if (!userRoleRelImpl.verifyPurview("projectId", projectArray)) {
                return null;
            }
        /* 资源隔离，权限验证 Over */
            List<dcosEnvInfoPojoExt> dcosEnvInfoPojoExts = new ArrayList<>();
            List<AdDcosDeployInfo> adDcosDeployInfos = adDcosDeployInfoImpl.getAllDcosInfo(pageNum, pageSize, projectArray);
            long count = adDcosDeployInfoImpl.getDcosConut(projectArray);
            for (AdDcosDeployInfo adDcosDeployInfo : adDcosDeployInfos) {
                StringBuilder appIds = new StringBuilder();
                dcosEnvInfoPojoExt dcosEnv = new dcosEnvInfoPojoExt();
                dcosEnv.setProjectId(adDcosDeployInfo.getAdProject().getProjectId());
                dcosEnv.setDeployInfoId(adDcosDeployInfo.getDeployInfoId());
                dcosEnv.setDcosFtpUrl(adDcosDeployInfo.getDcosFtpUrl());
                dcosEnv.setProjectName(adDcosDeployInfo.getAdProject().getProjectName());
                dcosEnv.setBranchDesc(adDcosDeployInfo.getEnvName());
                dcosEnv.setDocsServerUrl(adDcosDeployInfo.getDocsServerUrl());
                dcosEnv.setDcosFtpPath(adDcosDeployInfo.getDcosFtpPath());
                dcosEnv.setEnvName(adDcosDeployInfo.getEnvName());
                dcosEnv.setRegion(adDcosDeployInfo.getRegion());
                List<AdDcosDeployDtl> adDcosDeployDtlList = adDcosDeployDtlImpl.qryDcosDeployDtlByDcosInfoId(adDcosDeployInfo.getDeployInfoId());
                for (int i = 0; i < adDcosDeployDtlList.size(); i++) {
                    appIds.append(adDcosDeployDtlList.get(i).getAppid());
                    if (i == adDcosDeployDtlList.size() - 1) //若是最后一个beak
                    {
                        break;
                    }
                    appIds.append(",");
                }
                dcosEnv.setM(appIds.toString());
                dcosEnvInfoPojoExts.add(dcosEnv);
            }
            retMap.put("total", count);
            retMap.put("env", dcosEnvInfoPojoExts);
        } catch (Exception e) {
            log.error("****e=", e);
            retMap.put("retCode", "500");
            retMap.put("m", e.getMessage());
        }
        ret = JsonUtil.mapToJson(retMap);
        return ret;
    }

    @RequestMapping(value = "/updateDcosEnv", produces = "application/json")
    public String updateDcosEnv(@RequestBody dcosEnvPojoExt req) {
        String ret = "";
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {

            adDcosDeployInfoImpl.updateDcosEvnById(req);
            retMap.put("retCode", "200");
        } catch (Exception e) {
            log.error("修改环境配置" + e);
            retMap.put("retCode", "500");
            retMap.put("m", e.getMessage());
        }
        ret = JsonUtil.mapToJson(retMap);
        return ret;
    }

    /**
     * @param map
     * @return
     */
    @RequestMapping(value = "/deleteDcosSingleInfo", produces = "application/json")
    public String deleteDcosSingleInfo(@RequestParam Map map) {
        String ret = "";
        long deployInfoId = Long.valueOf(map.get("dcosId").toString());
        Map<String, Object> retMap = new HashMap<String, Object>();
        boolean flag = branchDAO.qryBranchByEnvId(deployInfoId, "dcos");//查找关联流水
        if (flag) {
            retMap.put("retCode", "500");
            retMap.put("m", "存在流水，删除失败");
        } else {
            int results = adDcosDeployInfoImpl.deleteReformDcosSigleInfo(deployInfoId);
            if (results == 0) {
                retMap.put("retCode", "200");
                retMap.put("m", "删除成功");
            } else {
                retMap.put("retCode", "500");
                retMap.put("m", "删除失败");
            }
        }
        ret = JsonUtil.mapToJson(retMap);
        return ret;
    }

    /**
     * 删除环境,存在关联流水，不删除
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/deleteReformVmSingleInfo", produces = "application/json")
    public String deleteReformVmSingleInfo(@RequestParam Map map) {
        String ret = "";
        long envId = Long.valueOf(map.get("envId").toString());
        Map<String, Object> retMap = new HashMap<String, Object>();
        boolean flag = branchDAO.qryBranchByEnvId(envId, "vm");//查找关联流水
        if (flag) {
            retMap.put("retCode", "500");
            retMap.put("m", "存在流水，删除失败");
        } else {
            int result = adVirtualEnvironmentDAO.deleteSingleInfo(envId); //删除环境
            try {
                if (result == 0) {
                    retMap.put("retCode", "200");
                    retMap.put("m", "删除成功");
                } else {
                    retMap.put("retCode", "500");
                    retMap.put("m", "删除失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ret = JsonUtil.mapToJson(retMap);
        return ret;
    }


    /**************************DCOS改造部分******************************/
    /**
     * @param req 需要新增的DCOS环境信息
     * @return 新建结果
     */
    @RequestMapping(value = "/addReformDcosEnvInfo", produces = "application/json")
    public String addReformDcosEnvInfo(@RequestBody dcosEnvInfoPojoExt req) {
        String ret = "";
        //用于返回结果
        Map<String, Object> retMap = new HashMap<String, Object>();
        //用于将结果转换成json
        try {
            if (req != null && req.getAppids() != null) {
                if (req.getProjectId() == null) {
                    throw new Exception("请选择应用");
                }
                if (StringUtils.isEmpty(req.getEnvName())) {
                    throw new Exception("请输入环境名称");
                }
                if (req.getRegion() == null) {
                    throw new Exception("请选择环境归属域");
                }
                //如果Appids存在
                for (AppIdinfoPojoExt appIdinfoPojoExt : req.getAppids()) {
                    //遍历信息
                    if (StringUtils.isEmpty(appIdinfoPojoExt.getAppid())) {
                        throw new Exception("请输入AppId");
                    }
                    if (StringUtils.isEmpty(appIdinfoPojoExt.getPackageName()
                    )) {
                        throw new Exception("请输入部署包名称");
                    }
                    if (StringUtils.isEmpty(appIdinfoPojoExt.getPriorityNum()
                    )) {
                        throw new Exception("请输入发布优先级,如数字：1,2,3.....");
                    } else {
                        if (!appIdinfoPojoExt.getPriorityNum().matches("[0-9]+")) {
                            throw new Exception("请输入发布优先级,如数字：1,2,3.....");
                        }
                    }
                }

                String[] branchIdArray = null;
                //获得branchIds
                if (StringUtils.isNotEmpty(req.getBranchIds())) {
                    //分割存入数组中
                    branchIdArray = req.getBranchIds().split(",");
                }
                AdDcosDeployInfo adDcosDeployInfo = adDcosDeployInfoImpl.addReformDcosInfo(req);        //新增DCOS环境信息
                if (branchIdArray != null && branchIdArray.length > 0) {
                    for (String branchId : branchIdArray) {
                        //数组不为空，遍历所有分支id
                        AdBranch adBranch = branchDAO.qryById(Long.parseLong(branchId));
                        //根据id查询获得分支信息，存入虚机流水对应表中
                        adDcosBranchRelateDAO.addDcosBranchRelate(adBranch, adDcosDeployInfo, 1L, new Date()); //新建对应关系
                    }
                }
                retMap.put("retCode", "200");
                retMap.put("m", adDcosDeployInfo.getDeployInfoId() + "_dcos");
            } else {
                retMap.put("retCode", "500");
                retMap.put("m", "传入的参数为空");
            }
        } catch (Exception e) {
            retMap.put("retCode", "500");
            retMap.put("m", e.getMessage());
        }
        ret = JsonUtil.mapToJson(retMap);
        return ret;
    }


    @RequestMapping(value = "/qryMoreVmEnv", produces = "application/json")
    public String qryMoreVmEnv(@RequestParam Map map) {
        String ret = "";
        Long projectId = 0L;
        String virtualIds = "";
        String[] virtualIdArray = null;
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            if (map != null) {
                if (map.containsKey("projectId") && StringUtils.isNotEmpty(
                    (String) map.get("projectId"))) {
                    projectId = Long.valueOf((String) map.get("projectId"));
                } else {
                    throw new Exception("项目编号不正确");
                }
                if (map.containsKey("virtualIds") && StringUtils.isNotEmpty(map.get("virtualIds").toString())) {
                    virtualIds = map.get("virtualIds").toString();
                    virtualIdArray = virtualIds.split(",");
                } else {
                    throw new Exception("环境名称不正确");
                }
            }
            List<AdVirtualEnvironment> adVirtualEnvironments = new ArrayList<AdVirtualEnvironment>();
            if (virtualIdArray != null && virtualIdArray.length > 0) {
                for (String virtualId : virtualIdArray) {
                    AdVirtualEnvironment adVirtualEnvironment = adVirtualEnvironmentDAO.getSingleVmEvn(projectId,
                        Long.parseLong(virtualId));
                    if (adVirtualEnvironment != null) {
                        adVirtualEnvironment.setServerPassword(null);// 密码替换为*号，默认8位字符串
                        adVirtualEnvironments.add(adVirtualEnvironment);
                    }
                }
            }
            retMap.put("evn", adVirtualEnvironments);
        } catch (Exception e) {
            retMap.put("retCode", "500");
            retMap.put("m", e.getMessage());
        }
        ret = JsonUtil.mapToJson(retMap);
        return ret;
    }

    /**
     * 查询本应用下相关域的所有流水
     *
     * @param envInfoPojoExt
     * @return
     */
    @RequestMapping(value = "/qryRelationDCOSByRegion", produces = "application/json")
    public String qryRelationDCOSByRegion(@RequestBody dcosEnvInfoPojoExt envInfoPojoExt) {
        String result = "";
        Map<String, Object> retMap = new HashMap<String, Object>();
        if (envInfoPojoExt != null) {
            retMap = adDcosDeployInfoDAO.qryDcosInfoByRegion(envInfoPojoExt); //获得结果map
        }
        result = JsonUtil.mapToJson(retMap);
        return result;
    }

    @RequestMapping(value = "/qryEnvBranchByRegion", produces = "application/json")
    public String qryEnvBranchByRegion(@RequestParam Map map) {
        String ret = "";
        Long projectId = 0L;
        int region = 0;
        List<AdBranchCheckPojoExt> adBranchCheckPojoExtList = new ArrayList<AdBranchCheckPojoExt>();
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            if (map != null) {
                if (map.containsKey("projectId") && StringUtils.isNotEmpty(
                    (String) map.get("projectId"))) {
                    projectId = Long.valueOf((String) map.get("projectId"));
                } else {
                    throw new Exception("项目编号不正确");
                }
                if (map.containsKey("region") && StringUtils.isNotEmpty(map.get("region") + "")) {
                    region = Integer.parseInt(map.get("region") + "");
                } else {
                    throw new Exception("环境归属域不正确");
                }
                adBranchCheckPojoExtList = branchDAO.qryEnvBranchByRegion(projectId, region);
            }
            retMap.put("branch", adBranchCheckPojoExtList);
        } catch (Exception e) {
            retMap.put("retCode", "500");
            retMap.put("m", e.getMessage());
        }
        ret = JsonUtil.mapToJson(retMap);
        return ret;
    }
}
