package com.asiainfo.comm.module.deploy.service.impl;

import com.asiainfo.comm.common.pojo.Pojo;
import com.asiainfo.comm.common.pojo.pojoMaster.CommonPojo;
import com.asiainfo.util.DateConvertUtils;
import com.asiainfo.util.MapUtil;
import com.asiainfo.util.StringUtil;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDeployPackageDAO;
import com.asiainfo.comm.module.build.service.impl.AdUserImpl;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.schedule.helper.DeployRunnable;
import com.google.common.collect.Maps;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


@Component
@lombok.extern.slf4j.Slf4j
public class DeployManagerImpl {

    @Autowired
    private SystemDeployLogImpl systemDeployLogImpl;
    @Autowired
    private VirtualDeployInfoImpl virtualDeployInfoImpl;
    @Autowired
    private DcosDeployInfoImpl dcosDeployInfoImpl;
    @Autowired
    private AdUserImpl adUserImpl;
    @Autowired
    private AdProjectDeployPackageDAO adProjectDeployPackageDAO;

    public CommonPojo deployPlan(Map<String, Object> params, HttpServletRequest request) throws Exception {
        Integer operType = null;//1、发布 2、重启 3、启动 4、停止
        CommonPojo poj = new CommonPojo();
        long userId = (Long) request.getSession().getAttribute("userId");//用户信息
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String time = (String) params.get("time");
        AdUser user = adUserImpl.qryById(userId);
        Long projectId = Long.parseLong((String) params.get("projectId"));//应用ID
        String env = (String) params.get("env");//环境
        String type = (String) params.get("type");//发布类型:1-全量，2-灰度，3-beta
        String remark = (String) params.get("remarks");//备注
        Long branchId = Long.parseLong((String) params.get("branchId"));//获取branchId
        String lsAppId = "";
        lsAppId = getAppId(params);
        params.put("projectId", Long.parseLong((String) params.get("projectId")));
        if (params.get("operType") != null && StringUtils.isNotEmpty((String) params.get("operType"))) {
            operType = Integer.parseInt((String) params.get("operType"));
        } else {
            log.error("传入的操作类型operType:" + operType + "错误");
            throw new Exception("传入的操作类型错误operType:" + operType);
        }
        //新增
        long packId = 0;
        if (operType == 1) {
            if (params.get("proTagId") != null) {
                packId = Long.parseLong((String) params.get("proTagId"));
                AdProjectDeployPackage adProjectDeployPackage = adProjectDeployPackageDAO.qryByPackageId(packId);
                if (adProjectDeployPackage == null) {
                    throw new Exception("找不到发布包");
                }
            } else {
                throw new Exception("找不到发布包");
            }
        }
        Date runTime = null;
        if (time != null && StringUtils.isNotEmpty(time)) {
            runTime = sdf.parse(time);
            if (DateConvertUtils.getSecSpace(runTime.getTime(), new Date().getTime()) > 0) {
                throw new Exception("执行时间必须要大于当前时间");
            }
        }
        //记录发布计划
        String jobToken = null;
        if (runTime == null) {
            runTime = new Date();
        } else {
            jobToken = new StringUtil().getRandomStr(10);
        }
        long envId;
        String envType;
        EnvParams envParams = new EnvParams(params, env).invoke();
        envId = envParams.getEnvId();
        envType = envParams.getEnvType();
        AdSystemDeployLog log = new AdSystemDeployLog();
        log.setProjectId(projectId);
        log.setDeployType(type);
        log.setAdUser(user);
        log.setDeployResult(3);//待执行
        checkEnvType(params, envId, envType, log);
        if (remark != null && StringUtils.isNotEmpty(remark)) {
            log.setRemark(remark);
        }
        log.setCreateDate(new Date());
        log.setRunTime(runTime);
        log.setJobToken(jobToken);
        log.setEnvId(envId);
        log.setProTagId(packId);
        log.setOperType(operType);
        log.setBranchId(branchId);
        if (StringUtils.isNotEmpty(lsAppId)) {
            log.setAppId(lsAppId);
        }
        if (time == null || !StringUtils.isNotEmpty(time)) {
            log.setPlanState(0);
            long logId = systemDeployLogImpl.saveAndReturn(log);
            params.put("logId", logId);
            Thread runThread = new Thread(new DeployRunnable(params));
            runThread.start();
            poj.setRetMessage("success");
        } else {
            log.setPlanState(1);
            long logId = systemDeployLogImpl.saveAndReturn(log);
            params.put("logId", logId);
            poj.setRetMessage("success");
        }
        return poj;
    }


