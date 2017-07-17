package com.asiainfo.comm.module.deploy.service.impl;

import com.asiainfo.comm.common.enums.DcosResultState;
import com.asiainfo.comm.common.pojo.pojoExt.NumberCount;
import com.asiainfo.comm.module.build.dao.impl.AdDcosDeployDtlDAO;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDAO;
import com.asiainfo.comm.module.build.dao.impl.AdProjectDeployPackageDAO;
import com.asiainfo.comm.module.build.service.impl.AdGroupImpl;
import com.asiainfo.comm.module.build.service.impl.AdProjectDeployPackageImpl;
import com.asiainfo.comm.module.build.service.impl.AdUserImpl;
import com.asiainfo.comm.module.common.AdStaticDataImpl;
import com.asiainfo.comm.module.deploy.dao.impl.AdDcosDeployInfoDAO;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.schedule.helper.DeployDcosRunnable;
import com.asiainfo.schedule.helper.RestartDcosRunnable;
import com.asiainfo.schedule.helper.StartDcosRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Created by guojian on 8/8/16.
 */
@Component("DcosApiImpl")
@lombok.extern.slf4j.Slf4j
public class DcosApiImpl {

    @Autowired
    AdProjectDeployPackageDAO adProjectDeployPackageDAO;
    @Autowired
    AdProjectDeployPackageImpl adProjectDeployPackageImpl;
    @Autowired
    SystemDeployLogImpl systemDeployLogImpl;
    @Autowired
    AdDcosDeployInfoDAO adDcosDeployInfoDAO;
    @Autowired
    AdDcosDeployDtlDAO dcosDeployDtlDAO;
    @Autowired
    AdStaticDataImpl bsStaticDataImpl;
    @Autowired
    AdGroupImpl adGroupImpl;
    @Autowired
    DeployManagerImpl deployManager;
    private String path;
    private RestTemplate restTemplate;
    private HttpHeaders headers;
    private String APP_UPLOAD;
    private String APP_DEPLOY;
    public  String APP_RESTAR;
    private String APP_STATUS;
    private String APP_START;
    private String APP_STOP;
    @Autowired
    private AdProjectDAO adProjectDAO;
    @Autowired
    private AdUserImpl adUserImpl;
    private SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private DcosDeployInfoImpl dcosDeployInfoImpl;

