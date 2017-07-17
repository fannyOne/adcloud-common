package com.asiainfo.comm.module.build.controller;

import com.asiainfo.comm.common.pojo.pojoMaster.AdWorkspaceAddPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.ManualHandPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.OperationNowPojo;
import com.asiainfo.comm.common.pojo.pojoMaster.UsersPojo;
import com.asiainfo.util.CommConstants;
import com.asiainfo.util.JsonUtil;
import com.asiainfo.util.JsonpUtil;
import com.asiainfo.util.SshUtil;
import com.asiainfo.comm.module.build.service.impl.*;
import com.asiainfo.comm.module.models.*;
import com.asiainfo.comm.module.role.controller.BaseController;
import com.asiainfo.comm.module.role.service.impl.VerifyRightImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by YangRy on 2016/8/4.
 * Param "branchId" is necessary in this rote
 */
@RestController
@RequestMapping(value = "/branch")
public class AdBranchController extends BaseController {
    @Autowired
    AdBranchImpl branchImpl;
    @Autowired
    AdStageLogDtlImpl logDtlImpl;
    @Autowired
    AdProjectDeployPackageImpl deployPackageImpl;
    @Autowired
    AdJenkinsInfoImpl jenkinsInfoImpl;
    @Autowired
    JenkinsImpl jenkinsImpl;
    @Autowired
    AdStageImpl stageImpl;
    @Autowired
    AdPipeLineStateImpl pipeLineStateImpl;
    @Autowired
    AdUserImpl userImpl;
    @Autowired
    AdBuildLogImpl buildLogImpl;
    @Autowired
    AdBuildReturnValueImpl returnValueImpl;
    @Autowired
    AdUserBranchImpl userBranchImpl;
    @Autowired
    AdOperationImpl optService;
    @Autowired
    ProjectStateImpl projectStateImpl;
    @Autowired
    GitServiceImpl gitService;
    @Autowired
    AdBranchImpl adBranchImpl;
    @Autowired
    VerifyRightImpl verifyRightImpl;

    @RequestMapping(value = "/branchHello")
    public String helloWorld(@RequestParam Map map) throws Exception {
        return "You has come in";
    }


    @RequestMapping(value = "/qryLogUser")
    public String qryOpUserInLogDtl(@RequestParam Map param) {
        String branchId = (String) param.get("branchId");
        AdBranch adBranch = branchImpl.qryById(Long.parseLong(branchId));
        UsersPojo poj = new UsersPojo();
        poj = logDtlImpl.qryOpUserInLogDtl(adBranch, poj);
        return JsonpUtil.modelToJson(poj);
    }