    private void checkEnvType(Map<String, Object> params, long envId, String envType, AdSystemDeployLog log) {
        if (envType.equals("dcos")) {
            log.setHostType(2);
            AdDcosDeployInfo dcosDeployInfo = dcosDeployInfoImpl.qryDcosDeployInfoById(envId);
            log.setIp(dcosDeployInfo.getDocsServerUrl());
            params.put("envInfo", dcosDeployInfo);
        } else {
            AdVirtualEnvironment virtualEnvironment = virtualDeployInfoImpl.qryById(envId);
            log.setIp(virtualEnvironment.getServerUrl());
            params.put("envInfo", virtualEnvironment);
            log.setHostType(1);
        }
    }

    public String getAppId(Map<String, Object> params) {
        String lsAppId = "";
        StringBuilder sb = new StringBuilder();
        if (params.get("appId") != null) {
            String appid = (String) params.get("appId");
            if (StringUtils.isNotEmpty(appid)) {
                JSONObject jsonObject;
                JSONArray jsonArray = JSONArray.fromObject(appid);
                for (int i = 0; i < jsonArray.size(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getBoolean("check")) {
                        sb.append(jsonObject.get("appid")).append(",");
                    }
                }
                lsAppId = sb.toString();
                if (lsAppId.length() > 0) {
                    lsAppId = lsAppId.substring(0, lsAppId.length() - 1);
                }
            }
        }
        return lsAppId;
    }

    public Pojo rollBackDeploy(Map<String, Object> params, HttpServletRequest request) throws Exception {
        CommonPojo poj = new CommonPojo();
        Long logId = Long.parseLong((String) params.get("logId"));
        AdSystemDeployLog rollBackLog = systemDeployLogImpl.qryById(logId);
        long userId = (Long) request.getSession().getAttribute("userId");//用户信息
        AdUser user = adUserImpl.qryById(userId);
        Long projectId = rollBackLog.getProjectId();//应用ID
        String version = rollBackLog.getDeployVersion();
        String type = rollBackLog.getDeployType();//发布类型:1-全量，2-灰度，3-beta
        String remark = rollBackLog.getRemark();//备注
        String appId = rollBackLog.getAppId();
        Long branchId = rollBackLog.getBranchId();
        //新增
        long packId;
        packId = rollBackLog.getProTagId();
        AdProjectDeployPackage adProjectDeployPackage = adProjectDeployPackageDAO.qryByPackageId(packId);
        if (adProjectDeployPackage == null) {
            throw new Exception("找不到发布包");
        }
        params.put("commitId", adProjectDeployPackage.getCommitId());
        params.put("projectId", rollBackLog.getProjectId());
        long envId = rollBackLog.getEnvId();
        String envType = rollBackLog.getHostType() == 1 ? "virtual" : "docs";
        params.put("envId", envId);
        params.put("envType", envType);
        params.put("appId", appId);
        params.put("branchId", "" + branchId);
        //记录发布计划
        AdSystemDeployLog log = new AdSystemDeployLog();
        log.setProjectId(projectId);
        log.setDeployType(type);
        log.setDeployVersion(version);
        log.setAdUser(user);
        log.setDeployResult(3);//待执行
        log.setRemark("回滚发布\n");
        log.setRunTime(new Date());
        log.setAppId(appId);
        log.setBranchId(branchId);
        checkEnvType(params, envId, envType, log);
        if (remark != null && StringUtils.isNotEmpty(remark)) {
            log.setRemark(log.getRemark() + remark);
        }
        log.setCreateDate(new Date());
        log.setRunTime(new Date());
        log.setEnvId(envId);
        log.setProTagId(packId);
        long newLogId = systemDeployLogImpl.saveAndReturn(log);
        params.put("logId", newLogId);
        try {
            Thread runThread = new Thread(new DeployRunnable(params));
            runThread.start();
        } catch (Exception e) {
            log.setDeployResult(2);
            log.setDeployComment(e.getMessage());
            systemDeployLogImpl.addLogsBySystemId(log);
        }
        poj.setRetMessage("success");
        return poj;
    }