    public
    @ResponseBody
    boolean deployTodocs(AdBranch adBranch, String ip, int deployResult) {
        boolean flag = true;
        AdSystemDeployLog log = new AdSystemDeployLog();
        //记录发布历史
        log.setProjectId(adBranch.getAdProject().getProjectId());
        log.setStartTime(new Date());
        log.setIp(ip);
        log.setDeployType("1");
        AdUser adUser = adUserImpl.qryById(1L);
        log.setAdUser(adUser);
        //获取appid
        try {
            log.setDeployResult(deployResult);
            log.setEndTime(new Date());
            systemDeployLogImpl.addLogsBySystemId(log);
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    public void deploy2docs(Long deployInfoId, long projectId, String commitId, AdSystemDeployLog deployLog, Long branchId) {
        //记录发布历史
        deployLog.setStartTime(new Date());
        StringBuilder sb = new StringBuilder("<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> Start");
        deployLog.setDeployComment(sb.toString());
        systemDeployLogImpl.addLogsBySystemId(deployLog);
        AdDcosDeployInfo dcosDeployInfo = dcosDeployInfoImpl.qryDcosDeployInfoById(deployInfoId);
        String appId = deployLog.getAppId();
//        List<AdDcosDeployDtl> dtlList = qryAppIdInfo(projectId, appId, dcosDeployInfo);// 查询发布APP相关的包
//       List<AdDcosDeployDtl> dtlList1 = dcosDeployDtlDAO.qryByBranchAndAppIds(branchId, appId);
        List<AdDcosDeployDtl> dtlList = dcosDeployDtlDAO.qryByDcosDeployDtlAndAppIds(deployInfoId, appId);
        //存储与发布APP相关的包
        List<List<String>> appIdList;
        appIdList = new ArrayList<>();
        Map<String, List<String>> packageAppMap = new HashMap<>();
        int priorityNum = -1;
        if (dtlList != null) {
            for (int i = 0; i < dtlList.size(); i++) {
                AdDcosDeployDtl dtl = dtlList.get(i);
                if (dtl.getPriorityNum() != priorityNum) {
                    priorityNum = checkPriorityNum(dtlList, appIdList, priorityNum, i, dtl);
                } else {
                    appIdList.get(appIdList.size() - 1).add(dtlList.get(i).getAppid());
                }
                addPackageAppMap(packageAppMap, dtl);
            }
        }
        //获取仓库路径
        List<AdProjectDeployPackage> deployPackages = adProjectDeployPackageDAO.qryByCommitAndBranch(commitId, branchId);
        if (deployPackages.size() < 1) {
            deployLog.setDeployResult(2);
            deployLog.setEndTime(new Date());
            logAppend(sb, "", "", false, "no Repository for this commit id!");
            saveFailedDeployLog(deployLog, sb);
            return;
        }
//        AdDcosDeployInfo adDcosDeployInfo = dcosDeployInfoImpl.qryDcosDeployInfoById(deployInfoId);
        this.init(dcosDeployInfo);
        try {
            HashSet<String> failedAppId = new HashSet<>();
            int i = 0;
//            String firstAppId="";
            HashSet<String> firstAppId = new HashSet<>();
            boolean firstAppIdDeployResult;
            String deployRemark = "";
            for (AdProjectDeployPackage deployPackage : deployPackages) {
                String[] packages = deployPackage.getPackagePath().split("/");
                String packageName = packages[packages.length - 1];
                //只上传和发布的APP相关的包
                if (!packageAppMap.containsKey(packageName)) {
                    continue;
                }
                deployRemark = packages[packages.length - 3] + "_" + packages[packages.length - 1] + " ";
                //下载 上传 获取md5
                String md5 = "";
                AdStaticData AdStaticData = bsStaticDataImpl.qryStaticDataByCodeValue("DCOS_ANSIBLE", "false");
                if (null == AdStaticData) {
                    md5 = adProjectDeployPackageImpl.uploadToDocsByAnsible(deployPackage, dcosDeployInfo);
                } else {
                    md5 = adProjectDeployPackageImpl.uploadToDocs(deployPackage, dcosDeployInfo);
                }
                if (i != 0) {
                    sb.append("\n");
                }
                logAppend(sb, "", "", true, "get MD5\" " + packageName + "\":" + md5);
                saveDeployLogByStringBuilder(deployLog, sb);
                //dcos上传
//                UploadNotifyResult uploadNotifyResult;
//                NumberCount count = new NumberCount(packageAppMap.get(packageName).size());
                int count = 0;
                for (String appIdStr : packageAppMap.get(packageName)) {
                    //版本号是dcos那边的版本号，暂时定为空
                    //日志记录暂时取消
                    /*deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> upload url[" + APP_UPLOAD + "],appId[<h class=\"dcos-deploy-appid\">" + appIdStr + "</h>],version[v1.0],ftpPath[" + dcosDeployInfo.getDcosFtpPath() + "],packageName[" + packageName + "],md5[" + md5 + "],remark[ADCloud发布计划上传 " + (deployLog.getRemark() == null ? "" : deployLog.getRemark()) + "]");
                    systemDeployLogImpl.addLogsBySystemId(deployLog);*/
                    if (count == 0) {
                        uploadPackageByAppId(deployLog, dcosDeployInfo, failedAppId, packages, md5, appIdStr, sb);
                        firstAppId.add(appIdStr);
                    }
                    count++;

//                    Thread thread = new Thread(new UploadDcosRunnable(deployLog, adDcosDeployInfo, failedAppId, packageName, md5, appIdStr, count, this, sb));
//                    thread.start();
//                    uploadPackageByAppId(deployLog, dcosDeployInfo, failedAppId, packageName, md5, appIdStr);
                }
                /*while (true) {
                    if (count.getNumber() == 0) {
                        break;
                    }
                    Thread.sleep(5000);
                }*/
                i++;
            }
            if (i != 0) {  // 如果 i=0 说明没有上传包
                sb.append("\n");
                for (List<String> appIdStrList : appIdList) {
                    if (appIdStrList.size() > 1) {//并发
                        NumberCount count = new NumberCount(appIdStrList.size());
                        firstAppIdDeployResult = false;
                        for (String appIdStr : appIdStrList) {
                            // 该appId已经执行失败
                            if (failedAppId.contains(appIdStr)) {
                                continue;
                            }
                            if (firstAppId.contains(appIdStr)) {  //如果上传包成功，就调 部署接口(包含重启)，部署只调一次
                                logAppend(sb, "deploy", appIdStr, true, "");
                                DeployNotifyResult deployNotifyResult = this.deploy(appIdStr, "v1.0"/*deployLoglog.getDeployVersion()*/, "ADCloud发布计划部署 " + deployRemark + " " + (deployLog.getRemark() == null ? "" : deployLog.getRemark()), dcosDeployInfo.getDocsUserName());
                                if (deployNotifyResult.getReturnCode() == null || deployNotifyResult.getReturnCode().equals(DcosResultState.Failed.getValue())) {
                                    logAppend(sb, "deploy", appIdStr, false, deployNotifyResult.getReturnMsg());
                                    saveFailedDeployLog(deployLog, sb);
                                } else {
                                    logAppend(sb, "deploy", appIdStr, true, "ok");
                                    saveDeployLogByStringBuilder(deployLog, sb);
                                    firstAppIdDeployResult = true;
                                }
                            }
                            if (firstAppIdDeployResult) {
                                Thread thread = new Thread(new DeployDcosRunnable(deployLog, dcosDeployInfo, appIdStr, count, this, sb, firstAppId));
                                thread.start();
                            }
                        }
                        if (firstAppIdDeployResult) {
                            while (true) {
                                if (count.getNumber() == 0) {
                                    break;
                                }
                                Thread.sleep(5000);
                            }
                        } else {
                            deployLog.setFailedTime(new Date());
                        }
                    } else {//顺序
                        //dcos部署
                        String appIdStr = appIdStrList.get(0);
                        // 该appId已经执行失败
                        if (failedAppId.contains(appIdStr)) {
                            continue;
                        }
                        //版本号是dcos那边的版本号，暂时定为空
                        logAppend(sb, "deploy", appIdStr, true, "");
                        DeployNotifyResult deployNotifyResult = this.deploy(appIdStr, "v1.0"/*deployLoglog.getDeployVersion()*/, "ADCloud发布计划部署-" + deployRemark + (deployLog.getRemark() == null ? "" : deployLog.getRemark()), dcosDeployInfo.getDocsUserName());
                        if (deployNotifyResult.getReturnCode() == null || deployNotifyResult.getReturnCode().equals("-1")) {
                            logAppend(sb, "deploy", appIdStr, false, deployNotifyResult.getReturnMsg());
                            saveFailedDeployLog(deployLog, sb);
                        } else {
                            logAppend(sb, "deploy", appIdStr, true, "ok");
                            saveDeployLogByStringBuilder(deployLog, sb);
                            restartOrStopDcos(deployLog, appIdStr, sb);
                        }
                    }
                }
            } else {
                logAppend(sb, "", "", false, "选择的包与选择的appId关联的包不符,请查看环境配置里的相应配置！！或者调用云管查APPId失败");
                deployLog.setFailedTime(new Date());
            }
            endDcosDeploy(deployLog, sb);
        } catch (Exception e) {
            log.error("调用deploy2docs异常:" + e);
            deployLog.setDeployResult(2);
            deployLog.setEndTime(new Date());
            logAppend(sb, "", "", true, e.getMessage());
            deployLog.setDeployComment(sb.toString());
            systemDeployLogImpl.addLogsBySystemId(deployLog);
        }
    }

    public void uploadPackageByAppId(AdSystemDeployLog deployLog, AdDcosDeployInfo dcosDeployInfo, HashSet<String> failedAppId, String[] packages, String md5, String appIdStr, StringBuilder sb) {
        UploadNotifyResult uploadNotifyResult;
        uploadNotifyResult = this.upload(appIdStr, "v1.0"/*deployLoglog.getDeployVersion()*/, dcosDeployInfo.getDcosFtpPath(), packages[packages.length - 1], md5, "ADCloud发布上传-" + packages[packages.length - 3] + "_" + packages[packages.length - 1] + " " + (deployLog.getRemark() == null ? "" : deployLog.getRemark()));
        if (uploadNotifyResult.getReturnCode() == null || uploadNotifyResult.getReturnCode().equals(DcosResultState.Failed.getValue())) {
            logAppend(sb, "upload package " + packages[packages.length - 1] + " for", appIdStr, false, uploadNotifyResult.getReturnMsg());
            saveFailedDeployLog(deployLog, sb);
            failedAppId.add(appIdStr);
        } else {
            logAppend(sb, "upload package " + packages[packages.length - 1] + " for", appIdStr, true, "success");
            saveDeployLogByStringBuilder(deployLog, sb);
        }
    }

    public void deployDcos(AdSystemDeployLog deployLog, String appIdStr, StringBuilder sb) throws InterruptedException, TimeoutException {
        //docs重启
        restartOrStopDcos(deployLog, appIdStr, sb);
    }

    public void addPackageAppMap(Map<String, List<String>> complicatePackageAppMap, AdDcosDeployDtl dtl) {
        if (!complicatePackageAppMap.containsKey(dtl.getPackageName())) {
            List<String> appIds = new ArrayList<>();
            appIds.add(dtl.getAppid() + "");
            complicatePackageAppMap.put(dtl.getPackageName(), appIds);
        } else {
            complicatePackageAppMap.get(dtl.getPackageName()).add(dtl.getAppid() + "");
        }
    }

    public void saveFailedDeployLog(AdSystemDeployLog deployLog, StringBuilder sb) {
        deployLog.setFailedTime(new Date());
        deployLog.setDeployComment(sb.toString());
        systemDeployLogImpl.addLogsBySystemId(deployLog);
    }

    public void saveDeployLogByStringBuilder(AdSystemDeployLog deployLog, StringBuilder sb) {
        deployLog.setDeployComment(sb.toString());
        systemDeployLogImpl.addLogsBySystemId(deployLog);
    }

    //  重启或者停止
    public void restartOrStopDocs(Long deployInfoId, AdSystemDeployLog deployLog, StringBuilder sb) {
        //记录发布历史
        deployLog.setStartTime(new Date());
        deployLog.setDeployComment("");
        String appId = deployLog.getAppId();
        AdDcosDeployInfo adDcosDeployInfo = dcosDeployInfoImpl.qryDcosDeployInfoById(deployInfoId);
        this.init(adDcosDeployInfo);
        List<AdDcosDeployDtl> dtlList = dcosDeployDtlDAO.qryByDcosDeployDtlAndAppIds(deployInfoId, appId);
        List<List<String>> appIdList = new ArrayList<>();
        sortAppIdPriorityNum(dtlList, appIdList); // 根据 appId 排优先级
        try {
            for (List<String> appIdStrList : appIdList) {
                if (appIdStrList.size() > 0) {//并发
                    NumberCount count = new NumberCount(appIdStrList.size());
                    for (String appIdStr : appIdStrList) {
                        Thread thread = new Thread(new RestartDcosRunnable(deployLog, appIdStr, count, this, sb));
                        thread.start();
                    }
                    while (true) {
                        if (count.getNumber() == 0) {
                            break;
                        }
                        Thread.sleep(5000);
                    }
                } else {//顺序
                    String appIdStr = appIdStrList.get(0);
                    //dcos重启 或停止
                    restartOrStopDcos(deployLog, appIdStr, sb);
                }
            }
            endDcosDeploy(deployLog, sb);
        } catch (Exception e) {
            log.error("调用restartOrStopDocs异常:" + e);
            sb.append("\n<h class=\"dcos-deploy-time\">").append(sFormat.format(new Date())).append("</h> ").append(e.getMessage());
            saveFailedDeployLog(deployLog, sb);
            deployLog.setDeployResult(2);
            deployLog.setEndTime(new Date());
            systemDeployLogImpl.addLogsBySystemId(deployLog);
        }
    }

    public void endDcosDeploy(AdSystemDeployLog deployLog, StringBuilder sb) {
        deployLog.setDeployComment(sb.toString());
        deployLog.setEndTime(new Date());
        if (deployLog.getFailedTime() == null) {
            deployLog.setDeployResult(1);
            systemDeployLogImpl.addLogsBySystemId(deployLog);
        } else {
            deployLog.setDeployResult(2);
            systemDeployLogImpl.addLogsBySystemId(deployLog);
        }
    }

    public int checkPriorityNum(List<AdDcosDeployDtl> dtlList, List<List<String>> appIdList, int priorityNum, int i, AdDcosDeployDtl dtl) {
        List<String> appIdStrList = new ArrayList<>();
        appIdStrList.add(dtl.getAppid() + "");
        appIdList.add(appIdStrList);
        if (i != dtlList.size() - 1) {
            priorityNum = dtl.getPriorityNum();
        }
        return priorityNum;
    }

    public void restartOrStopDcos(AdSystemDeployLog deployLog, String appIdStr, StringBuilder sb) throws InterruptedException, TimeoutException {
        String messages = "restart";
        deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> restart <h class=\"dcos-deploy-appid\">" + appIdStr + "</h>");
        RestartNotifyResult restartNotifyResult = null;
        if (deployLog.getOperType() == 4) {  //  如果 操作类型是 4 ,则是 停止操作
            restartNotifyResult = restart(appIdStr, APP_STOP);
            messages = "stop";
        } else {
            restartNotifyResult = restart(appIdStr, APP_RESTAR);
        }
        if (restartNotifyResult.getReturnCode().equals(DcosResultState.Failed.getValue())) {
            logAppend(sb, messages, appIdStr, false, restartNotifyResult.getReturnMsg());
            saveFailedDeployLog(deployLog, sb);
        } else {
            logAppend(sb, messages, appIdStr, true, "started");
            saveDeployLogByStringBuilder(deployLog, sb);
        }
        systemDeployLogImpl.addLogsBySystemId(deployLog);
        //检查状态,写历史日志
        this.checkDeploySync(appIdStr, deployLog, sb);
    }

    public void checkFirstDeploySync(AdSystemDeployLog deployLog, String appIdStr, StringBuilder sb) throws InterruptedException {
        deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> restart <h class=\"dcos-deploy-appid\">" + appIdStr + "</h>");
        systemDeployLogImpl.addLogsBySystemId(deployLog);
        //检查状态,写历史日志
        this.checkDeploySync(appIdStr, deployLog, sb);
    }

