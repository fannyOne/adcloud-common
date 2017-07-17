package com.asiainfo.comm.module.deploy.controller;

import com.asiainfo.comm.common.pojo.pojoMaster.AdSystemDeployLogPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.AdSystemdeployLogStrPojo;
import com.asiainfo.util.DateConvertUtils;
import com.asiainfo.util.JsonUtil;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDeployPackageDAO;
import com.asiainfo.comm.module.build.service.impl.AdDcosDeployInfoImpl;
import com.asiainfo.comm.module.deploy.service.impl.SystemDeployLogImpl;
import com.asiainfo.comm.module.deploy.service.impl.VirtualDeployInfoImpl;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.role.service.impl.AdProjectImpl;
import com.asiainfo.comm.module.role.service.impl.AdUserRoleRelImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Created by guojian on 7/26/16.
 */
@RestController
@RequestMapping("/deployLog")
public class SystemDeployLogController {

    @Autowired
    VirtualDeployInfoImpl virtualDeployInfoImpl;
    @Autowired
    AdDcosDeployInfoImpl dcosDeployInfoImpl;
    @Autowired
    AdUserRoleRelImpl userRoleRelImpl;
    @Autowired
    AdProjectImpl projectImpl;
    @Autowired
    AdProjectDeployPackageDAO adProjectDeployPackageDAO;
    @Autowired
    private SystemDeployLogImpl systemDeployLogImpl;

    @RequestMapping(value = "/getLogsBySystemId", produces = "application/json")
    public String getLogsBySystemId(@RequestParam Map map) throws IOException {
        Map<String, Object> hmap = null;
        int pageNum = 0;
        long opId = 0;
        AdSystemDeployLog adSystemDeployLog = new AdSystemDeployLog();
        if (map.get("projectId") != null) {
            adSystemDeployLog.setProjectId(Long.valueOf((String) map.get("projectId")));
        }
        if (map.get("deployType") != null) {
            adSystemDeployLog.setDeployType((String) map.get("deployType"));
        }
        if (map.get("user") != null) {
            opId = Long.valueOf((String) map.get("user"));
        }
        if (map.get("deployResult") != null) {
            adSystemDeployLog.setDeployResult(Integer.valueOf((String) map.get("deployResult")));
        }
        if (map.get("startTime") != null) {
            adSystemDeployLog.setStartTime(DateConvertUtils.StringToDate(map.get("startTime") + " 00:00:00"));
        }
        if (map.get("endTime") != null) {
            adSystemDeployLog.setEndTime(DateConvertUtils.StringToDate(map.get("endTime") + " 24:00:00"));
        }
        if (map.get("page") != null) {
            pageNum = Integer.valueOf((String) map.get("page"));
        }
        if (map.get("envId") != null && !map.get("envId").equals("0")) {
            String envIdStr = (String) map.get("envId");
            String[] envIdStrList = envIdStr.split("_");
            adSystemDeployLog.setEnvId(Long.parseLong(envIdStrList[0]));
            adSystemDeployLog.setHostType(envIdStrList[1].equals("dcos") ? 2 : 1);
        }
        if (map.get("operType") != null && StringUtils.isNotEmpty((String) map.get("operType"))) {
            adSystemDeployLog.setOperType(Integer.valueOf((String) map.get("operType")));
        } else {
            adSystemDeployLog.setOperType(0);
        }

        /* 资源隔离，权限验证 */
//        if (!userRoleRelImpl.verifyPurview("projectId", projectId)) {
//            return null;
//        }
        /* 资源隔离，权限验证 Over */
        if (pageNum > 0) {
            pageNum = pageNum - 1;
        }
        hmap = systemDeployLogImpl.qrySystemDeployLogs(adSystemDeployLog, pageNum, opId);
        String retvalue = JsonUtil.mapToJson(hmap);
        return retvalue;
    }

    @RequestMapping(value = "/project/qryLogById", produces = "application/json")
    public String qryLogById(@RequestParam Map map) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (!map.containsKey("logId")) {
            throw new Exception("lose necessary param");
        }
        Long logId = Long.parseLong((String) map.get("logId"));
        AdSystemDeployLog log = systemDeployLogImpl.qryById(logId);
        if (log == null) {
            return null;
        }
        AdProjectDeployPackage adProjectDeployPackage = adProjectDeployPackageDAO.qryByPackageId(log.getProTagId());
        AdSystemDeployLogPojo poj = new AdSystemDeployLogPojo();
        if (adProjectDeployPackage != null) {
            if (adProjectDeployPackage.getCreateDate() != null)
                poj.setTagCreateDate(sdf.format(adProjectDeployPackage.getCreateDate()));
            String packagePath = "";
            String[] packageName;
            String tagName = "";
            String branchName = "";
            packagePath = adProjectDeployPackage.getPackagePath();
            if (packagePath != null) {
                branchName = adProjectDeployPackage.getAdBranch().getBranchName();
                packageName = packagePath.split("/");
                if (packageName.length > 0) {
                    tagName = branchName + "_" + packageName[packageName.length - 3] + "_" + packageName[packageName.length - 1];
                }
            }
            poj.setTagName(tagName);
            poj.setCommitId(adProjectDeployPackage.getCommitId());
            poj.setProTagId(adProjectDeployPackage.getPackageId() + "");
            poj.setAdProjectDeployPackage(adProjectDeployPackage);
        }
        poj.setDeployType(log.getDeployType());
        poj.setVersion(log.getDeployVersion());
        if (log.getEnvId() != null) {
            if (log.getHostType() == 1) {
                AdVirtualEnvironment virtualEnvironment = virtualDeployInfoImpl.qryById(log.getEnvId());
                poj.setEnvId(log.getEnvId() + "_virtual");
                poj.setEnvName(virtualEnvironment.getVirtualName());
                poj.setEnv_id(log.getEnvId() == null ? "" : (log.getEnvId() + ""));
            } else if (log.getHostType() == 2) {
                AdDcosDeployInfo dcosDeployInfo = dcosDeployInfoImpl.qryById(log.getEnvId());
                poj.setEnvId(log.getEnvId() + "_dcos");
                poj.setEnvName(dcosDeployInfo.getEnvName());
                poj.setEnv_id(log.getEnvId() == null ? "" : (log.getEnvId() + ""));
            }
        }
        poj.setProjectId(log.getProjectId() == null ? "" : (log.getProjectId() + ""));
        poj.setRemarks(log.getRemark());
        if (log.getRunTime() != null) {
            poj.setRunTime(sdf.format(log.getRunTime()));
        }

        AdProject project = projectImpl.qryProject(log.getProjectId());
        poj.setProjectName(project.getProjectName());
        if (StringUtils.isNotEmpty(log.getAppId())) {
            poj.setAppId(log.getAppId().split(","));
        }
        return JsonpUtil.modelToJson(poj);
    }

    @RequestMapping(value = "/project/qryLogStrById", produces = "application/json")
    public String qryLogStrById(@RequestParam Map map) throws Exception {
        if (!map.containsKey("logId")) {
            throw new Exception("lose necessary param");
        }
        Long logId = Long.parseLong((String) map.get("logId"));
        AdSystemDeployLog log = systemDeployLogImpl.qryById(logId);
        if (log == null) {
            throw new Exception("AdSystemDeployLog is null!");
        }
        AdSystemdeployLogStrPojo poj = new AdSystemdeployLogStrPojo();
        poj.setLogStr(log.getDeployComment());
        poj.setState(log.getDeployResult());
        return JsonpUtil.modelToJson(poj);
    }
}