    // 回滾
    @RequestMapping(value = "/rollBack")
    public ManualHandPojo rollBack(@RequestParam Map map, HttpServletRequest request) throws SQLException {
        ManualHandPojo poj = new ManualHandPojo();
        String branchIdStr = (String) map.get("branchId");
        //操作权限限制
        if (!verifyRightImpl.verifyBranchRight(Long.valueOf(branchIdStr), (String) request.getSession().getAttribute("username"))) {
            poj.setRetCode("500");
            poj.setRetMessage("该用户没有权限");
            return poj;
        }
        /* 资源隔离，权限验证 Over */
        Long branchId = Long.parseLong(branchIdStr);
        List<AdProjectDeployPackage> packageList = deployPackageImpl.qryByBranchId(branchId);
        if (packageList == null) {
            poj.setRetCode("500");
            poj.setRetMessage("找不到对应的包");
            return poj;
        }
        AdStage downloadStage = stageImpl.qryByBranchAndStageCode(branchId, 1);
        AdBranch adBranch = packageList.get(0).getAdBranch();
        AdStage deployStage = stageImpl.qryByBranchAndStageCode(branchId, 6);
        if (deployStage == null) {
            poj.setRetCode("500");
            poj.setRetMessage("没有对应节点");
            return poj;
        }
        long step = deployStage.getStep();

        AdJenkinsInfo jkInfo = jenkinsInfoImpl.qryByJkId(adBranch.getAdJenkinsInfo().getJenkinsId());
        String jenkinsUrl = jkInfo.getJenkinsUrl();
        String serverUsername = jkInfo.getServerUsername();
        String serverPassword = jkInfo.getServerPassword();
        //建立连接
        SshUtil sshUtil = new SshUtil(jenkinsUrl, serverUsername, serverPassword, "utf-8");
        //循环调用下载包脚本
        for (AdProjectDeployPackage deployPackage : packageList) {
            if (!deployPackageImpl.downloadPackageToHost(deployPackage, sshUtil)) {
                poj.setRetCode("500");
                poj.setRetMessage("下载包失败");
                return poj;
            }
        }
        /**
         * 执行回滚操作
         */
        //查询部署节点
        Long userId = (Long) request.getSession().getAttribute("userId");
        AdUser adUser = userImpl.qryById(userId);
        AdPipeLineState pipeLineState = deployStage.getAdPipeLineState();
        Long serialNumber = null;
        int busiType = CommConstants.BuildConstants.BUILDlOG.BUILD_TYPE.ROLLBACK;
        String seqNumStr = stageImpl.UpdateAdOperationByEnvId("" + branchId, "0");
        if (StringUtils.isNotEmpty(seqNumStr)) {
            serialNumber = Long.valueOf(seqNumStr);
        }
        if (pipeLineState != null) {
            pipeLineState.setAdBranch(adBranch);
            pipeLineState.setLastBuildResult(0);
            pipeLineState.setBranchState(2);
            pipeLineState.setBuildType(busiType);
            pipeLineStateImpl.updatePipeLineState(pipeLineState);
        }
        AdBuildLog adBuildLog = new AdBuildLog();
        adBuildLog.setBuildSeq(step);
        adBuildLog.setAdUser(adUser);
        adBuildLog.setBuildType(busiType);
        adBuildLog.setCreateDate(new java.util.Date());
        adBuildLog.setAdBranch(adBranch);
        adBuildLog.setBuildResult(0);
        adBuildLog.setState(1);
        adBuildLog.setLastStep((int) step);
        if (serialNumber != null) {
            adBuildLog.setTotalStep((int) serialNumber.longValue());
        }
        buildLogImpl.insertBuildLog(adBuildLog);
        //记录seqId
        AdBuildReturnValue adBuildReturnValue = null;
        if (pipeLineState != null) {
            adBuildReturnValue = returnValueImpl.qryBuildReturnValue(pipeLineState.getPipelineId(), step);
        }
        if (adBuildReturnValue == null) {
            adBuildReturnValue = new AdBuildReturnValue();
            adBuildReturnValue.setPipelineId(pipeLineState.getPipelineId());
            adBuildReturnValue.setBuildSeq(serialNumber);
            adBuildReturnValue.setStep(step);
            adBuildReturnValue.setNext_step(step + 1);
            returnValueImpl.insertBuildReturnSeq(adBuildReturnValue);
        } else {
            adBuildReturnValue.setBuildSeq(serialNumber);
            returnValueImpl.updateBuildReturnSeq(adBuildReturnValue);
        }
        //启动构建
        poj = jenkinsImpl.triggerJenkins(adBranch, deployStage);
        if (!poj.getRetCode().equals("200")) {
            pipeLineState.setBranchState(1);
            pipeLineState.setLastBuildResult(3);
            pipeLineStateImpl.updatePipeLineState(pipeLineState);
            stageImpl.updateState(deployStage, 3);
            poj.setRetCode("500");
            poj.setRetMessage("触发Jenkins失败");
        }
        downloadStage.setCommitId(downloadStage.getPreCommitId());
        stageImpl.save(downloadStage);
        /**
         * 执行回滚操作Over
         */
        return poj;
    }

    //重启
    @RequestMapping(value = "/restart")
    public ManualHandPojo restart(@RequestParam Map map, HttpServletRequest request) throws Exception {
        ManualHandPojo poj;
        Long branchId = Long.parseLong((String) map.get("branchId"));
        AdStage restartStage = stageImpl.qryByBranchAndStageCode(branchId, 7);//查出对应的job
        if (restartStage == null) {
            throw new Exception("没有对应的重启节点");
        }
        long step = restartStage.getStep();
        System.out.println("对应的步骤是step:" + step);
        //查询重启节点
        try {
            Long userId = (Long) request.getSession().getAttribute("userId");
            poj = branchImpl.beginStage(step, userId, branchId, restartStage, CommConstants.BuildConstants.BUILDlOG.BUILD_TYPE.RESTART);
        } catch (Exception e) {
            throw e;
        }
        /**
         * 执行重启操作Over
         */
        return poj;
    }