    public void logAppend(StringBuilder sb, String step, String appId, boolean isTrue, String message) {
        sb.append("\n<h class=\"dcos-deploy-time\">").append(sFormat.format(new Date())).append("</h>");
        if (step != null && step.length() > 0) {
            sb.append(" " + step);
        }
        if (appId != null && appId.length() > 0) {
            sb.append(" <h class=\"dcos-deploy-appid\">").append(appId).append("</h>");
        }
        if (!isTrue) {
            sb.append(" failed: <h class=\"dcos-deploy-error-msg\">").append(message).append("</h>");
        } else {
            sb.append(" " + message);
        }
    }

    /**
     * 采用同步方式检查重启状态，直到成功或失败再进行下一个APP的重启
     *
     * @param appId
     */
    private int checkDeploySync(String appId, AdSystemDeployLog deployLog, StringBuilder sb) throws InterruptedException {
        int i = 0;
        while (true) {
            logAppend(sb, "status", appId, true, "is still starting");
            saveDeployLogByStringBuilder(deployLog, sb);
            if (i > 240) {
                logAppend(sb, "status", appId, false, "超时");
                saveFailedDeployLog(deployLog, sb);
                return -1;
            }
            StatusNotifyResult snr = status(appId);
            if (deployLog.getOperType() == 4 && snr.getReturnCode().equals(DcosResultState.Stopped.getValue())) { // 若操作类型是 停止，状态是 返回的状态是2
                logAppend(sb, "status", appId, true, snr.getReturnMsg());
                saveDeployLogByStringBuilder(deployLog, sb);
                return 1;
            }
            if (snr.getReturnCode().equals(DcosResultState.Finished.getValue())) {
                logAppend(sb, "status", appId, true, snr.getReturnMsg());
                saveDeployLogByStringBuilder(deployLog, sb);
                return 1;
            }
            if (snr.getReturnCode().equals(DcosResultState.Stopped.getValue()) || snr.getReturnCode().equals(DcosResultState.Failed.getValue())) {
                logAppend(sb, "status", appId, false, snr.getReturnMsg());
                saveFailedDeployLog(deployLog, sb);
                return -1;
            }
            Thread.sleep(17000);
            i++;
        }
    }