    public Pojo modifyDeployPlan(Map<String, Object> params, HttpServletRequest request) throws Exception {
        CommonPojo poj = new CommonPojo();
        long userId = (Long) request.getSession().getAttribute("userId");//用户信息
        AdUser user = adUserImpl.qryById(userId);
        Long projectId = Long.parseLong((String) params.get("projectId"));//应用ID
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String time = (String) params.get("time");
        String env = (String) params.get("env");//环境
        String version = (String) params.get("version");
        String type = (String) params.get("type");//发布类型:1-全量，2-灰度，3-beta
        String remark = (String) params.get("remarks");//备注
        Long logId = Long.parseLong((String) params.get("logId"));
        //新增
        Long tagId = Long.parseLong((String) params.get("proTagId"));//tag的Id
        params.put("proTagId", tagId);
        params.put("logId", logId);
        String lsAppId = "";
        lsAppId = getAppId(params);
        AdProjectDeployPackage adProjectDeployPackage = adProjectDeployPackageDAO.qryByPackageId(tagId);
        params.put("tag", adProjectDeployPackage);
        Date runTime = null;
        if (time != null && StringUtils.isNotEmpty(time)) {
            runTime = sdf.parse(time);
            if (DateConvertUtils.getSecSpace(runTime.getTime(), new Date().getTime()) > 0) {
                throw new Exception("执行时间必须要大于当前时间");
            }
        }
        long envId;
        String envType;
        EnvParams envParams = new EnvParams(params, env).invoke();
        envId = envParams.getEnvId();
        envType = envParams.getEnvType();
        //记录发布计划
        String jobToken = null;
        if (runTime == null) {
            runTime = new Date();
        } else {
            jobToken = new StringUtil().getRandomStr(10);
        }
        AdSystemDeployLog log = systemDeployLogImpl.qryById(logId);
        if (log.getDeployResult() != 3) {
            poj.setRetCode("500");
            poj.setRetMessage("只有\"待执行\"的计划才可更改！");
            return poj;
        } else if (log.getRunTime() == null) {
            poj.setRetCode("500");
            poj.setRetMessage("发布类型为\"立即发布\"的计划不可更改！");
            return poj;
        } else if (DateConvertUtils.getMinSpace(new Date().getTime(), log.getRunTime().getTime()) < 1) {
            poj.setRetCode("500");
            poj.setRetMessage("当前操作距离发布开始时间小于一分钟，对象被锁定！");
            return poj;
        }
        log.setProjectId(projectId);
        log.setDeployType(type);
        log.setDeployVersion(version);
        log.setModifyUser(user);
        log.setEnvId(envId);
        log.setProTagId(tagId);
        log.setAppId(lsAppId);
        params.put("projectId", projectId);
        checkEnvType(params, envId, envType, log);
        if (remark != null && StringUtils.isNotEmpty(remark)) {
            log.setRemark(remark);
        }
        log.setModifyDate(new Date());
        log.setRunTime(runTime);
        log.setJobToken(jobToken);
        if (time == null || !StringUtils.isNotEmpty(time)) {
            log.setPlanState(0);
            systemDeployLogImpl.addLogsBySystemId(log);
            params.put("logId", logId);
            Thread runThread = new Thread(new DeployRunnable(params));
            runThread.start();
            poj.setRetMessage("success");
        } else {
            //状态置为修改
            log.setPlanState(3);
            systemDeployLogImpl.addLogsBySystemId(log);
            params.put("logId", logId);
            poj.setRetMessage("success");
        }
        return poj;
    }

    //  获取 AppId 和实例数的 对应关系
    public Map<String, String> getAppIdScale(Map<String, Object> params) {
        Map<String, String> appIdScale = Maps.newHashMap();
        String appid = MapUtil.getValue(params, "appId", "");
        if (StringUtils.isNotEmpty(appid)) {
            JSONObject jsonObject;
            JSONArray jsonArray = JSONArray.fromObject(appid);
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getBoolean("check")) {
                    appIdScale.put(jsonObject.get("appid").toString(), jsonObject.get("exampleNum").toString());
                }
            }
        }
        return appIdScale;
    }

    private class EnvParams {
        private Map<String, Object> params;
        private String env;
        private long envId;
        private String envType;

        EnvParams(Map<String, Object> params, String env) {
            this.params = params;
            this.env = env;
        }

        public long getEnvId() {
            return envId;
        }

        public String getEnvType() {
            return envType;
        }

        EnvParams invoke() throws Exception {
            if (env != null && StringUtils.isNotEmpty(env)) {
                String[] envStrList = env.split("_");
                envId = Long.parseLong(envStrList[0]);
                params.put("envId", envId);
                envType = envStrList[1];
                params.put("envType", envType);
            } else {
                throw new Exception("环境数据有误");
            }
            return this;
        }
    }
}