    @RequestMapping(value = "/redeploy")
    public ManualHandPojo redeploy(@RequestParam Map map, HttpServletRequest request) throws Exception {
        ManualHandPojo poj;
        Long branchId = Long.parseLong((String) map.get("branchId"));
        AdStage restartStage = stageImpl.qryByBranchAndStageCode(branchId, 6);
        if (restartStage == null) {
            throw new Exception("没有对应的重启节点");
        }
        long step = restartStage.getStep();
        System.out.println("对应的步骤是step:" + step);
        try {
            Long userId = (Long) request.getSession().getAttribute("userId");
            poj = branchImpl.beginStage(step, userId, branchId, restartStage, CommConstants.BuildConstants.BUILDlOG.BUILD_TYPE.REDEPLOY);
        } catch (Exception e) {
            throw e;
        }
        return poj;
    }

    //在个人工作台中添加我关注的流水线
    @RequestMapping(value = "/addBranchToWorkSpace", produces = "application/json")
    public String addBranchToWorkSpace(@RequestParam Map param, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String branchIdStr = (String) param.get("branchId");
        Long branchId;
        AdWorkspaceAddPojo poj = new AdWorkspaceAddPojo();
        poj.setFlag(false);
        if (StringUtils.isNotEmpty(branchIdStr) && userId != null && !userId.equals(0L)) {
            branchId = Long.parseLong(branchIdStr);
            branchImpl.addBranchToWorkSpace(poj, branchId, userId, username);
        }
        return JsonpUtil.modelToJson(poj);
    }

    //在个人工作台中删除我关注的流水线
    @RequestMapping(value = "/delBranchToWorkSpace", produces = "application/json")
    public String delBranchToWorkSpace(@RequestParam Map param, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String branchIdStr = (String) param.get("branchId");
        Long branchId;
        AdWorkspaceAddPojo poj = new AdWorkspaceAddPojo();
        poj.setFlag(false);
        if (StringUtils.isNotEmpty(branchIdStr) && userId != null && !userId.equals(0L)) {
            branchId = Long.parseLong(branchIdStr);
            if (!userId.equals(0L)) {
                List<AdUserBranch> userBranchList = userBranchImpl.qryByUserAndBranch(branchId, userId);
                userBranchImpl.del(userBranchList);
                poj.setFlag(true);
            } else {
                List<AdUserBranch> userBranchList = userBranchImpl.qryByUserAndBranch(branchId, username);
                userBranchImpl.del(userBranchList);
                poj.setFlag(true);
            }
        }
        return JsonpUtil.modelToJson(poj);
    }

    @RequestMapping(value = "/opts", produces = "application/json")
    public String opts(@RequestParam long branchId) throws IOException {
        OperationNowPojo pojo;
        pojo = optService.qryPipsByBranch(branchId);
        return JsonpUtil.modelToJson(pojo);
    }

    @RequestMapping(value = "/fivebuild", produces = "application/json")
    public String fivebuild(@RequestParam Map map) throws IOException {
        long begin_time = System.currentTimeMillis();
        long branchId = map.get("branchId") != null ? Long.valueOf((String) map.get("branchId")) : 0;

//        Map hmap = projectStateImpl.qryFiveBuildResult(projectId);
        Map hMap = projectStateImpl.qryFiveBuildResultByBranch(branchId);
        String ret = JsonUtil.mapToJson(hMap);
        long end_time = System.currentTimeMillis();
        System.out.print("***************" + (end_time - begin_time));
        return ret;
    }

    @RequestMapping(value = "/qryProjectVersion", produces = "application/json")
    public String qryProjectVersion(@RequestParam Map map) throws Exception {
        String retvalue;
        long branchId;
        int buildType;
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            if (map != null && map.get("branchId") != null) {
                branchId = Long.valueOf((String) map.get("branchId"));
            } else {
                throw new Exception("项目编号不正确");
            }
            if (map.get("buildType") != null) {
                buildType = Integer.valueOf((String) map.get("buildType"));
            } else {
                throw new Exception("构建类型不正确");
            }
            retMap.put("version", gitService.qryProjectVersionByBranch(branchId));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        retvalue = JsonUtil.mapToJson(retMap);
        return retvalue;
    }

}