    /**
     * 調用Shell腳本從artifactory倉庫上傳文件至DCOS服務器
     *
     * @param appId
     * @param version
     * @param packagePath
     * @param packageName
     * @param md5
     * @param remark
     * @return
     */
    public UploadNotifyResult upload(String appId, String version, String packagePath, String packageName, String md5, String remark) {
        UploadNotify un = new UploadNotify(appId, version, packagePath, packageName, md5, remark);
        log.error("************uploadNotifyInput=" + appId + "*" + version + "*" + packagePath + "*" + packageName + "*" + md5 + "*" + remark + "*" + APP_UPLOAD);
        HttpEntity request = new HttpEntity(un, headers);
        UploadNotifyResult result = restTemplate.postForObject(APP_UPLOAD, request, UploadNotifyResult.class);
        log.error("************uploadNotifyreturn=" + result.getReturnCode());
        return result;

    }

    /**
     * @param appId
     * @param version
     * @param remark
     * @return
     */
    public DeployNotifyResult deploy(String appId, String version, String remark, String username) {
        DeployNotify dn = new DeployNotify(appId, version, remark, username);
        log.error("************DeployNotifyInput=" + appId);
        HttpEntity request = new HttpEntity(dn, headers);
        DeployNotifyResult result = restTemplate.postForObject(APP_DEPLOY, request, DeployNotifyResult.class);
        log.error("************uploadNotifyreturn=" + result.toString() + "****" + result.getReturnCode());
        return result;
    }

    /**
     * 根据APPID重启当前应用
     *
     * @param appId
     * @param type  操作类型
     * @return
     */
    public RestartNotifyResult restart(String appId, String type) {
        RestartNotify rn = new RestartNotify(appId);
        log.error("************RestartNotifyInput=" + appId);
        HttpEntity request = new HttpEntity(rn, headers);
        RestartNotifyResult result = restTemplate.postForObject(type, request, RestartNotifyResult.class);
        log.error("************RestartNotifyreturn=" + result.getReturnCode());
        return result;
    }

    /**
     * 根据APPID查询当前应用的状态
     *
     * @param appId
     * @return
     */
    public StatusNotifyResult status(String appId) {
        StatusNotify sn = new StatusNotify(appId);
        log.error("************StatusNotifyInput=" + appId);
        HttpEntity request = new HttpEntity(sn, headers);
        StatusNotifyResult result = restTemplate.postForObject(APP_STATUS, request, StatusNotifyResult.class);
        log.error("************statusNotifyreturn=" + result.getReturnCode());
        return result;
    }

    /**
     * 根据APPId 启动当前应用
     *
     * @param appId
     * @return
     */
    public StartNotifyResult start(String appId) {
        StartNotify start = new StartNotify(appId);
        log.error("************StatusNotifyInput=" + appId);
        HttpEntity request = new HttpEntity(start, headers);
        StartNotifyResult result = restTemplate.postForObject(APP_START, request, StartNotifyResult.class);
        log.error("************statusNotifyreturn=" + result.getReturnCode());
        return result;
    }

    public void init(AdDcosDeployInfo adDcosDeployInfo) {
        restTemplate = new RestTemplate();
        headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json;");
        headers.setContentType(type);
        headers.set("username", adDcosDeployInfo.getDocsUserName());
        headers.set("password", adDcosDeployInfo.getDocsUserPassword());
        path = adDcosDeployInfo.getDocsServerUrl();
        APP_UPLOAD = adDcosDeployInfo.getDocsServerUrl() + "/v1/apps/uploadNotify";
        APP_DEPLOY = adDcosDeployInfo.getDocsServerUrl() + "/v1/apps/deploy";
        APP_RESTAR = adDcosDeployInfo.getDocsServerUrl() + "/v1/apps/restart";
        APP_STATUS = adDcosDeployInfo.getDocsServerUrl() + "/v1/apps/status";
        APP_START = adDcosDeployInfo.getDocsServerUrl() + "/v1/apps/start";
        APP_STOP = adDcosDeployInfo.getDocsServerUrl() + "/v1/apps/stop";
    }

    // 查询 根据页面不同入口来查询 appID信息，因为 从云管入口进来的项目，appId 对应的包名或ftp 信息会变
//    public List<AdDcosDeployDtl> qryAppIdInfo(Long projectId, String appId, AdDcosDeployInfo dcosDeployInfo) {
//        List<AdDcosDeployDtl> dtlList;
//        Boolean isCmpGroup = adGroupImpl.qryIsCmpGroup(projectId);
//        if (isCmpGroup != null && isCmpGroup) {   // 若 group 表中GroupIdExt 不为空，则是云管入口进来的项目，appId 对应的包名或ftp 信息会变
//            dtlList = dcosDeployInfoImpl.updateCmpAppIdInfo(appId, dcosDeployInfo);
//        } else {   // 否则 是从ADcloud入口进来的
//            dtlList = dcosDeployDtlDAO.qryByDcosDeployDtlAndAppIds(dcosDeployInfo.getDeployInfoId(), appId);
//        }
//        return dtlList;
//    }

    public void sortAppIdPriorityNum(List<AdDcosDeployDtl> dtlList, List<List<String>> appIdList) {
        int priorityNum = -1;
        for (int i = 0; i < dtlList.size(); i++) {  // AppId 优先级排序
            AdDcosDeployDtl dtl = dtlList.get(i);
            if (dtl.getPriorityNum() != priorityNum) {
                priorityNum = checkPriorityNum(dtlList, appIdList, priorityNum, i, dtl);
            } else {
                appIdList.get(appIdList.size() - 1).add(dtlList.get(i).getAppid());
            }
        }
    }

    public void startDocsScale(AdSystemDeployLog deployLog, String appIdStr, StringBuilder sb) throws InterruptedException, TimeoutException {
        deployLog.setDeployComment(deployLog.getDeployComment() + "\n<h class=\"dcos-deploy-time\">" + sFormat.format(new Date()) + "</h> restart <h class=\"dcos-deploy-appid\">" + appIdStr + "</h>");
        StartNotifyResult startNotifyResult = this.start(appIdStr);
        if (!startNotifyResult.getReturnCode().equals(DcosResultState.Finished.getValue())) {
            logAppend(sb, "start", appIdStr, false, startNotifyResult.getReturnMsg());
            saveFailedDeployLog(deployLog, sb);
        } else {
            logAppend(sb, "start", appIdStr, true, "started");
            saveDeployLogByStringBuilder(deployLog, sb);
            //检查状态,写历史日志
            this.checkDeploySync(appIdStr, deployLog, sb);
        }
        systemDeployLogImpl.addLogsBySystemId(deployLog);
    }

    // 根据APPID 启动当前应用
    public void startDcos(Long deployInfoId, AdSystemDeployLog deployLog) {
        //记录发布历史
        StringBuilder sb = new StringBuilder();
        deployLog.setStartTime(new Date());
        deployLog.setDeployComment("begin start\n");
        String appId = deployLog.getAppId();
        AdDcosDeployInfo adDcosDeployInfo = dcosDeployInfoImpl.qryDcosDeployInfoById(deployInfoId);
        this.init(adDcosDeployInfo);
        List<AdDcosDeployDtl> dtlList = dcosDeployDtlDAO.qryByDcosDeployDtlAndAppIds(deployInfoId, appId);
        List<List<String>> appIdList = new ArrayList<>();
        sortAppIdPriorityNum(dtlList, appIdList); // 根据 appId 排优先级
        try {
            for (List<String> appIdStrList : appIdList) {
                if (appIdStrList.size() > 0) { //并发
                    NumberCount count = new NumberCount(appIdStrList.size());
                    for (String appIdStr : appIdStrList) {
                        Thread thread = new Thread(new StartDcosRunnable(deployLog, appIdStr, count, this, sb));
                        thread.start();
                    }
                    while (true) {
                        if (count.getNumber() == 0) {
                            break;
                        }
                        Thread.sleep(5000);
                    }
                } else {//顺序
                    String appIdStr = appIdStrList.get(0);
                    // 根据 appId  启动应用
                    startDocsScale(deployLog, appIdStr, sb);
                }
            }
            endDcosDeploy(deployLog, sb);
        } catch (Exception e) {
            sb.append("\n<h class=\"dcos-deploy-time\">").append(sFormat.format(new Date())).append("</h> ").append(e.getMessage());
            saveFailedDeployLog(deployLog, sb);
            deployLog.setDeployResult(2);
            deployLog.setEndTime(new Date());
            systemDeployLogImpl.addLogsBySystemId(deployLog);
        }
    }

}
